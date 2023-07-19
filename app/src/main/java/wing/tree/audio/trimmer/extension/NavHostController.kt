package wing.tree.audio.trimmer.extension

import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder

fun NavHostController.navigate(
    route: Enum<*>,
    builder: NavOptionsBuilder.() -> Unit,
) = navigate(
    route = route.name,
    builder = builder,
)
