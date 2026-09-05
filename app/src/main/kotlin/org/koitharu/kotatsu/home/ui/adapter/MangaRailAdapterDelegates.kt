package org.koitharu.kotatsu.home.ui.adapter

import androidx.core.view.isVisible
import org.koitharu.kotatsu.core.ui.BaseListAdapter
import org.koitharu.kotatsu.core.ui.list.OnListItemClickListener
import org.koitharu.kotatsu.core.util.ext.textAndVisible
import org.koitharu.kotatsu.databinding.ItemMangaRailBinding
import org.koitharu.kotatsu.databinding.ItemMangaRailCellBinding
import org.koitharu.kotatsu.home.ui.model.MangaRail
import org.koitharu.kotatsu.list.ui.adapter.ListItemType
import org.koitharu.kotatsu.list.ui.model.ListModel
import org.koitharu.kotatsu.list.ui.model.MangaCompactListModel
import org.koitharu.kotatsu.parsers.model.Manga
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding

fun mangaRailAD(
	itemClickListener: OnListItemClickListener<Manga>,
	actionClickListener: OnListItemClickListener<MangaRail>,
) = adapterDelegateViewBinding<MangaRail, ListModel, ItemMangaRailBinding>(
	{ layoutInflater, parent -> ItemMangaRailBinding.inflate(layoutInflater, parent, false) },
) {

	val adapter = BaseListAdapter<MangaCompactListModel>()
		.addDelegate(ListItemType.MANGA_RAIL_CELL, mangaRailCellAD(itemClickListener))
	binding.recyclerView.adapter = adapter
	// The rail lives inside a vertically scrolling list; letting it participate in nested scrolling
	// makes vertical drags that start on a cover fight the outer list.
	binding.recyclerView.isNestedScrollingEnabled = false

	binding.buttonMore.setOnClickListener { v ->
		actionClickListener.onItemClick(item, v)
	}

	bind {
		binding.textViewTitle.setText(item.titleResId)
		if (item.actionResId != 0) {
			binding.buttonMore.setText(item.actionResId)
			binding.buttonMore.isVisible = true
		} else {
			binding.buttonMore.isVisible = false
		}
		adapter.items = item.items
	}
}

fun mangaRailCellAD(
	itemClickListener: OnListItemClickListener<Manga>,
) = adapterDelegateViewBinding<MangaCompactListModel, MangaCompactListModel, ItemMangaRailCellBinding>(
	{ layoutInflater, parent -> ItemMangaRailCellBinding.inflate(layoutInflater, parent, false) },
) {

	binding.root.setOnClickListener { v ->
		itemClickListener.onItemClick(item.manga, v)
	}

	bind {
		binding.textViewTitle.text = item.title
		binding.textViewSubtitle.textAndVisible = item.subtitle
		binding.imageViewCover.setImageAsync(item.coverUrl, item.manga)
		// The new-chapters rail carries an unread count; the continue-reading rail passes zero.
		binding.badge.number = item.counter
		binding.badge.isVisible = item.counter > 0
	}
}
