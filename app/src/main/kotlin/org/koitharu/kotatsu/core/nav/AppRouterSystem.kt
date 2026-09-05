/*
 * Leaving the app: the built-in browser, external apps and system screens.
 *
 * These were members of AppRouter, which had grown to 862 lines covering every screen in the app.
 * They are extensions rather than members so that each area owns its own navigation in its own
 * file. Call sites read the same as before — `router.openDetails(manga)` — but now need an import
 * for the function they use.
 */
package org.koitharu.kotatsu.core.nav

import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import androidx.annotation.CheckResult
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.ACCOUNT_KEY
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.ACTION_ACCOUNT_SYNC_SETTINGS
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.EXTRA_SHOW_FRAGMENT_ARGUMENTS
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_ERROR
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_URL
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.browserIntent
import org.koitharu.kotatsu.core.ui.dialog.ErrorDetailsDialog
import org.koitharu.kotatsu.core.util.ext.toUriOrNull
import org.koitharu.kotatsu.core.util.ext.withArgs
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.util.isNullOrEmpty

fun AppRouter.openBrowser(url: String, source: MangaSource?, title: String?) {
    startActivity(browserIntent(contextOrNull() ?: return, url, source, title))
}

fun AppRouter.openBrowser(manga: Manga) = openBrowser(
    url = manga.publicUrl,
    source = manga.source,
    title = manga.title,
)

@CheckResult
fun AppRouter.openExternalBrowser(url: String, chooserTitle: CharSequence? = null): Boolean {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = url.toUriOrNull() ?: return false
    return startActivitySafe(
        if (!chooserTitle.isNullOrEmpty()) {
            Intent.createChooser(intent, chooserTitle)
        } else {
            intent
        },
    )
}

@CheckResult
fun AppRouter.openSystemSyncSettings(account: Account): Boolean {
    val args = Bundle(1)
    args.putParcelable(ACCOUNT_KEY, account)
    val intent = Intent(ACTION_ACCOUNT_SYNC_SETTINGS)
    intent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, args)
    return startActivitySafe(intent)
}

fun AppRouter.showErrorDialog(error: Throwable, url: String? = null) {
    ErrorDetailsDialog().withArgs(2) {
        putSerializable(KEY_ERROR, error)
        putString(KEY_URL, url)
    }.show()
}
