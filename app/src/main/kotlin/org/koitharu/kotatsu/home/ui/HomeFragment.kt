package org.koitharu.kotatsu.home.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import org.koitharu.kotatsu.core.exceptions.resolve.SnackbarErrorObserver
import org.koitharu.kotatsu.core.nav.openDetails
import org.koitharu.kotatsu.core.nav.openHistory
import org.koitharu.kotatsu.core.nav.openMangaUpdates
import org.koitharu.kotatsu.core.nav.openSourcesCatalog
import org.koitharu.kotatsu.core.nav.router
import org.koitharu.kotatsu.core.prefs.NavItem
import org.koitharu.kotatsu.core.ui.BaseFragment
import org.koitharu.kotatsu.core.ui.BaseListAdapter
import org.koitharu.kotatsu.core.ui.list.OnListItemClickListener
import org.koitharu.kotatsu.core.ui.util.RecyclerViewOwner
import org.koitharu.kotatsu.core.util.ext.observe
import org.koitharu.kotatsu.core.util.ext.observeEvent
import org.koitharu.kotatsu.databinding.FragmentHomeBinding
import org.koitharu.kotatsu.home.ui.adapter.mangaRailAD
import org.koitharu.kotatsu.home.ui.model.MangaRail
import org.koitharu.kotatsu.home.ui.model.MangaRailDestination
import org.koitharu.kotatsu.list.ui.adapter.ListItemType
import org.koitharu.kotatsu.list.ui.adapter.ListStateHolderListener
import org.koitharu.kotatsu.list.ui.adapter.emptyStateListAD
import org.koitharu.kotatsu.list.ui.model.ListModel
import org.koitharu.kotatsu.main.ui.MainActivity
import org.koitharu.kotatsu.parsers.model.Manga

/**
 * The home screen: rails of manga, resume first.
 *
 * Home owns no data of its own. Every rail is a projection of an existing repository, so nothing
 * here needs to be kept in sync with the library, history or tracker.
 */
@AndroidEntryPoint
class HomeFragment :
	BaseFragment<FragmentHomeBinding>(),
	RecyclerViewOwner,
	ListStateHolderListener,
	OnListItemClickListener<Manga> {

	private val viewModel by viewModels<HomeViewModel>()
	private var adapter: BaseListAdapter<ListModel>? = null

	override val recyclerView: RecyclerView
		get() = requireViewBinding().recyclerView

	override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
		FragmentHomeBinding.inflate(inflater, container, false)

	override fun onViewBindingCreated(binding: FragmentHomeBinding, savedInstanceState: Bundle?) {
		super.onViewBindingCreated(binding, savedInstanceState)
		val listAdapter = BaseListAdapter<ListModel>()
			.addDelegate(ListItemType.MANGA_RAIL, mangaRailAD(this, ::onRailActionClick))
			.addDelegate(ListItemType.STATE_EMPTY, emptyStateListAD(this))
		adapter = listAdapter
		binding.recyclerView.adapter = listAdapter
		viewModel.content.observe(viewLifecycleOwner) { listAdapter.items = it }
		viewModel.onError.observeEvent(viewLifecycleOwner, SnackbarErrorObserver(binding.recyclerView, this))
		viewModel.onOpenManga.observeEvent(viewLifecycleOwner) { manga ->
			router.openDetails(manga)
		}
	}

	override fun onDestroyView() {
		adapter = null
		super.onDestroyView()
	}

	override fun onItemClick(item: Manga, view: View) {
		router.openDetails(item)
	}

	/** The first-run state offers the one action that makes home fill up. */
	override fun onEmptyActionClick() {
		router.openSourcesCatalog()
	}

	override fun onRetryClick(error: Throwable) = Unit

	override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
		val barsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
		requireViewBinding().recyclerView.updatePadding(
			left = barsInsets.left,
			right = barsInsets.right,
			bottom = barsInsets.bottom,
		)
		return insets
	}

	/**
	 * History is also a bottom-bar destination for most readers, so its rail action switches tabs
	 * rather than stacking a second copy of the same screen on top of this one. Readers who removed
	 * that tab still get the standalone screen. Updates has no tab, so it always opens as a screen.
	 */
	private fun onRailActionClick(rail: MangaRail, view: View) {
		when (rail.destination) {
			MangaRailDestination.HISTORY -> {
				if ((activity as? MainActivity)?.trySelectNavItem(NavItem.HISTORY) != true) {
					router.openHistory()
				}
			}

			MangaRailDestination.UPDATES -> router.openMangaUpdates()
		}
	}
}
