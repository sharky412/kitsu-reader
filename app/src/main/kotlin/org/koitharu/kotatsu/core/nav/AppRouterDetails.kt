/*
 * One manga: its details screen and everything reachable from there.
 *
 * These were members of AppRouter, which had grown to 862 lines covering every screen in the app.
 * They are extensions rather than members so that each area owns its own navigation in its own
 * file. Call sites read the same as before — `router.openDetails(manga)` — but now need an import
 * for the function they use.
 */
package org.koitharu.kotatsu.core.nav

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.appcompat.R as appcompatR
import androidx.core.net.toUri
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.alternatives.ui.AlternativesActivity
import org.koitharu.kotatsu.core.image.CoilMemoryCacheKey
import org.koitharu.kotatsu.core.model.appUrl
import org.koitharu.kotatsu.core.model.getTitle
import org.koitharu.kotatsu.core.model.isBroken
import org.koitharu.kotatsu.core.model.isLocal
import org.koitharu.kotatsu.core.model.parcelable.ParcelableManga
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_ID
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_INDEX
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_MANGA
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_PREVIEW
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_SOURCE
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_TAB
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.detailsIntent
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.overrideEditIntent
import org.koitharu.kotatsu.core.ui.dialog.buildAlertDialog
import org.koitharu.kotatsu.core.util.ext.getThemeDrawable
import org.koitharu.kotatsu.core.util.ext.toFileOrNull
import org.koitharu.kotatsu.core.util.ext.withArgs
import org.koitharu.kotatsu.details.ui.DetailsActivity
import org.koitharu.kotatsu.details.ui.pager.ChaptersPagesSheet
import org.koitharu.kotatsu.details.ui.related.RelatedMangaActivity
import org.koitharu.kotatsu.details.ui.scrobbling.ScrobblingInfoSheet
import org.koitharu.kotatsu.image.ui.ImageActivity
import org.koitharu.kotatsu.local.ui.info.LocalInfoDialog
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.scrobbling.common.domain.model.ScrobblerService
import org.koitharu.kotatsu.scrobbling.common.ui.selector.ScrobblingSelectorSheet
import org.koitharu.kotatsu.stats.ui.sheet.MangaStatsSheet

fun AppRouter.openDetails(manga: Manga) {
    startActivity(detailsIntent(contextOrNull() ?: return, manga))
}

fun AppRouter.openDetails(mangaId: Long) {
    startActivity(detailsIntent(contextOrNull() ?: return, mangaId))
}

fun AppRouter.openDetails(link: Uri) {
    startActivity(
        Intent(contextOrNull() ?: return, DetailsActivity::class.java)
            .setData(link),
    )
}

fun AppRouter.openAlternatives(manga: Manga) {
    startActivity(
        Intent(contextOrNull() ?: return, AlternativesActivity::class.java)
            .putExtra(KEY_MANGA, ParcelableManga(manga)),
    )
}

fun AppRouter.openRelated(manga: Manga) {
    startActivity(
        Intent(contextOrNull(), RelatedMangaActivity::class.java)
            .putExtra(KEY_MANGA, ParcelableManga(manga)),
    )
}

fun AppRouter.openImage(url: String, source: MangaSource?, anchor: View? = null, preview: CoilMemoryCacheKey? = null) {
    startActivity(
        Intent(contextOrNull(), ImageActivity::class.java)
            .setData(url.toUri())
            .putExtra(KEY_SOURCE, source?.name)
            .putExtra(KEY_PREVIEW, preview),
        anchor?.let { scaleUpActivityOptionsOf(it) },
    )
}

fun AppRouter.openMangaOverrideConfig(manga: Manga) {
    val intent = overrideEditIntent(contextOrNull() ?: return, manga)
    startActivity(intent)
}

fun AppRouter.showLocalInfoDialog(manga: Manga) {
    LocalInfoDialog().withArgs(1) {
        putParcelable(KEY_MANGA, ParcelableManga(manga))
    }.showDistinct()
}

fun AppRouter.showShareDialog(manga: Manga) {
    if (manga.isBroken) {
        return
    }
    if (manga.isLocal) {
        manga.url.toUri().toFileOrNull()?.let {
            shareFile(it)
        }
        return
    }
    buildAlertDialog(contextOrNull() ?: return) {
        setIcon(context.getThemeDrawable(appcompatR.attr.actionModeShareDrawable))
        setTitle(R.string.share)
        setItems(
            arrayOf(
                context.getString(R.string.link_to_manga_in_app),
                context.getString(R.string.link_to_manga_on_s, manga.source.getTitle(context)),
            ),
        ) { _, which ->
            val link = when (which) {
                0 -> manga.appUrl.toString()
                1 -> manga.publicUrl
                else -> return@setItems
            }
            shareLink(link, manga.title)
        }
        setNegativeButton(android.R.string.cancel, null)
        setCancelable(true)
    }.show()
}

fun AppRouter.showStatisticSheet(manga: Manga) {
    MangaStatsSheet().withArgs(1) {
        putParcelable(KEY_MANGA, ParcelableManga(manga))
    }.showDistinct()
}

fun AppRouter.showChapterPagesSheet() {
    ChaptersPagesSheet().showDistinct()
}

fun AppRouter.showChapterPagesSheet(defaultTab: Int) {
    ChaptersPagesSheet().withArgs(1) {
        putInt(KEY_TAB, defaultTab)
    }.showDistinct()
}

fun AppRouter.showScrobblingSelectorSheet(manga: Manga, scrobblerService: ScrobblerService?) {
    ScrobblingSelectorSheet().withArgs(2) {
        putParcelable(KEY_MANGA, ParcelableManga(manga))
        if (scrobblerService != null) {
            putInt(KEY_ID, scrobblerService.id)
        }
    }.show()
}

fun AppRouter.showScrobblingInfoSheet(index: Int) {
    ScrobblingInfoSheet().withArgs(1) {
        putInt(KEY_INDEX, index)
    }.showDistinct()
}

fun AppRouter.isChapterPagesSheetShown(): Boolean {
    val sheet = getFragmentManager()?.findFragmentByTag(fragmentTag<ChaptersPagesSheet>()) as? ChaptersPagesSheet
    return sheet?.dialog?.isShowing == true
}
