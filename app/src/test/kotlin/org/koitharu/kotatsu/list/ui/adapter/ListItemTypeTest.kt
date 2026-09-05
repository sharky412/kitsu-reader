package org.koitharu.kotatsu.list.ui.adapter

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pins the ordinal of every [ListItemType].
 *
 * `BaseListAdapter.addDelegate` registers each adapter delegate under `type.ordinal`, so the
 * ordinal *is* the RecyclerView view type. Inserting or reordering a constant silently shifts every
 * later view type: delegates then receive holders created by a different delegate, which surfaces
 * as wrong-looking rows or a `ClassCastException` deep inside `AdapterDelegatesManager` rather than
 * as a compile error.
 *
 * Appending a new constant at the end of the enum is safe and does not require touching this test.
 * Inserting, reordering or removing one is a breaking change.
 */
class ListItemTypeTest {

	@Test
	fun `view type ordinals are stable`() {
		val expected = listOf(
			"QUICK_FILTER",
			"FILTER_SORT",
			"FILTER_TAG",
			"FILTER_TAG_MULTI",
			"FILTER_STATE",
			"FILTER_LANGUAGE",
			"HEADER",
			"MANGA_LIST",
			"MANGA_LIST_DETAILED",
			"MANGA_GRID",
			"MANGA_NESTED_GROUP",
			"FOOTER_LOADING",
			"FOOTER_ERROR",
			"FOOTER_BUTTON",
			"STATE_LOADING",
			"STATE_ERROR",
			"STATE_EMPTY",
			"EXPLORE_BUTTONS",
			"EXPLORE_SOURCE_GRID",
			"EXPLORE_SOURCE_LIST",
			"EXPLORE_SUGGESTION",
			"TIP",
			"INFO",
			"HINT_EMPTY",
			"PAGE_THUMB",
			"FEED",
			"DOWNLOAD",
			"CATEGORY_LARGE",
			"MANGA_SCROBBLING",
			"NAV_ITEM",
			"CHAPTER_LIST",
			"CHAPTER_GRID",
		)
		val actual = ListItemType.entries.map { it.name }
		assertEquals(
			"ListItemType ordinals are RecyclerView view types and must not shift. " +
				"New types may only be appended after the last pinned entry.",
			expected,
			actual.take(expected.size),
		)
	}

	@Test
	fun `ordinals match declaration order`() {
		ListItemType.entries.forEachIndexed { index, type ->
			assertEquals("Unexpected ordinal for $type", index, type.ordinal)
		}
	}
}
