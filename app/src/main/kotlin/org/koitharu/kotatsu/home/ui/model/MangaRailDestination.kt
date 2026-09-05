package org.koitharu.kotatsu.home.ui.model

/**
 * Where a rail's action leads.
 *
 * Naming the destination rather than matching on the rail's title keeps the fragment from depending
 * on which string a rail happens to be labelled with, and makes it obvious that every rail has
 * somewhere to go.
 */
enum class MangaRailDestination {

	HISTORY,
	UPDATES,
}
