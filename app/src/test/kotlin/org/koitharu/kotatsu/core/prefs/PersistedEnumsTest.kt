package org.koitharu.kotatsu.core.prefs

import org.junit.Assert.assertEquals
import org.junit.Test
import org.koitharu.kotatsu.core.model.ZoomMode
import org.koitharu.kotatsu.list.domain.ListSortOrder

/**
 * Pins the constant names of every enum that is persisted by name.
 *
 * `SharedPreferences.getEnumValue`/`putEnumValue` store `Enum.name`, and several enums are also
 * persisted by name in the database (for example the per-category sort order in
 * `favourite_categories`). Renaming a constant therefore silently discards the stored value and
 * falls back to the default: the user's theme, sort order, reader background or navigation layout
 * resets without any error.
 *
 * Two of the enums covered here already carry a `// Do not rename this` comment in their source;
 * this test turns that comment into a build failure.
 *
 * [SearchSuggestionType] is the sharpest case: `AppSettings.searchSuggestionTypes` resolves stored
 * names with `enumValueOf`, which throws rather than returning `null`, so a rename there crashes
 * instead of quietly resetting.
 *
 * Adding a constant is safe. Renaming or removing one requires a preference/database migration in
 * the same commit, plus an update to the pin below.
 */
class PersistedEnumsTest {

	@Test
	fun `ListMode names are stable`() = assertNames(
		ListMode.entries,
		"LIST", "DETAILED_LIST", "GRID",
	)

	@Test
	fun `ColorScheme names are stable`() = assertNames(
		ColorScheme.entries,
		"DEFAULT", "MONET", "EXPRESSIVE", "MIKU", "RENA", "FROG",
		"BLUEBERRY", "SAKURA", "MAMIMI", "KANADE", "ITSUKA",
	)

	@Test
	fun `NavItem names are stable`() = assertNames(
		NavItem.entries,
		"HOME", "LIBRARY", "UPDATES", "HISTORY", "FAVORITES", "LOCAL", "EXPLORE",
		"SUGGESTIONS", "FEED", "UPDATED", "BOOKMARKS",
	)

	@Test
	fun `ReaderMode names are stable`() = assertNames(
		ReaderMode.entries,
		"STANDARD", "REVERSED", "VERTICAL", "WEBTOON",
	)

	@Test
	fun `ReaderAnimation names are stable`() = assertNames(
		ReaderAnimation.entries,
		"NONE", "DEFAULT", "ADVANCED",
	)

	@Test
	fun `ReaderBackground names are stable`() = assertNames(
		ReaderBackground.entries,
		"DEFAULT", "LIGHT", "DARK", "WHITE", "BLACK",
	)

	@Test
	fun `ScreenshotsPolicy names are stable`() = assertNames(
		ScreenshotsPolicy.entries,
		"ALLOW", "BLOCK_NSFW", "BLOCK_INCOGNITO", "BLOCK_ALL",
	)

	@Test
	fun `ProgressIndicatorMode names are stable`() = assertNames(
		ProgressIndicatorMode.entries,
		"NONE", "PERCENT_READ", "PERCENT_LEFT", "CHAPTERS_READ", "CHAPTERS_LEFT",
	)

	@Test
	fun `TriStateOption names are stable`() = assertNames(
		TriStateOption.entries,
		"ENABLED", "ASK", "DISABLED",
	)

	@Test
	fun `ZoomMode names are stable`() = assertNames(
		ZoomMode.entries,
		"FIT_CENTER", "FIT_HEIGHT", "FIT_WIDTH", "KEEP_START",
	)

	@Test
	fun `DownloadFormat names are stable`() = assertNames(
		DownloadFormat.entries,
		"AUTOMATIC", "SINGLE_CBZ", "MULTIPLE_CBZ",
	)

	/**
	 * [NetworkPolicy] is the exception: it is persisted by a private integer key, not by name, and
	 * resolved through [NetworkPolicy.from]. Asserting through that entry point pins the real
	 * contract without exposing the field.
	 */
	@Test
	fun `NetworkPolicy keys are stable`() {
		assertEquals(NetworkPolicy.NEVER, NetworkPolicy.from("0", NetworkPolicy.ALWAYS))
		assertEquals(NetworkPolicy.ALWAYS, NetworkPolicy.from("1", NetworkPolicy.NEVER))
		assertEquals(NetworkPolicy.NON_METERED, NetworkPolicy.from("2", NetworkPolicy.NEVER))
		assertEquals(
			"An unknown key must fall back rather than throw",
			NetworkPolicy.NEVER,
			NetworkPolicy.from("nonsense", NetworkPolicy.NEVER),
		)
	}

	@Test
	fun `TrackerDownloadStrategy names are stable`() = assertNames(
		TrackerDownloadStrategy.entries,
		"DISABLED", "DOWNLOADED",
	)

	@Test
	fun `SearchSuggestionType names are stable`() = assertNames(
		SearchSuggestionType.entries,
		"GENRES", "QUERIES_RECENT", "QUERIES_SUGGEST", "MANGA",
		"SOURCES", "RECENT_SOURCES", "AUTHORS",
	)

	@Test
	fun `ListSortOrder names are stable`() = assertNames(
		ListSortOrder.entries,
		"NEWEST", "OLDEST", "PROGRESS", "UNREAD", "ALPHABETIC", "ALPHABETIC_REVERSE",
		"RATING", "RELEVANCE", "NEW_CHAPTERS", "LAST_READ", "LONG_AGO_READ", "UPDATED",
	)

	/**
	 * [ReaderMode.id] is written to the `mode` column of the `preferences` table by
	 * `MangaDataRepository.saveReaderMode` and read back by [ReaderMode.valueOf]. The ids are
	 * deliberately not sequential; changing one would remap every manga that has a per-title
	 * reader mode saved.
	 */
	@Test
	fun `ReaderMode ids are stable`() {
		assertEquals(1, ReaderMode.STANDARD.id)
		assertEquals(3, ReaderMode.REVERSED.id)
		assertEquals(4, ReaderMode.VERTICAL.id)
		assertEquals(2, ReaderMode.WEBTOON.id)
		val ids = ReaderMode.entries.map { it.id }
		assertEquals("ReaderMode ids must be unique", ids.size, ids.toSet().size)
	}

	/**
	 * Asserts that every pinned constant still exists. Order is irrelevant because persistence uses
	 * [Enum.name], and adding new constants is allowed; only renames and removals fail.
	 */
	private fun assertNames(actual: List<Enum<*>>, vararg expected: String) {
		val present = actual.mapTo(HashSet()) { it.name }
		val missing = expected.filterNot { it in present }
		assertEquals(
			"Persisted enum constants renamed or removed without a migration",
			emptyList<String>(),
			missing,
		)
	}
}
