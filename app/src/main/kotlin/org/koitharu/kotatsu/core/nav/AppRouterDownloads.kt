/*
 * Saving manga for offline reading, and where it is stored.
 *
 * These were members of AppRouter, which had grown to 862 lines covering every screen in the app.
 * They are extensions rather than members so that each area owns its own navigation in its own
 * file. Call sites read the same as before — `router.openDetails(manga)` — but now need an import
 * for the function they use.
 */
package org.koitharu.kotatsu.core.nav

import android.content.DialogInterface
import android.view.View
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.model.parcelable.ParcelableManga
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_MANGA
import org.koitharu.kotatsu.core.prefs.TriStateOption
import org.koitharu.kotatsu.core.ui.dialog.BigButtonsAlertDialog
import org.koitharu.kotatsu.core.util.ext.connectivityManager
import org.koitharu.kotatsu.core.util.ext.withArgs
import org.koitharu.kotatsu.download.ui.dialog.DownloadDialogFragment
import org.koitharu.kotatsu.download.ui.list.DownloadsActivity
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.util.mapToArray
import org.koitharu.kotatsu.settings.storage.MangaDirectorySelectDialog
import org.koitharu.kotatsu.settings.storage.directories.MangaDirectoriesActivity

fun AppRouter.openDownloads() = startActivity(DownloadsActivity::class.java)

fun AppRouter.openDirectoriesSettings() = startActivity(MangaDirectoriesActivity::class.java)

fun AppRouter.showDownloadDialog(manga: Manga, snackbarHost: View?) = showDownloadDialog(setOf(manga), snackbarHost)

fun AppRouter.showDownloadDialog(manga: Collection<Manga>, snackbarHost: View?) {
    if (manga.isEmpty()) {
        return
    }
    val fm = getFragmentManager() ?: return
    if (snackbarHost != null) {
        getLifecycleOwner()?.let { lifecycleOwner ->
            DownloadDialogFragment.registerCallback(fm, lifecycleOwner, snackbarHost)
        }
    } else {
        DownloadDialogFragment.unregisterCallback(fm)
    }
    DownloadDialogFragment().withArgs(1) {
        putParcelableArray(KEY_MANGA, manga.mapToArray { ParcelableManga(it, withDescription = false) })
    }.showDistinct()
}

fun AppRouter.showDirectorySelectDialog() {
    MangaDirectorySelectDialog().showDistinct()
}

fun AppRouter.askForDownloadOverMeteredNetwork(onConfirmed: (allow: Boolean) -> Unit) {
    val context = contextOrNull() ?: return
    when (settings.allowDownloadOnMeteredNetwork) {
        TriStateOption.ENABLED -> onConfirmed(true)
        TriStateOption.DISABLED -> onConfirmed(false)
        TriStateOption.ASK -> {
            if (!context.connectivityManager.isActiveNetworkMetered) {
                onConfirmed(true)
                return
            }
            val listener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        settings.allowDownloadOnMeteredNetwork = TriStateOption.ENABLED
                        onConfirmed(true)
                    }

                    DialogInterface.BUTTON_NEUTRAL -> {
                        onConfirmed(true)
                    }

                    DialogInterface.BUTTON_NEGATIVE -> {
                        settings.allowDownloadOnMeteredNetwork = TriStateOption.DISABLED
                        onConfirmed(false)
                    }
                }
            }
            BigButtonsAlertDialog.Builder(context)
                .setIcon(R.drawable.ic_network_cellular)
                .setTitle(R.string.download_cellular_confirm)
                .setPositiveButton(R.string.allow_always, listener)
                .setNeutralButton(R.string.allow_once, listener)
                .setNegativeButton(R.string.dont_allow, listener)
                .create()
                .show()
        }
    }
}
