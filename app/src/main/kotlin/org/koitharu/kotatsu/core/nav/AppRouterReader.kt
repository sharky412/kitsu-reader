/*
 * The reader and the screens that configure how a page is displayed.
 *
 * These were members of AppRouter, which had grown to 862 lines covering every screen in the app.
 * They are extensions rather than members so that each area owns its own navigation in its own
 * file. Call sites read the same as before — `router.openDetails(manga)` — but now need an import
 * for the function they use.
 */
package org.koitharu.kotatsu.core.nav

import android.content.Intent
import android.view.View
import org.koitharu.kotatsu.core.model.parcelable.ParcelableManga
import org.koitharu.kotatsu.core.model.parcelable.ParcelableMangaPage
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_MANGA
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_PAGES
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_READER_MODE
import org.koitharu.kotatsu.core.prefs.ReaderMode
import org.koitharu.kotatsu.core.util.ext.withArgs
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaPage
import org.koitharu.kotatsu.reader.ui.colorfilter.ColorFilterConfigActivity
import org.koitharu.kotatsu.reader.ui.config.ReaderConfigSheet

fun AppRouter.openReader(manga: Manga, anchor: View? = null) {
    openReader(
        ReaderIntent.Builder(contextOrNull() ?: return)
            .manga(manga)
            .build(),
        anchor,
    )
}

fun AppRouter.openReader(intent: ReaderIntent, anchor: View? = null) {
    val activityIntent = intent.intent
    if (settings.isReaderMultiTaskEnabled && activityIntent.data != null) {
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
    }
    startActivity(activityIntent, anchor?.let { view -> scaleUpActivityOptionsOf(view) })
}

fun AppRouter.openColorFilterConfig(manga: Manga, page: MangaPage) {
    startActivity(
        Intent(contextOrNull(), ColorFilterConfigActivity::class.java)
            .putExtra(KEY_MANGA, ParcelableManga(manga))
            .putExtra(KEY_PAGES, ParcelableMangaPage(page)),
    )
}

fun AppRouter.showReaderConfigSheet(mode: ReaderMode) {
    ReaderConfigSheet().withArgs(1) {
        putInt(KEY_READER_MODE, mode.id)
    }.showDistinct()
}
