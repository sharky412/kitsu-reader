/*
 * The reader's own collection: favourites, history, bookmarks and tracked updates.
 *
 * These were members of AppRouter, which had grown to 862 lines covering every screen in the app.
 * They are extensions rather than members so that each area owns its own navigation in its own
 * file. Call sites read the same as before — `router.openDetails(manga)` — but now need an import
 * for the function they use.
 */
package org.koitharu.kotatsu.core.nav

import android.content.Intent
import org.koitharu.kotatsu.bookmarks.ui.AllBookmarksActivity
import org.koitharu.kotatsu.core.model.FavouriteCategory
import org.koitharu.kotatsu.core.model.parcelable.ParcelableManga
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_ID
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_MANGA_LIST
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.KEY_TITLE
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.mangaUpdatesIntent
import org.koitharu.kotatsu.core.nav.AppRouter.Companion.suggestionsIntent
import org.koitharu.kotatsu.core.util.ext.withArgs
import org.koitharu.kotatsu.favourites.ui.FavouritesActivity
import org.koitharu.kotatsu.favourites.ui.categories.FavouriteCategoriesActivity
import org.koitharu.kotatsu.favourites.ui.categories.edit.FavouritesCategoryEditActivity
import org.koitharu.kotatsu.favourites.ui.categories.select.FavoriteDialog
import org.koitharu.kotatsu.history.ui.HistoryActivity
import org.koitharu.kotatsu.parsers.model.Manga

fun AppRouter.openBookmarks() = startActivity(AllBookmarksActivity::class.java)

fun AppRouter.openSuggestions() {
    startActivity(suggestionsIntent(contextOrNull() ?: return))
}

fun AppRouter.openHistory() = startActivity(HistoryActivity::class.java)

fun AppRouter.openFavorites() = startActivity(FavouritesActivity::class.java)

fun AppRouter.openFavorites(category: FavouriteCategory) {
    startActivity(
        Intent(contextOrNull() ?: return, FavouritesActivity::class.java)
            .putExtra(KEY_ID, category.id)
            .putExtra(KEY_TITLE, category.title),
    )
}

fun AppRouter.openFavoriteCategories() = startActivity(FavouriteCategoriesActivity::class.java)

fun AppRouter.openFavoriteCategoryEdit(categoryId: Long) {
    startActivity(
        Intent(contextOrNull() ?: return, FavouritesCategoryEditActivity::class.java)
            .putExtra(KEY_ID, categoryId),
    )
}

fun AppRouter.openFavoriteCategoryCreate() = openFavoriteCategoryEdit(FavouritesCategoryEditActivity.NO_ID)

fun AppRouter.openMangaUpdates() {
    startActivity(mangaUpdatesIntent(contextOrNull() ?: return))
}

fun AppRouter.showFavoriteDialog(manga: Manga) = showFavoriteDialog(setOf(manga))

fun AppRouter.showFavoriteDialog(manga: Collection<Manga>) {
    if (manga.isEmpty()) {
        return
    }
    FavoriteDialog().withArgs(1) {
        putParcelableArrayList(
            KEY_MANGA_LIST,
            manga.mapTo(ArrayList(manga.size)) { ParcelableManga(it, withDescription = false) },
        )
    }.showDistinct()
}
