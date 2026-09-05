package org.koitharu.kotatsu.home.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.model.isNsfw
import org.koitharu.kotatsu.core.parser.MangaDataRepository
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.ui.BaseViewModel
import org.koitharu.kotatsu.core.util.ext.MutableEventFlow
import org.koitharu.kotatsu.history.data.HistoryRepository
import org.koitharu.kotatsu.home.ui.model.MangaRail
import org.koitharu.kotatsu.home.ui.model.MangaRailDestination
import org.koitharu.kotatsu.list.ui.model.EmptyState
import org.koitharu.kotatsu.list.ui.model.ListModel
import org.koitharu.kotatsu.list.ui.model.MangaCompactListModel
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.tracker.domain.TrackingRepository
import javax.inject.Inject

/**
 * Home answers one question first: what should I read now.
 *
 * Both rails are served from the database, so home paints without touching the network and works
 * offline. Network-backed rails (recommendations, popular) are additive and must never delay or
 * fail this first screen.
 *
 * A rail with no content is omitted rather than shown empty: on a first run that would fill the
 * screen with descriptions of things the reader has not done yet.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
	private val historyRepository: HistoryRepository,
	private val trackingRepository: TrackingRepository,
	private val dataRepository: MangaDataRepository,
	private val settings: AppSettings,
) : BaseViewModel() {

	val onOpenManga = MutableEventFlow<Manga>()

	val content: StateFlow<List<ListModel>> = combine(
		historyRepository.observeAll(RAIL_LIMIT),
		trackingRepository.observeUpdatedManga(RAIL_LIMIT, emptySet()),
	) { history, updated ->
		buildContent(
			continueReading = history.skipNsfwIfNeeded(),
			newChapters = updated.associate { it.manga to it.newChapters }
				.filterKeys { !settings.isNsfwContentDisabled || !it.isNsfw() },
		)
	}.withErrorHandling()
		.stateIn(viewModelScope + Dispatchers.Default, SharingStarted.Eagerly, emptyList())

	private suspend fun buildContent(
		continueReading: List<Manga>,
		newChapters: Map<Manga, Int>,
	): List<ListModel> = buildList(2) {
		if (continueReading.isEmpty() && newChapters.isEmpty()) {
			// A first run has no history and nothing tracked, so every rail is omitted. Rather than
			// leave the landing screen blank, say what home is for and point at the one action that
			// makes it fill up.
			add(
				EmptyState(
					icon = R.drawable.ic_empty_history,
					textPrimary = R.string.text_history_holder_primary,
					textSecondary = R.string.home_empty_secondary,
					actionStringRes = R.string.catalog,
				),
			)
			return@buildList
		}
		if (continueReading.isNotEmpty()) {
			add(
				MangaRail(
					destination = MangaRailDestination.HISTORY,
					titleResId = R.string.continue_reading,
					actionResId = R.string.history,
					items = continueReading.toRailItems(),
				),
			)
		}
		if (newChapters.isNotEmpty()) {
			add(
				MangaRail(
					destination = MangaRailDestination.UPDATES,
					titleResId = R.string.new_chapters,
					actionResId = R.string.updated,
					items = newChapters.keys.toList().toRailItems(newChapters),
				),
			)
		}
	}

	private suspend fun List<Manga>.toRailItems(
		counters: Map<Manga, Int> = emptyMap(),
	): List<MangaCompactListModel> {
		val overrides = dataRepository.getOverrides()
		return map { manga ->
			MangaCompactListModel(
				manga = manga,
				override = overrides[manga.id],
				subtitle = manga.tags.take(2).joinToString { it.title },
				counter = counters[manga] ?: 0,
			)
		}
	}

	private fun List<Manga>.skipNsfwIfNeeded() = if (settings.isNsfwContentDisabled) {
		filterNot { it.isNsfw() }
	} else {
		this
	}

	private companion object {

		const val RAIL_LIMIT = 12
	}
}
