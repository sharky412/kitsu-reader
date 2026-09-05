package org.koitharu.kotatsu.screenshot

import android.content.Context
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import com.github.takahirom.roborazzi.captureRoboImage
import com.google.android.material.imageview.ShapeableImageView
import org.junit.Test
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.ui.widgets.BadgeView
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf

/**
 * A 411dp-wide phone at xhdpi, the density the drawable set targets. Resource qualifiers must appear
 * in the order Android defines, so night mode sits between the screen dimensions and the density.
 *
 * These are top-level constants because `@Config` arguments on a subclass are resolved in file
 * scope, where the base class companion is not visible.
 */
internal const val QUALIFIERS_LIGHT = "w411dp-h891dp-notnight-xhdpi"
internal const val QUALIFIERS_DARK = "w411dp-h891dp-night-xhdpi"

/**
 * Visual baselines for the shared list components.
 *
 * The redesign replaces the theme token layer and the list item layouts. Those changes are wide and
 * mostly invisible to unit tests: a changed text appearance, a lost margin or an unreadable colour
 * in one theme only. Capturing the components in both themes turns that class of regression into a
 * reviewable image diff.
 *
 * Each theme runs as its own test class with its own `@Config` qualifiers, rather than switching
 * `RuntimeEnvironment.setQualifiers` inside one test method. Switching mid-method left Material
 * components reusing colour state lists resolved under the previous configuration, which rendered
 * the tonal button of a dark-mode capture with its light-mode container. A separate class per theme
 * gives each configuration a clean Robolectric sandbox.
 *
 * Components are inflated directly rather than through their activities, so these tests need no
 * dependency injection, no network and no database. Layouts containing `@AndroidEntryPoint` views
 * are still covered: see [inflate] for how the two cover views are stood in for.
 *
 * Record baselines with `./gradlew :app:recordRoborazziDebug`, check for regressions with
 * `./gradlew :app:verifyRoborazziDebug`, and produce diff images with
 * `./gradlew :app:compareRoborazziDebug`.
 */
abstract class ComponentScreenshotTestCase(private val variant: String) {

	@Test
	fun listHeader() = capture("list-header") { context ->
		inflate(context, R.layout.item_header).apply {
			findViewById<TextView>(R.id.textView_title).text = "Continue reading"
			findViewById<TextView>(R.id.button_more).apply {
				text = "See all"
				visibility = View.VISIBLE
			}
		}
	}

	@Test
	fun mangaRowCompact() = capture("manga-row-compact") { context ->
		inflate(context, R.layout.item_manga_list).apply {
			findViewById<TextView>(R.id.textView_title)?.text = "Frieren: Beyond Journey's End"
			findViewById<TextView>(R.id.textView_subtitle)?.text = "MangaDex · Ch. 112 · 68%"
		}
	}

	@Test
	fun mangaRowDetailed() = capture("manga-row-detailed") { context ->
		inflate(context, R.layout.item_manga_list_details).apply {
			findViewById<TextView>(R.id.textView_title)?.text = "Frieren: Beyond Journey's End"
			findViewById<TextView>(R.id.textView_author)?.text = "Kanehito Yamada"
			findViewById<TextView>(R.id.textView_tags)?.text = "Adventure, Drama, Fantasy"
		}
	}

	/**
	 * The rail cell sizes its cover from an aspect ratio implemented by `CoverImageView`, which the
	 * substitute does not reproduce, so the height is pinned here instead. Everything the baseline
	 * is meant to assert — cell width, title clamping, metadata treatment — is unaffected.
	 *
	 * Note what this pin cannot catch: it supplies a height the real view is supposed to compute, so
	 * a layout that leaves the cover with no height at all still renders correctly here. That is how
	 * a `0dp` cover height survived this baseline and only surfaced on a device.
	 */
	@Test
	fun mangaRailCell() = capture("manga-rail-cell") { context ->
		inflate(context, R.layout.item_manga_rail_cell).apply {
			findViewById<View>(R.id.imageView_cover)?.layoutParams?.height = COVER_HEIGHT_PX
			findViewById<TextView>(R.id.textView_title)?.text = "Frieren: Beyond Journey's End"
			findViewById<TextView>(R.id.textView_subtitle)?.text = "Ch. 112 · 68%"
			findViewById<BadgeView>(R.id.badge)?.apply {
				number = 12
				visibility = View.VISIBLE
			}
		}
	}

