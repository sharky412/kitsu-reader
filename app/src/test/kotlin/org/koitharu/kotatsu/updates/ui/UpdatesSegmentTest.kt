package org.koitharu.kotatsu.updates.ui

import androidx.fragment.app.Fragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A segment's key is written to saved state and used as the child fragment's tag. A duplicate key
 * would make two segments share one child, and a changed key drops the reader back on the default
 * segment after a process death.
 */
class UpdatesSegmentTest {

	private val segments = UpdatesSegment.entries.map { it.toContentSegment() }

	@Test
	fun `keys are the constant names and are distinct`() {
		assertEquals(UpdatesSegment.entries.map { it.name }, segments.map { it.key })
		assertEquals(segments.size, segments.mapTo(HashSet()) { it.key }.size)
	}

	@Test
	fun `segment keys are stable`() {
		assertEquals(listOf("NEW_CHAPTERS", "FEED"), segments.map { it.key })
	}

	@Test
	fun `every segment has a title and a distinct fragment`() {
		val fragments = segments.map { it.fragmentClass }

		assertEquals(fragments.size, fragments.toSet().size)
		for (segment in segments) {
			assertNotEquals(0, segment.title)
			assertTrue(Fragment::class.java.isAssignableFrom(segment.fragmentClass))
		}
	}
}
