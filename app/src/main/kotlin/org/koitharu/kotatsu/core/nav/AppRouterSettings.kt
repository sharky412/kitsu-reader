/*
 * Settings screens, and the app-level sheets and dialogs that belong with them.
 *
 * These were members of AppRouter, which had grown to 862 lines covering every screen in the app.
 * They are extensions rather than members so that each area owns its own navigation in its own
 * file. Call sites read the same as before — `router.openDetails(manga)` — but now need an import
 * for the function they use.
 */
package org.koitharu.kotatsu.core.nav

import android.content.Intent
import android.net.Uri
import org.koitharu.kotatsu.backups.ui.backup.BackupDialogFragment
import org.koitharu.kotatsu.backups.ui.restore.RestoreDialogFragment
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_DATA
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_FILE
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_ID
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_LIST_SECTION
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.discordSettingsIntent
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.downloadsSettingsIntent
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.proxySettingsIntent
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.readerSettingsIntent
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.sourceSettingsIntent
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.sourcesSettingsIntent
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.suggestionsSettingsIntent
import org.koitharu.kotatsu.core.util.ext.withArgs
import org.koitharu.kotatsu.list.ui.config.ListConfigBottomSheet
import org.koitharu.kotatsu.list.ui.config.ListConfigSection
import org.koitharu.kotatsu.local.ui.ImportDialogFragment
import org.koitharu.kotatsu.main.ui.welcome.WelcomeSheet
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.scrobbling.common.domain.model.ScrobblerService
import org.koitharu.kotatsu.scrobbling.common.ui.config.ScrobblerConfigActivity
import org.koitharu.kotatsu.settings.SettingsActivity
import org.koitharu.kotatsu.settings.about.AppUpdateActivity
import org.koitharu.kotatsu.settings.reader.ReaderTapGridConfigActivity
import org.koitharu.kotatsu.settings.tracker.categories.TrackerCategoriesConfigSheet
import org.koitharu.kotatsu.stats.ui.StatsActivity

fun AppRouter.openAppUpdate() = startActivity(AppUpdateActivity::class.java)

fun AppRouter.openSettings() = startActivity(SettingsActivity::class.java)

fun AppRouter.openReaderSettings() {
    startActivity(readerSettingsIntent(contextOrNull() ?: return))
}

fun AppRouter.openProxySettings() {
    startActivity(proxySettingsIntent(contextOrNull() ?: return))
}

fun AppRouter.openDownloadsSetting() {
    startActivity(downloadsSettingsIntent(contextOrNull() ?: return))
}

fun AppRouter.openSourceSettings(source: MangaSource) {
    startActivity(sourceSettingsIntent(contextOrNull() ?: return, source))
}

fun AppRouter.openSuggestionsSettings() {
    startActivity(suggestionsSettingsIntent(contextOrNull() ?: return))
}

fun AppRouter.openSourcesSettings() {
    startActivity(sourcesSettingsIntent(contextOrNull() ?: return))
}

fun AppRouter.openDiscordSettings() {
    startActivity(discordSettingsIntent(contextOrNull() ?: return))
}

fun AppRouter.openReaderTapGridSettings() = startActivity(ReaderTapGridConfigActivity::class.java)

fun AppRouter.openScrobblerSettings(scrobbler: ScrobblerService) {
    startActivity(
        Intent(contextOrNull() ?: return, ScrobblerConfigActivity::class.java)
            .putExtra(KEY_ID, scrobbler.id),
    )
}

fun AppRouter.openStatistic() = startActivity(StatsActivity::class.java)

fun AppRouter.showBackupRestoreDialog(fileUri: Uri) {
    RestoreDialogFragment().withArgs(1) {
        putString(KEY_FILE, fileUri.toString())
    }.show()
}

fun AppRouter.createBackup(destination: Uri) {
    BackupDialogFragment().withArgs(1) {
        putParcelable(KEY_DATA, destination)
    }.showDistinct()
}

fun AppRouter.showImportDialog() {
    ImportDialogFragment().showDistinct()
}

fun AppRouter.showListConfigSheet(section: ListConfigSection) {
    ListConfigBottomSheet().withArgs(1) {
        putParcelable(KEY_LIST_SECTION, section)
    }.showDistinct()
}

fun AppRouter.showWelcomeSheet() {
    WelcomeSheet().showDistinct()
}

fun AppRouter.showTrackerCategoriesConfigSheet() {
    TrackerCategoriesConfigSheet().showDistinct()
}

fun AppRouter.closeWelcomeSheet(): Boolean {
    val tag = fragmentTag<WelcomeSheet>()
    val sheet = fragment?.findFragmentByTagRecursive(tag)
        ?: activity?.supportFragmentManager?.findFragmentByTag(tag)
        ?: return false
    return if (sheet is WelcomeSheet) {
        sheet.dismissAllowingStateLoss()
        true
    } else {
        false
    }
}
