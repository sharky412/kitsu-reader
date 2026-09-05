package org.koitharu.kotatsu.main.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.koitharu.kotatsu.core.prefs.NavItem

/**
 * Guards the bottom-navigation mapping.
 *
 * Before [MainNavDestinations] existed, an unmapped entry did not fail loudly: selecting the tab
 * returned `false` and the fragment-to-id lookup returned `0`, so the tab simply did nothing. These
 * tests make an incomplete mapping a red build instead.
 */
class MainNavDestinationsTest {

	@Test
	fun `every navigation entry has a destination`() {
		for (item in NavItem.entries) {
			assertNotNull("NavItem.$item has no destination fragment", MainNavDestinations.fragmentClass(item))
		}
	}

	@Test
	fun `every navigation entry resolves from its menu id`() {
		for (item in NavItem.entries) {
			assertEquals("NavItem.$item does not resolve from its own id", item, MainNavDestinations.find(item.id))
		}
	}

	@Test
	fun `unknown menu id does not resolve`() {
		assertNull(MainNavDestinations.find(0))
	}

	@Test
	fun `destinations are distinct`() {
		val classes = NavItem.entries.map { MainNavDestinations.fragmentClass(it) }
		assertEquals("Two navigation entries share a fragment", classes.size, classes.toSet().size)
	}

	/**
	 * Fragment lookup matches by assignability, so a destination that is a subclass of another
	 * destination would resolve ambiguously depending on map order.
	 */
	@Test
	fun `no destination is a subclass of another`() {
		val classes = NavItem.entries.map { MainNavDestinations.fragmentClass(it) }
		for (a in classes) {
			for (b in classes) {
				if (a !== b && a.isAssignableFrom(b)) {
					throw AssertionError("${b.simpleName} extends ${a.simpleName}; navigation lookup is ambiguous")
				}
			}
		}
	}
}
