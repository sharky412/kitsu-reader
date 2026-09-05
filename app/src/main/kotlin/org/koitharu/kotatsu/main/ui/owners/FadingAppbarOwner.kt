package org.koitharu.kotatsu.main.ui.owners

/**
 * A primary destination that puts its own navigation directly beneath the app bar.
 *
 * Those destinations need the app bar to fade into the content behind it rather than sit as a
 * separate raised surface, or the two strips read as two competing headers. [MainActivity] used to
 * decide this by naming one fragment class, which meant wrapping that fragment inside another
 * destination silently lost the treatment. Declaring the requirement lets any destination ask for
 * it.
 */
interface FadingAppbarOwner