	/**
	 * The fourth way a manga is drawn in a list. Its cover carries the same overlays as the rail
	 * cell — an unread badge in one corner, reading progress in the other — so a change to either
	 * shows up in both baselines.
	 */
	@Test
	fun mangaGridCell() = capture("manga-grid-cell") { context ->
		inflate(context, R.layout.item_manga_grid).apply {
			// A grid cell is as wide as its column, which the layout manager decides at runtime. The
			// layout itself says match_parent, so without this the baseline would be a screen-wide
			// row and would say nothing about how the cell actually looks.
			layoutParams?.width = GRID_COLUMN_PX
			findViewById<View>(R.id.imageView_cover)?.layoutParams?.height = COVER_HEIGHT_PX
			findViewById<TextView>(R.id.textView_title)?.text = "Frieren: Beyond Journey's End"
			findViewById<BadgeView>(R.id.badge)?.apply {
				number = 3
				visibility = View.VISIBLE
			}
		}
	}

	@Test
	fun emptyState() = capture("state-empty") { context ->
		inflate(context, R.layout.item_empty_state).apply {
			findViewById<TextView>(R.id.textPrimary)?.text = "Nothing saved yet"
			findViewById<TextView>(R.id.textSecondary)?.text =
				"Manga you add to your library will appear here."
			findViewById<TextView>(R.id.button_retry)?.apply {
				text = "Browse sources"
				visibility = View.VISIBLE
			}
		}
	}

	@Test
	fun chapterRow() = capture("chapter-row") { context ->
		inflate(context, R.layout.item_chapter).apply {
			findViewById<TextView>(R.id.textView_title)?.text = "Chapter 12: The long way round"
			findViewById<TextView>(R.id.textView_description)?.text = "3 days ago"
		}
	}

	/**
	 * The same row without a date or scanlator, which is every chapter of a local manga. It exists as
	 * its own baseline because the row is meant to shrink to one line rather than pad itself out to
	 * the height of a two-line one.
	 */
	@Test
	fun chapterRowSingleLine() = capture("chapter-row-single-line") { context ->
		inflate(context, R.layout.item_chapter).apply {
			findViewById<TextView>(R.id.textView_title)?.text = "Chapter 12"
			findViewById<TextView>(R.id.textView_description)?.visibility = View.GONE
		}
	}

	@Test
	fun chapterGridCell() = capture("chapter-grid-cell") { context ->
		inflate(context, R.layout.item_chapter_grid).apply {
			findViewById<TextView>(R.id.textView_title)?.text = "12"
			findViewById<View>(R.id.imageView_new)?.visibility = View.VISIBLE
		}
	}

	@Test
	fun errorState() = capture("state-error") { context ->
		inflate(context, R.layout.item_error_state).apply {
			findViewById<TextView>(R.id.textView_error)?.text =
				"Could not reach this source. Check your connection and try again."
			findViewById<TextView>(R.id.button_retry)?.apply {
				text = "Retry"
				visibility = View.VISIBLE
			}
			findViewById<TextView>(R.id.button_secondary)?.apply {
				text = "Open in browser"
				visibility = View.VISIBLE
			}
		}
	}

	@Test
	fun tipCard() = capture("tip-card") { context ->
		inflate(context, R.layout.item_tip).apply {
			findViewById<TextView>(R.id.textView)?.text =
				"Recommendations are off. Turn them on to see manga picked from your reading history."
			findViewById<TextView>(R.id.button_close)?.text = "Not now"
		}
	}

