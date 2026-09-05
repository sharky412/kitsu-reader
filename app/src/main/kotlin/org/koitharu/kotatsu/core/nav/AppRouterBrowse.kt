/*
 * Finding something to read: source lists, search, tag and author entry points.
 *
 * These were members of AppRouter, which had grown to 862 lines covering every screen in the app.
 * They are extensions rather than members so that each area owns its own navigation in its own
 * file. Call sites read the same as before — `router.openDetails(manga)` — but now need an import
 * for the function they use.
 */
package org.koitharu.kotatsu.core.nav

import android.content.Intent
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.model.getTitle
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_EXCLUDE
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_KIND
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_QUERY
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.listIntent
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.manageSourcesIntent
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.sourceAuthIntent
import org.koitharu.kotatsu.core.ui.dialog.buildAlertDialog
import org.koitharu.kotatsu.core.util.ext.withArgs
import org.koitharu.kotatsu.filter.ui.FilterCoordinator
import org.koitharu.kotatsu.filter.ui.sheet.FilterSheetFragment
import org.koitharu.kotatsu.filter.ui.tags.TagsCatalogSheet
import org.koitharu.kotatsu.parsers.model.MangaListFilter
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.model.MangaTag
import org.koitharu.kotatsu.parsers.model.SortOrder
import org.koitharu.kotatsu.search.domain.SearchKind
import org.koitharu.kotatsu.search.ui.multi.SearchActivity
import org.koitharu.kotatsu.settings.sources.catalog.SourcesCatalogActivity

fun AppRouter.openList(source: MangaSource, filter: MangaListFilter?, sortOrder: SortOrder?) {
    startActivity(listIntent(contextOrNull() ?: return, source, filter, sortOrder))
}

fun AppRouter.openList(tag: MangaTag) = openList(tag.source, MangaListFilter(tags = setOf(tag)), null)

fun AppRouter.openSearch(query: String, kind: SearchKind = SearchKind.SIMPLE) {
    startActivity(
        Intent(contextOrNull() ?: return, SearchActivity::class.java)
            .putExtra(KEY_QUERY, query)
            .putExtra(KEY_KIND, kind),
    )
}

fun AppRouter.openSearch(source: MangaSource, query: String) = openList(source, MangaListFilter(query = query), null)

fun AppRouter.openSourcesCatalog() = startActivity(SourcesCatalogActivity::class.java)

fun AppRouter.openSourceAuth(source: MangaSource) {
    startActivity(sourceAuthIntent(contextOrNull() ?: return, source))
}

fun AppRouter.openManageSources() {
    startActivity(
        manageSourcesIntent(contextOrNull() ?: return),
    )
}

fun AppRouter.showTagDialog(tag: MangaTag) {
    buildAlertDialog(contextOrNull() ?: return) {
        setIcon(R.drawable.ic_tag)
        setTitle(tag.title)
        setItems(
            arrayOf(
                context.getString(R.string.search_on_s, tag.source.getTitle(context)),
                context.getString(R.string.search_everywhere),
            ),
        ) { _, which ->
            when (which) {
                0 -> openList(tag)
                1 -> openSearch(tag.title, SearchKind.TAG)
            }
        }
        setNegativeButton(R.string.close, null)
        setCancelable(true)
    }.show()
}

fun AppRouter.showAuthorDialog(author: String, source: MangaSource) {
    buildAlertDialog(contextOrNull() ?: return) {
        setIcon(R.drawable.ic_user)
        setTitle(author)
        setItems(
            arrayOf(
                context.getString(R.string.search_on_s, source.getTitle(context)),
                context.getString(R.string.search_everywhere),
            ),
        ) { _, which ->
            when (which) {
                0 -> openList(source, MangaListFilter(author = author), null)
                1 -> openSearch(author, SearchKind.AUTHOR)
            }
        }
        setNegativeButton(R.string.close, null)
        setCancelable(true)
    }.show()
}

fun AppRouter.showFilterSheet(): Boolean = if (isFilterSupported()) {
    FilterSheetFragment().showDistinct()
} else {
    false
}

fun AppRouter.showTagsCatalogSheet(excludeMode: Boolean) {
    if (!isFilterSupported()) {
        return
    }
    TagsCatalogSheet().withArgs(1) {
        putBoolean(KEY_EXCLUDE, excludeMode)
    }.showDistinct()
}

fun AppRouter.isFilterSupported(): Boolean = when {
    fragment != null -> FilterCoordinator.find(fragment) != null
    activity != null -> activity is FilterCoordinator.Owner
    else -> false
}
