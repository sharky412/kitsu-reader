# Manual regression checklist

The redesign changes presentation and navigation while leaving business logic in place. Most of the
ways that can go wrong are invisible to the compiler: an observer that is no longer registered, a
snackbar host that no longer exists, an intent that no longer resolves. This checklist covers
exactly those cases.

**How to use it.** Run the sections affected by the phase you are finishing, on a real device, on a
build installed **over** an existing install with real data — never a fresh install. A fresh install
hides every persistence regression this list is designed to catch.

Automated coverage that already exists:

* `app/src/test/.../AppSettingsKeysTest.kt` — preference keys
* `app/src/test/.../PersistedEnumsTest.kt` — persisted enum names and `ReaderMode` ids
* `app/src/test/.../ListItemTypeTest.kt` — RecyclerView view types
* `app/src/test/.../NavItemTest.kt` — navigation menu ids
* `app/src/androidTest/.../MangaDatabaseTest.kt` — Room migration chain
* `app/src/androidTest/.../AppShortcutManagerTest.kt` — dynamic shortcuts
* `app/src/androidTest/.../AppBackupAgentTest.kt` — backup round trip
* `app/src/test/.../screenshot/ComponentScreenshotTest.kt` — visual baselines for shared list
  components in light and dark; see `LOCAL_SETUP.md` for the record/verify workflow

Anything below that an automated test already covers is marked *(covered)* and only needs a device
check when its area was touched.

---

## 1. Entry points

Run after any navigation change. See `ENTRY_POINTS.md` for what each one is.

Ticked rows were re-verified on an emulator after the navigation change, by firing the intent and
reading back the component that answered; `ENTRY_POINTS.md` section 8 records what was run. A tick
here means the entry point still routes where it used to, which is what a navigation change can
break. It does not mean the whole flow behind it was walked through — the unticked rows need a
person, a network, a launcher, or a real account.

- [x] Launcher icon opens the app at the expected destination.
- [x] Dynamic shortcuts list recent manga and carry a `READ_MANGA` intent to the reader, keyed by
      manga id. Rebuilt only on a Room invalidation of `history`, so a database-level seed does not
      produce them.
- [ ] A pinned manga shortcut created **before** the change still opens the right manga. *(by hand)*
- [ ] A pinned source shortcut still opens that source's list.
- [ ] Recent widget: renders, and tapping an item opens the reader. *(by hand; the provider is
      registered)*
- [ ] Shelf widget: renders the configured category, tapping opens details.
- [ ] Adding each widget from the launcher opens its config activity and completes.
- [x] A supported manga URL resolves to `DetailsByLinkActivity` (checked mangadex.org,
      webtoons.com, comick.io, hitomi.la). Whether the manga itself is then found needs a network.
- [x] `kotatsu://manga/...` and `https://kotatsu.app/manga` links resolve. Note that
      `https://kotatsu.app/manga/<id>` does **not** — the filter declares an exact path.
- [x] `kotatsu://about` and `kotatsu://sync-settings` open the right settings screens.
- [ ] Scrobbler OAuth: complete a full link flow for at least one service; the callback returns into
      the app and the account appears as linked. *(by hand; the four auth links do resolve to
      `ScrobblerConfigActivity`)*
- [x] Another app can pick an image through Kotatsu: `GET_CONTENT image/*` resolves to
      `PageImagePickActivity`.
- [x] After the `AppRouter` split, the custom actions and scheme links above were re-run, and
      in-app routes driven by the moved code were exercised on a device: a home rail cover to
      details, a rail action to the history tab, the favourites overflow to Settings and to
      Favorite categories.
- [ ] Sharing a page out of the reader produces a working file (FileProvider).
- [ ] Notification actions: download pause / resume / cancel, tracker "open manga", ACRA send.
      *(by hand; the ACRA and captcha receivers resolve)*

## 2. Persistence and settings

- [ ] Existing preferences survive the upgrade: theme, colour scheme, list mode, grid size,
      reader mode, sort orders, nav bar layout. *(covered by unit tests; still spot-check on device)*
- [ ] Per-manga reader mode set before the upgrade is still applied.
- [ ] Per-manga colour filter set before the upgrade is still applied.
- [ ] Per-manga title/cover override still displays.
- [ ] Per-category sort order still applies to that category.
- [ ] Settings search finds a preference from every group.
- [ ] Changing tracker frequency or enabling/disabling suggestions reschedules the worker
      (`WorkScheduleManager` reacts to the key, verify in WorkInspector on a debug build).

## 3. Library, history, bookmarks

- [ ] Favourites categories: create, rename, reorder, delete, hide from library.
- [ ] Add a manga to multiple categories; the heart state reflects it everywhere it is shown.
- [ ] Remove from favourites, then **undo** from the snackbar; the manga returns to the same
      categories. Repeat for remove-from-history and disable-source.
