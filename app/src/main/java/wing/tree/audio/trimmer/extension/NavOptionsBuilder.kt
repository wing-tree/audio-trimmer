package wing.tree.audio.trimmer.extension

import androidx.navigation.NavOptionsBuilder
import androidx.navigation.PopUpToBuilder

fun NavOptionsBuilder.popupTo(
    route: Enum<*>,
    popUpToBuilder: PopUpToBuilder.() -> Unit,
) = popUpTo(
    route = route.name,
    popUpToBuilder = popUpToBuilder,
)
