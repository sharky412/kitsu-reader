package org.koitharu.kotatsu.screenshot

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Dark-theme baselines. See [ComponentScreenshotTestCase]. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = QUALIFIERS_DARK)
class ComponentScreenshotDarkTest : ComponentScreenshotTestCase("dark")
