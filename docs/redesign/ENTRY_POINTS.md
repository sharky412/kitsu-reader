# Entry-point inventory

Every way the app can be entered from outside its own UI. The redesign changes navigation and
screen structure, so each of these has to keep resolving to an equivalent destination.

Source of truth: `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/`,
`core/os/AppShortcutManager.kt`, `core/nav/AppRouter.kt`.

Verified against the tree at the start of the redesign (version 9.4.1, versionCode 1033).

---

## 1. Launcher

| Component | Filter |
| --- | --- |
| `main.ui.MainActivity` | `MAIN` / `LAUNCHER`, `exported=true` |

`MainActivity` also declares `android.app.default_searchable`. **The value points at
`org.koitharu.kotatsu.search.ui.multi.MultiSearchActivity`, which does not exist in the source tree**
(`AndroidManifest.xml:79`; the real class is `search.ui.multi.SearchActivity`). System-driven search
delegation to that activity therefore cannot work today. Recorded here as a pre-existing defect, not
introduced by the redesign; fix deliberately and separately so the change is attributable.

## 2. Custom app actions (exported, third-party reachable)

These are `exported="true"` with `${applicationId}`-namespaced actions. They are used by the app's
own widgets and shortcuts, and are reachable by any other app on the device.

| Action | Target | Payload |
| --- | --- | --- |
| `${applicationId}.action.VIEW_MANGA` | `details.ui.DetailsActivity` | `ParcelableManga` or manga id |
| `${applicationId}.action.READ_MANGA` | `reader.ui.ReaderActivity` | manga + optional `ReaderState` |
| `${applicationId}.action.EXPLORE_MANGA` | `search.ui.MangaListActivity` | source + optional filter/sort |

Intent construction lives in `AppRouter`'s companion object; extras are read back through
`MangaIntent` / `ReaderIntent`. Extra keys are part of this contract, not internal detail, and the
companion is deliberately the one part of the router that was **not** split up — widgets, shortcuts
and other apps reach these factories as `AppRouter.detailsIntent(...)` and that spelling must hold.
The screen-opening functions now live in `AppRouter<Area>.kt` files as extensions.

## 3. Web and scheme deep links

| Component | Scheme | Hosts |
| --- | --- | --- |
| `details.ui.DetailsActivity` | `http`, `https` | `kotatsu.app`, `android:path="/manga"` — an **exact** path |
| `details.ui.DetailsActivity` | `kotatsu` | `manga` |
| `details.ui.DetailsByLinkActivity` (alias of `DetailsActivity`) | `http`, `https` | **1174 `android:host` entries, 1133 distinct** — every supported site (bato.to family, mangadex.org, webtoons.com, mangafire.to, comick.io, hitomi.la, …) |
| `settings.SettingsActivity` | `kotatsu` | `about`, `sync-settings` |
| `scrobbling.common.ui.config.ScrobblerConfigActivity` | `kotatsu` | `shikimori-auth`, `anilist-auth`, `mal-auth`, `kitsu-auth` |
| `scrobbling.kitsu.ui.KitsuAuthActivity` | `kotatsu+kitsu` | — (`exported=false`) |

Notes:

* `DetailsByLinkActivity` is an `activity-alias` targeting `DetailsActivity`; its host list contains
  41 duplicate entries (`webtoons.com` and `mangafire.to` appear seven times each). Harmless, but
  do not treat the raw count as the number of supported sites.
* The scrobbler OAuth redirect hosts resolve to `ScrobblerConfigActivity` only. An earlier revision
  of this file also listed `sync.ui.SyncAuthActivity` against them; that is wrong. `SyncAuthActivity`
  is exported but declares no intent filter at all, so nothing routes to it from outside the app.
* Because `kotatsu.app` uses `android:path` rather than `android:pathPrefix`, only the bare
  `https://kotatsu.app/manga` matches. A link that carries an id in the path, such as
  `https://kotatsu.app/manga/1001`, resolves to no component in this app — the manga is identified by
  query parameters, not by the path.

## 4. System integrations

