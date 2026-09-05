package org.koitharu.kotatsu.core.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.nav.AppRouter
import org.koitharu.kotatsu.core.ui.util.RecyclerViewOwner
import org.koitharu.kotatsu.core.util.ext.buildBundle
import org.koitharu.kotatsu.core.util.ext.consumeSystemBarsInsets
import org.koitharu.kotatsu.databinding.FragmentSegmentedBinding
import org.koitharu.kotatsu.main.ui.owners.FadingAppbarOwner

/**
 * A destination that gathers several existing screens behind a segmented control.
 *
 * Each segment shows its screen unchanged, as a child fragment. Segments are added the first time
 * they are asked for and hidden afterwards rather than replaced, so a reader who looks at one and
 * comes back finds the other exactly as they left it — same scroll position, same inner tab, same
 * selection. That costs keeping each visited segment in memory, which is the trade this makes
 * deliberately: they are all lists backed by the same database, and losing a reader's place is more
 * expensive than holding a fragment.
 */
abstract class SegmentedContainerFragment :
	BaseFragment<FragmentSegmentedBinding>(),
	FadingAppbarOwner,
	RecyclerViewOwner,
	MaterialButtonToggleGroup.OnButtonCheckedListener {

	/** The segments to offer, in the order they are shown. Must not be empty. */
	protected abstract val segments: List<ContentSegment>

	private var selected: ContentSegment? = null

	/**
	 * Scroll-to-top on tab reselection, and the fast scroller, both act on whichever list is
	 * currently showing rather than on this fragment.
	 */
	override val recyclerView: RecyclerView?
		get() = (currentFragment() as? RecyclerViewOwner)?.recyclerView

	override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
		FragmentSegmentedBinding.inflate(inflater, container, false)

	override fun onViewBindingCreated(binding: FragmentSegmentedBinding, savedInstanceState: Bundle?) {
		super.onViewBindingCreated(binding, savedInstanceState)
		val available = segments
		check(available.isNotEmpty()) { "${javaClass.simpleName} declares no segments" }
		val restored = savedInstanceState?.getString(KEY_SEGMENT)
		val target = available.find { it.key == restored } ?: available.first()
		selected = target
		createButtons(binding.groupSegments, available, target)
		// A restored fragment manager has already recreated the child, so committing again would
		// replace a live screen with a fresh one and throw away what was restored.
		if (currentFragment() == null) {
			showSegment(target)
		}
		binding.groupSegments.addOnButtonCheckedListener(this)
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		selected?.let { outState.putString(KEY_SEGMENT, it.key) }
	}

	override fun onButtonChecked(
		group: MaterialButtonToggleGroup?,
		checkedId: Int,
		isChecked: Boolean,
	) {
		if (!isChecked) {
			return
		}
		val checked = segments.getOrNull(group?.findViewById<View>(checkedId)?.tag as? Int ?: return)
		if (checked != null && checked != selected) {
			selected = checked
			showSegment(checked)
		}
	}

	override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
		// The segment row is the only part of this screen outside the child fragment, so it is the
		// only part that needs the horizontal cutout padding applied here.
		val barsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
		requireViewBinding().scrollViewSegments.updatePadding(
			left = barsInsets.left,
			right = barsInsets.right,
		)
		// The status bar is already handled by the activity's app bar, and the segment row sits below
		// both. A segment that pads itself by the top inset — as the bookmarks list does — would
		// otherwise open with a status bar's worth of empty space under the segments.
		return insets.consumeSystemBarsInsets(top = true)
	}

	private fun currentFragment() = selected?.let { childFragmentManager.findFragmentByTag(it.key) }

	private fun createButtons(
		group: MaterialButtonToggleGroup,
		available: List<ContentSegment>,
		target: ContentSegment,
	) {
		val inflater = LayoutInflater.from(group.context)
		available.forEachIndexed { index, segment ->
			val button = inflater.inflate(R.layout.item_segment_button, group, false) as MaterialButton
			// Ids are generated, so they cannot be trusted across a configuration change. The
			// position is what identifies the segment when the group reports a check.
			button.id = View.generateViewId()
			button.tag = index
			button.setText(segment.title)
			group.addView(button)
		}
		group.check(group.getChildAt(available.indexOf(target)).id)
	}

	private fun showSegment(target: ContentSegment) {
		childFragmentManager.commit {
			setReorderingAllowed(true)
			for (segment in segments) {
				val existing = childFragmentManager.findFragmentByTag(segment.key)
				when {
					segment != target -> existing?.let { hide(it) }
					existing != null -> show(existing)
					// Segments carry the flag the bottom bar sets on a primary destination: from the
					// reader's point of view this still is one, and `LocalListViewModel` uses it to
					// decide whether its filter appears inline.
					else -> add(
						R.id.container_segment,
						segment.fragmentClass,
						buildBundle(1) {
							putBoolean(AppRouter.KEY_IS_BOTTOMTAB, true)
						},
						segment.key,
					)
				}
			}
		}
	}

	private companion object {

		const val KEY_SEGMENT = "content_segment"
	}
}
