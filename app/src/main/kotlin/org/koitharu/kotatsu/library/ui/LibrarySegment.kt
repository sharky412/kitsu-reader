package org.koitharu.kotatsu.library.ui

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.bookmarks.ui.AllBookmarksFragment
import org.koitharu.kotatsu.core.ui.ContentSegment
import org.koitharu.kotatsu.favourites.ui.container.FavouritesContainerFragment
import org.koitharu.kotatsu.local.ui.LocalListFragment

/**
 * The parts of a reader's own collection, gathered under one destination.
 *
 * Saved manga, downloaded files and bookmarked pages are all things the reader put there
 * deliberately, and each previously occupied a slot in a bottom bar that holds six. Grouping them
 * frees those slots without taking anything away: every segment is still reachable as its own
 * destination for anyone who prefers it that way.
 *
 * The constant name is the segment key, which is written to the fragment's saved state. Renaming one
 * loses the reader's last segment across a process death; nothing else depends on it.
 */
enum class LibrarySegment(
	@StringRes val title: Int,
	val fragmentClass: Class<out Fragment>,
) {

	SAVED(R.string.favourites, FavouritesContainerFragment::class.java),
	ON_DEVICE(R.string.on_device, LocalListFragment::class.java),
	BOOKMARKS(R.string.bookmarks, AllBookmarksFragment::class.java),
	;

	fun toContentSegment() = ContentSegment(name, title, fragmentClass)
}
