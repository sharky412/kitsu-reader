package org.koitharu.kotatsu.core.ui

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

/**
 * One choice in a [SegmentedContainerFragment].
 *
 * [key] identifies the segment in saved state and as the child fragment's tag, so it has to stay
 * stable across releases but never reaches the reader. Enum constant names make good keys.
 */
data class ContentSegment(
	val key: String,
	@StringRes val title: Int,
	val fragmentClass: Class<out Fragment>,
)
