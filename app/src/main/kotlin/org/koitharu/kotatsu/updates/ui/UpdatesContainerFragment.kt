package org.koitharu.kotatsu.updates.ui

import dagger.hilt.android.AndroidEntryPoint
import org.koitharu.kotatsu.core.ui.SegmentedContainerFragment

/**
 * Everything the tracker has found: manga with unread chapters, and the chapter feed.
 *
 * Neither screen is reimplemented here; this destination only decides which is on show. See
 * [UpdatesSegment] for why the two belong together.
 */
@AndroidEntryPoint
class UpdatesContainerFragment : SegmentedContainerFragment() {

	override val segments = UpdatesSegment.entries.map { it.toContentSegment() }
}
