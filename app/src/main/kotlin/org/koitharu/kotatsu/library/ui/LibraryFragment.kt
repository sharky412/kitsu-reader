package org.koitharu.kotatsu.library.ui

import dagger.hilt.android.AndroidEntryPoint
import org.koitharu.kotatsu.core.ui.SegmentedContainerFragment

/**
 * The reader's own collection: saved manga, downloaded files and bookmarked pages.
 *
 * Nothing about how favourites, local storage or bookmarks work is reimplemented here. This
 * destination only decides which of those screens is on show; see [LibrarySegment].
 */
@AndroidEntryPoint
class LibraryFragment : SegmentedContainerFragment() {

	override val segments = LibrarySegment.entries.map { it.toContentSegment() }
}