| Integration | Component | Notes |
| --- | --- | --- |
| Image picker for other apps | `picker.ui.PageImagePickActivity` | `GET_CONTENT` + `PICK`, `image/*`, `OPENABLE`. Other applications call this; `PageImagePickContract` is the in-app counterpart. Breaking it breaks third-party apps, not just Kotatsu. |
| Samsung remote control | `reader.ui.ReaderActivity` | `com.samsung.android.support.REMOTE_ACTION` with `@xml/remote_action`, mapping trigger keys `L`/`R` to previous/next page. These map onto `KEYCODE_L` / `KEYCODE_R` in `ReaderControlDelegate.onKeyDown`; removing those key cases disables Samsung remote and S Pen gestures. |
| App widgets | `widget.recent.RecentWidgetProvider`, `widget.shelf.ShelfWidgetProvider` | `APPWIDGET_UPDATE`, each with a config activity (`RecentWidgetConfigActivity`, `ShelfWidgetConfigActivity`) declared through `android:configure`. Content is served by `RemoteViewsService` implementations and refreshed by `WidgetUpdater`, which is registered as a Room `InvalidationTracker.Observer`. |
| Account manager | `sync.ui.SyncAuthenticatorService` | `android.accounts.AccountAuthenticator`, `@xml/authenticator_sync` |
| Sync adapters | `sync.ui.favourites.FavouritesSyncService`, `sync.ui.history.HistorySyncService` | `android.content.SyncAdapter`, backed by `FavouritesSyncProvider` / `HistorySyncProvider` (`syncable=true`) |
| Crash reporting | `core.ErrorReporterReceiver` | `${applicationId}.action.REPORT_ERROR`, invoked from ACRA notifications |
| Captcha dismissal | `core.exceptions.resolve.CaptchaHandler$DiscardReceiver` | `org.koitharu.kotatsu.CAPTCHA_DISCARD` |
| Android Auto Backup | `backups.domain.AppBackupAgent` | `@xml/backup_rules`, `@xml/backup_content` |

## 5. Content providers

| Authority | Provider | Purpose |
| --- | --- | --- |
| `${applicationId}.files` | `androidx.core.content.FileProvider` | sharing pages, exporting backups (`@xml/filepaths`) |
| `${applicationId}.MangaSuggestionsProvider` | `search.ui.MangaSuggestionsProvider` | recent search queries (`SearchRecentSuggestions`) |
| `@string/sync_authority_favourites` | `FavouritesSyncProvider` | sync adapter surface |
| `@string/sync_authority_history` | `HistorySyncProvider` | sync adapter surface |

`androidx.startup.InitializationProvider` is explicitly removed (`tools:node="remove"`); WorkManager
is initialised manually through `BaseApp : Configuration.Provider`. Restoring the default
initializer would double-initialise WorkManager.

## 6. Shortcuts

Managed entirely at runtime by `core/os/AppShortcutManager.kt`; there is no `res/xml/shortcuts.xml`.

* **Dynamic** — rebuilt from reading history whenever the `history` table is invalidated, capped at
  `ShortcutManagerCompat.getMaxShortcutCountPerActivity()` (minimum 5). Shortcut **id is the manga
  id as a string**; `notifyMangaOpened` reports usage with the same id. Gated by
  `AppSettings.KEY_SHORTCUTS`.
* **Pinned** — user-initiated for a manga or for a source (`requestPinShortcut`).

Both resolve through `AppRouter` intents, so a change to those intents changes the behaviour of
shortcuts a user pinned months ago.

## 7. Notifications

Not manifest-declared, but they are entry points and they carry actions:

| Source | Actions |
| --- | --- |
| `download.ui.worker.DownloadNotificationFactory` | open, pause, resume, cancel (delivered to `PausingReceiver` as broadcasts) |
| `tracker.work.TrackerNotificationHelper` | open manga, per-manga channels, grouped summary |
| `suggestions.ui.SuggestionsWorker` | open suggestions settings, cancel work |
| ACRA dialog | send report |

## 8. Verification record

Re-verified on an Android 34 emulator against the redesigned navigation (Home, Library, History,
Explore, Updates), by firing each intent with `adb` and reading back the resolved component, rather
than by reading the manifest.

Resolving as documented: `VIEW_MANGA`, `READ_MANGA` and `EXPLORE_MANGA`; `kotatsu://manga/...`,
`kotatsu://about`, `kotatsu://sync-settings`, and all four scrobbler auth hosts;
`https://kotatsu.app/manga`; `DetailsByLinkActivity` for mangadex.org, webtoons.com, comick.io and
hitomi.la; `GET_CONTENT image/*` to `PageImagePickActivity`; the Samsung remote action on
`ReaderActivity`; both widget providers; both content provider authorities; the ACRA and captcha
receivers; and the authenticator and both sync-adapter services.

Dynamic shortcuts were verified end to end: they rebuild only when Room invalidates the `history`
table, so writing history straight into the database does not produce them. Deleting one entry from
the history screen rebuilt all five, each keyed by the manga id as a string and each carrying a
`READ_MANGA` intent to `ReaderActivity` — exactly as section 6 describes, and unchanged by the new
navigation.

Not verifiable this way, and still to be checked by hand: pinned shortcuts created before the
change, widget rendering and their configuration activities, a full scrobbler OAuth round trip, and
notification actions.

## 9. Redesign checklist for this file

When navigation changes, each row above must be re-verified end to end on a device, not by reading
code. `docs/redesign/REGRESSION_CHECKLIST.md` section 1 tracks that verification.
