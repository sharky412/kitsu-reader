package org.koitharu.kotatsu.updates.ui

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.ui.ContentSegment
import org.koitharu.kotatsu.tracker.ui.feed.FeedFragment
import org.koitharu.kotatsu.tracker.ui.updates.UpdatesFragment

/**
 * The two views of what the tracker found, gathered under one destination.
 *
 * Both answer "what is new", from the same tracking data: [UpdatesFragment] lists the manga that
 * have unread chapters, [FeedFragment] lists the individual chapters as they arrived. They were two
 * neighbouring entries in the bottom bar, which asked the reader to know the difference before they
 * could look.
 *
 * The constant name is the segment key, which is written to the fragment's saved state.
 */
enum class UpdatesSegment(
	@StringRes val title: Int,
	val fragmentClass: Class<out Fragment>,
) {

	NEW_CHAPTERS(R.string.new_chapters, UpdatesFragment::class.java),
	FEED(R.string.feed, FeedFragment::class.java),
	;

	fun toContentSegment() = ContentSegment(name, title, fragmentClass)
}