	@Test
	fun listGroupHeader() = capture("list-group") { context ->
		inflate(context, R.layout.item_list_group).apply {
			findViewById<TextView>(R.id.textView_title)?.text = "MangaDex"
			findViewById<TextView>(R.id.button_more)?.apply {
				text = "More"
				visibility = View.VISIBLE
			}
			findViewById<View>(R.id.recyclerView)?.visibility = View.GONE
			findViewById<TextView>(R.id.textView_error)?.apply {
				text = "Nothing found"
				visibility = View.VISIBLE
			}
		}
	}

	/**
	 * Inflates a layout, substituting the two cover views for a plain [ShapeableImageView].
	 *
	 * `CoilImageView` and its subclass `CoverImageView` are annotated `@AndroidEntryPoint`, so
	 * inflating them requires a Hilt-injected activity. Providing that graph here is not viable:
	 * building Coil's `ImageLoader` pulls in `MangaPageFetcher.Factory`, which needs the real
	 * `@MangaHttpClient` client, and `NetworkModule.provideBaseHttpClient` asserts it is off the
	 * main thread — which is exactly where Robolectric runs.
	 *
	 * The substitute keeps the same attributes, so shape, scale type and declared size still come
	 * from the layout. What is lost is the cover bitmap itself, which is a network image that would
	 * render as a placeholder in a screenshot test regardless. These baselines therefore assert the
	 * frame around the artwork — geometry, type, spacing, badges — and not the artwork.
	 */
	private fun inflate(context: Context, layoutResId: Int): View {
		// Inflating with a null root discards the layout's own layout_width/layout_height, which
		// would stretch a component that declares a fixed width, such as a rail cell. A throwaway
		// parent gives the inflater something to generate LayoutParams against.
		val parent = FrameLayout(context)
		val inflater = LayoutInflater.from(context).cloneInContext(context)
		inflater.factory2 = object : LayoutInflater.Factory2 {

			override fun onCreateView(
				parent: View?,
				name: String,
				context: Context,
				attrs: AttributeSet,
			): View? = if (name in SUBSTITUTED_VIEWS) {
				ShapeableImageView(context, attrs)
			} else {
				null
			}

			override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? =
				onCreateView(null, name, context, attrs)
		}
		return inflater.inflate(layoutResId, parent, false)
	}

	/**
	 * Roborazzi renders through a real window, so the view is hosted in an activity rather than
	 * captured detached. Inflation goes through an explicit [ContextThemeWrapper]: calling
	 * `setTheme` on an already-created activity is unreliable, because its decor and cached
	 * `LayoutInflater` have resolved against the previous theme.
	 */
	private fun capture(name: String, viewFactory: (Context) -> View) {
		Robolectric.buildActivity(AppCompatActivity::class.java).use { controller ->
			val activity = controller.setup().get()
			val themedContext = ContextThemeWrapper(activity, R.style.Theme_Kotatsu)
			val view = viewFactory(themedContext)
			// Keep the width the layout asked for; height always wraps so a capture is only as tall
			// as its content, even for components that declare match_parent.
			val width = view.layoutParams?.width ?: ViewGroup.LayoutParams.MATCH_PARENT
			activity.setContentView(view, ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT))
			shadowOf(Looper.getMainLooper()).idle()
			view.captureRoboImage("$OUTPUT_DIR/$name-$variant.png")
		}
	}

	private companion object {

		const val OUTPUT_DIR = "src/test/screenshots"

		/**
		 * A 120dp cover at the 13:18 ratio `Widget.Kotatsu.ImageView.Cover` applies, rendered at
		 * xhdpi. `CoverImageView` derives this from its width at measure time; the substitute cannot,
		 * so the same arithmetic is repeated here.
		 */
		const val COVER_HEIGHT_PX = 240 * 18 / 13

		/** `preferred_grid_width`, 120dp, rendered at xhdpi. */
		const val GRID_COLUMN_PX = 240

		/** Views that require Hilt injection and are stood in for during inflation. */
		val SUBSTITUTED_VIEWS = setOf(
			"org.koitharu.kotatsu.core.image.CoilImageView",
			"org.koitharu.kotatsu.image.ui.CoverImageView",
		)
	}
}