- [ ] Multi-select action mode in every list: select, select-all, act, dismiss; back exits selection
      rather than the screen.
- [ ] History shows correct progress percentages and groups by date.
- [ ] Clear history by date range removes only the expected entries.
- [ ] Incognito mode: reading does not write history; the state is visible while active.
- [ ] Bookmarks created before the change still open at the exact page and scroll offset.
- [ ] Quick filters (Downloaded, Completed, New chapters, Favourite, tag, source) return the same
      sets as before.
- [ ] Segmented destinations (Library: Favorites, On device, Bookmarks; Updates: New chapters,
      Feed): each segment shows the same screen it showed as a standalone destination, including its
      own menu items, filters and empty state.
- [ ] Switching segments and returning keeps the previous segment where it was — the same favourite
      category selected, the same scroll position, an active selection intact.
- [ ] The chosen segment survives rotation and process death.
- [ ] Every segment is still reachable on its own for readers who put Favorites, On device,
      Bookmarks, Updated or Feed in their navigation bar directly.
- [ ] No segment opens with a band of empty space under the segment row. A screen that pads itself
      by the top system-bar inset double-counts the app bar when it is hosted as a segment; the
      container consumes that inset for exactly this reason.
- [ ] With tracking turned off, Updates carries the same "checking for new chapters is disabled"
      hint as Feed in the navigation settings screen. Note that no navigation entry is ever *hidden*
      for being unavailable — `NavItem.isAvailable` only drives that hint.

- [ ] List items: compact rows, detailed rows, grid cells and rail cells all show the title in the
      same style, secondary text in the same colour, and cover overlays inset the same distance.
      See `LIST_SYSTEM.md` for the rules and for what was deliberately left alone.
- [ ] Section headers read as headers, one step above a row title, in every grouped list.

## 4. Sources, search, filtering

- [ ] Enable, disable, pin and reorder sources; the order persists across restart.
- [ ] Source catalog filters by locale and content type.
- [ ] Per-source settings open, and mirror switching still works on a source that has mirrors.
- [ ] A source that requires auth still routes into `SourceAuthActivity`.
- [ ] A CloudFlare-protected source still routes into `CloudFlareActivity` and succeeds afterwards.
- [ ] Filter sheet shows **only** the facets the source supports — check one source without tag
      exclusion, one without author search, one without year filtering.
- [ ] Applied filters survive rotation and process death.
- [ ] Saved filters can be created and re-applied.
- [ ] Multi-source search returns results progressively and the "search disabled sources"
      continuation still works.
- [ ] Search suggestions show recent queries, tags, authors and sources; deleting a recent query
      works.
- [ ] Voice search input still populates the query.

## 5. Details and reader

- [ ] Details opens from every path: list, search, history, widget, shortcut, deep link, related.
- [ ] Branch (translation) selector switches chapter lists and remembers the choice.
- [ ] Chapter list shows read / unread / downloaded / current state correctly.
- [ ] "Continue" resolves to the correct next chapter in the selected branch.
- [ ] All reader modes render and paginate: standard, reversed, vertical, webtoon, plus double-page
      in landscape and on an unfolded foldable.
- [ ] Tap grid: every one of the nine areas, tap and long-press, performs its configured action.
- [ ] Hardware keys: volume up/down, D-pad, page up/down, space, `L`/`R`, Ctrl+key for chapters.
- [ ] Reading position is saved when leaving via back, home, and process death, at the exact page
      **and** scroll offset.
- [ ] Reading a chapter updates history, stats, scrobbler progress and Discord presence.
- [ ] Page save, page share, and "open in browser" work from the reader.
- [ ] Screenshot policy: with the NSFW or block-all setting, the recents thumbnail is hidden and
      capture is blocked.
- [ ] Reader is usable offline on a downloaded chapter with airplane mode on.

- [ ] Details: a manga with no description shows neither the heading nor a placeholder, and the tags
      sit directly under the details card.
- [ ] Details: field names in the table read as labels, section headings read as headings, and both
      match the equivalents in a list.
- [x] Details for a local manga: chapters resolve, the read button is enabled, and the table shows
      only the fields that apply. Verified with `seed-device.py --local-manga`.
- [ ] Chapter rows shrink to one line when a chapter has no date or scanlator, and grow to two when
      it does, without dropping below a 48dp touch target.

## 6. Downloads and offline

