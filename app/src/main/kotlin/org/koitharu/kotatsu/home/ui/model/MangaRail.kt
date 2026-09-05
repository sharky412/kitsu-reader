package org.koitharu.kotatsu.home.ui.model

import androidx.annotation.StringRes
import org.koitharu.kotatsu.list.ui.model.ListModel
import org.koitharu.kotatsu.list.ui.model.MangaCompactListModel

/**
 * A horizontal row of manga on the home screen.
 *
 * Rails are deliberately not carousels: nothing auto-advances and there is no page indicator. A
 * reader opens the app to resume something specific, and a moving target makes that harder.
 *
 * A rail renders only when it has content. An empty rail is not shown as an empty state, because on
 * home that would fill the first screen with explanations of things the reader has not done yet.
 */
data class MangaRail(
	val destination: MangaRailDestination,
	@StringRes val titleResId: Int,
	@StringRes val actionResId: Int,
	val items: List<MangaCompactListModel>,
) : ListModel {

	override fun areItemsTheSame(other: ListModel): Boolean {
		return other is MangaRail && other.destination == destination
	}

	override fun getChangePayload(previousState: ListModel): Any? {
		return if (previousState is MangaRail && previousState.items != items) {
			PAYLOAD_ITEMS
		} else {
			super.getChangePayload(previousState)
		}
	}

	companion object {

		const val PAYLOAD_ITEMS = "rail_items"
	}
}
