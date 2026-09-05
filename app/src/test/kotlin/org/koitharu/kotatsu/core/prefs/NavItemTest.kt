package org.koitharu.kotatsu.core.prefs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koitharu.kotatsu.main.ui.MainNavigationDelegate

/**
 * Guards the bottom-navigation contract.
 *
 * `MainNavigationDelegate` builds the navigation menu from `AppSettings.mainNavItems` using
 * [NavItem.id] as the menu item id, and maps that id back to a fragment class in a hand-written
 * `when` block. A duplicated or zero id makes `Menu.findItem` return the wrong entry and makes tab
 * selection fail silently, with no compile error and no crash.
 *
 * The selected items are persisted by [NavItem.name]; that side of the contract is covered by
 * [PersistedEnumsTest].
 */
class NavItemTest {

	@Test
	fun `menu item ids are unique`() {
		val ids = NavItem.entries.map { it.id }
		assertEquals("Duplicate NavItem ids", ids.size, ids.toSet().size)
	}

	@Test
	fun `menu item ids are resolvable`() {
		for (item in NavItem.entries) {
			assertNotEquals("NavItem.$item has an unresolved id", 0, item.id)
		}
	}

	@Test
	fun `every item has a title and an icon`() {
		for (item in NavItem.entries) {
			assertNotEquals("NavItem.$item has no title", 0, item.title)
			assertNotEquals("NavItem.$item has no icon", 0, item.icon)
		}
	}

	/**
	 * `MainNavigationDelegate.createMenu` stops adding entries once the navigation bar is full, so
	 * declaring fewer items than the bar can show would leave the bar permanently under-filled.
	 */
	@Test
	fun `enough items exist to fill the navigation bar`() {
		assertTrue(
			"NavItem must offer at least MAX_ITEM_COUNT choices",
			NavItem.entries.size >= MainNavigationDelegate.MAX_ITEM_COUNT,
		)
	}
}