- [ ] Queue a multi-chapter download; progress, ETA and notification update.
- [ ] Pause, resume, skip chapter, skip all, cancel — each takes effect within a few seconds.
- [ ] Kill the app mid-download; the work resumes and the notification returns.
- [ ] A failed download surfaces an actionable error, and retry succeeds after resolving it.
- [ ] Metered-network restriction is honoured, and toggling it updates already-queued work.
- [ ] Completed downloads appear in the offline library and are readable with no network.
- [ ] Each download format produces a readable result: single CBZ, multiple CBZ, directory.
- [ ] Import a third-party CBZ; it appears and opens.
- [ ] Delete read chapters, and delete a whole local manga; storage figures update.
- [ ] Multi-directory storage: add a second directory and download into it.

## 7. Background work and notifications

- [ ] Tracker run produces per-manga notifications, a grouped summary, and feed entries.
- [ ] Tracker auto-download strategy still enqueues downloads when enabled.
- [ ] Suggestions worker refreshes recommendations.
- [ ] Periodic backup produces a file, honours retention, and uploads when configured.
- [ ] Restore from a backup produced by the **pre-redesign** build succeeds and restores every
      section. *(partially covered by `AppBackupAgentTest`)*
- [ ] Sync: an account still authenticates and history/favourites converge across two devices.

- [ ] Settings rows use Material's own title size; the root settings list fits its ten entries
      without the last one being pushed under the navigation bar.

## 8. Cross-cutting UI

- [ ] Tablet layout: navigation rail, side sheets, two-pane settings.
- [ ] Foldable: fold and unfold while reading; double-page toggles as configured.
- [ ] Rotation on every major screen preserves scroll position and state.
- [ ] Dark, light, AMOLED, and dynamic colour all render with no unreadable text.
- [ ] An RTL locale (Arabic or Hebrew) lays out correctly, including the reader.
- [ ] A long-text locale (German or Russian) does not truncate or overlap labels.
- [ ] TalkBack can reach and describe primary actions on Home, Library, Details and the reader.
- [ ] Touch targets on the reader chrome are at least 48dp.
- [ ] App lock: password and biometric both gate the app on cold start and on return from
      background.
- [ ] Low-RAM device or emulator: lists scroll without OOM, covers still load.
- [ ] API 23 device or emulator: app starts, reads, downloads.

- [ ] Reader: the position bar's digits hold their place as pages turn — page, percentage, clock and
      battery all use tabular figures, so nothing shifts sideways while reading.
- [ ] Branding: launcher name and icon read Kitsu; the fox mark survives every launcher mask shape,
      including a circle. Package name, deep-link schemes and sync account type are unchanged, so
      existing installs, links and synced accounts keep working.

## 9. Sign-off

A phase is done when the sections it touched are green, `./gradlew :app:verifyRoborazziDebug` and
`:app:lintDebug` pass, and any newly discovered invariant has been added to this list or, better,
turned into a test.

## 10. Known pre-existing defects

Found while building this checklist. They are not regressions, and fixing them should be separate,
attributable commits.

* `AndroidManifest.xml:79` declares `android.app.default_searchable` as
  `org.koitharu.kotatsu.search.ui.multi.MultiSearchActivity`, a class that does not exist. The real
  class is `SearchActivity`. System search delegation cannot work as declared.
* `values/themes.xml` never sets `android:textColorPrimary` or `android:textColorSecondary`
  explicitly, but this is **not** a defect: those attributes come from `Theme.Material3.DayNight`
  and resolve through `?attr/colorOnSurface`, which the base theme does map to `@color/kotatsu_*`.
  Mutating `kotatsu_onSurface` fails screenshot verification, so the colour tokens genuinely drive
  rendered text.

  An earlier revision of this document claimed the opposite. That claim came from the screenshot
  harness calling `Activity.setTheme()` after the activity was created, which left some components
  resolving against platform defaults. The harness now inflates through an explicit
  `ContextThemeWrapper`; do not reintroduce the old pattern.
* The `DetailsByLinkActivity` deep-link filter contains 1174 `android:host` entries of which 1133
  are distinct; `webtoons.com` and `mangafire.to` are each repeated seven times.
* `NavItem.UPDATED` reports itself unavailable when tracking is off, but
  `NavConfigViewModel.getUnavailabilityHint` maps no string for it, so it silently shows no
  explanation where Feed shows one. Left as-is: fixing it is a one-line change in code this branch
  does not otherwise touch, and it deserves its own commit.
* `TrimTransformation` produced a zero-height crop for any cover whose rows are each a single
  colour — a solid fill, a vertical gradient, a generated placeholder. It guarded the equivalent
  case for columns but not for rows, so `Bitmap.createBitmap` threw. Coil catches the exception, so
  the visible symptom was a cover that rendered as an error tile with no crash and no user-facing
  message. **Fixed** in this branch with a matching guard and `TrimTransformationTest`; the fix was
  confirmed by red-green (removing the guard fails the test).
