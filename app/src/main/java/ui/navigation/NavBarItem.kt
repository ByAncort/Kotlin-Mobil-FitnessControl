package ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

data class NavBarItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val navBarItems = listOf(
    NavBarItem(
        route = Route.Home.path,
        icon = Icons.Filled.Home,
        label = "Inicio"
    ),
    NavBarItem(
        route = Route.Profile.path,
        icon = Icons.Filled.Person,
        label = "Perfil"
    ),
    NavBarItem(
        route = Route.Settings.path,
        icon = Icons.Filled.Settings,
        label = "Ajustes"
    )
)

