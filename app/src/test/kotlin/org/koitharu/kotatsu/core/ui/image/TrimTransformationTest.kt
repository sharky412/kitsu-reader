package org.koitharu.kotatsu.core.ui.image

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil3.size.Size
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Covers do not always carry detail. A solid colour, a vertical gradient or a generated placeholder
 * all produce an image in which every row is one colour, and trimming such an image used to leave a
 * crop of zero height that `Bitmap.createBitmap` rejects. Coil catches the exception, so the symptom
 * is a cover that silently renders as an error tile rather than a crash.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class TrimTransformationTest {

	@Test
	fun `keeps an image whose every row is a single colour`() {
		val input = bitmap(20, 30) { _, y -> Color.rgb(0, y * 8, 0) }

		val result = transform(input)

		assertEquals(20, result.width)
		assertEquals(30, result.height)
	}

	@Test
	fun `keeps an image whose every column is a single colour`() {
		val input = bitmap(20, 30) { x, _ -> Color.rgb(x * 8, 0, 0) }

		val result = transform(input)

		assertEquals(20, result.width)
		assertEquals(30, result.height)
	}

	@Test
	fun `keeps a single-colour image`() {
		val input = bitmap(20, 30) { _, _ -> Color.WHITE }

		val result = transform(input)

		assertEquals(20, result.width)
		assertEquals(30, result.height)
	}

	@Test
	fun `removes a uniform border`() {
		val border = 4
		val input = bitmap(20, 30) { x, y ->
			val isBorder = x < border || x >= 20 - border || y < border || y >= 30 - border
			when {
				isBorder -> Color.WHITE
				// The interior has to vary along both axes, or its rows and columns read as blank too.
				(x + y) % 2 == 0 -> Color.BLACK
				else -> Color.RED
			}
		}

		val result = transform(input)

		assertEquals(20 - border * 2, result.width)
		assertEquals(30 - border * 2, result.height)
	}

	private fun transform(input: Bitmap): Bitmap = runBlocking {
		TrimTransformation().transform(input, Size.ORIGINAL)
	}

	private fun bitmap(width: Int, height: Int, color: (Int, Int) -> Int): Bitmap {
		val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		for (y in 0 until height) {
			for (x in 0 until width) {
				result.setPixel(x, y, color(x, y))
			}
		}
		return result
	}
}
