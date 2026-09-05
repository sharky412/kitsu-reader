package org.koitharu.kotatsu.main.ui

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import org.koitharu.kotatsu.bookmarks.ui.AllBookmarksFragment
import org.koitharu.kotatsu.core.prefs.NavItem
import org.koitharu.kotatsu.explore.ui.ExploreFragment
import org.koitharu.kotatsu.favourites.ui.container.FavouritesContainerFragment
import org.koitharu.kotatsu.history.ui.HistoryListFragment
import org.koitharu.kotatsu.home.ui.HomeFragment
import org.koitharu.kotatsu.library.ui.LibraryFragment
import org.koitharu.kotatsu.local.ui.LocalListFragment
import org.koitharu.kotatsu.suggestions.ui.SuggestionsFragment
import org.koitharu.kotatsu.tracker.ui.feed.FeedFragment
import org.koitharu.kotatsu.tracker.ui.updates.UpdatesFragment
import org.koitharu.kotatsu.updates.ui.UpdatesContainerFragment
import java.util.EnumMap

/**
 * The single mapping between a bottom-navigation entry and the fragment it shows.
 *
 * This used to live in two hand-written `when` blocks inside [MainNavigationDelegate], one per
 * direction. They could disagree, and both fell through silently: an unmapped id returned `false`
 * from selection and an unmapped fragment reported id `0`, so a mistake surfaced as a tab that
 * quietly refused to switch rather than as a compile error or a crash.
 *
 * Keeping the mapping in one place means the two directions cannot drift, and
 * `MainNavDestinationsTest` asserts that every [NavItem] resolves.
 */
internal object MainNavDestinations {

	private val fragments: Map<NavItem, Class<out Fragment>> =
		EnumMap<NavItem, Class<out Fragment>>(NavItem::class.java).apply {
			put(NavItem.HOME, HomeFragment::class.java)
			put(NavItem.LIBRARY, LibraryFragment::class.java)
			put(NavItem.UPDATES, UpdatesContainerFragment::class.java)
			put(NavItem.HISTORY, HistoryListFragment::class.java)
			put(NavItem.FAVORITES, FavouritesContainerFragment::class.java)
			put(NavItem.LOCAL, LocalListFragment::class.java)
			put(NavItem.EXPLORE, ExploreFragment::class.java)
			put(NavItem.SUGGESTIONS, SuggestionsFragment::class.java)
			put(NavItem.FEED, FeedFragment::class.java)
			put(NavItem.UPDATED, UpdatesFragment::class.java)
			put(NavItem.BOOKMARKS, AllBookmarksFragment::class.java)
		}

	/** Every navigation entry, in the order they are declared. */
	val items: List<NavItem>
		get() = NavItem.entries

	fun fragmentClass(item: NavItem): Class<out Fragment> = checkNotNull(fragments[item]) {
		"No destination registered for NavItem.$item"
	}

	fun find(@IdRes itemId: Int): NavItem? = NavItem.entries.find { it.id == itemId }

	/**
	 * Resolves the entry a fragment belongs to.
	 *
	 * Matching is by assignability rather than exact class so that a subclassed destination still
	 * resolves, which is what the previous `is` checks did.
	 */
	fun find(fragment: Fragment): NavItem? = fragments.entries
		.firstOrNull { (_, cls) -> cls.isInstance(fragment) }
		?.key
}
