package br.com.atlas.atlasapp.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val PROFILE = "profile"
    const val DETAILS = "details"
    const val IN_PROGRESS = "in_progress"
    const val NEW_ROUTE = "new_route"
}

sealed class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String)
{
    data object HomeButton :
        BottomNavItem("Início", Icons.Default.Home, Routes.HOME)
    data object ExploreButton :
        BottomNavItem("Explorar", Icons.Default.LocationOn, Routes.EXPLORE)
    data object CreateButton :
        BottomNavItem("Criar", Icons.Default.Add, Routes.NEW_ROUTE)
    data object ProfileButton :
        BottomNavItem("Perfil", Icons.Default.Person, Routes.PROFILE)
}