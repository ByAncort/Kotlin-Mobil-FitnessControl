package ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitnesscontrol.FitnessApplication
import com.example.fitnesscontrol.navigation.navBarItems
import com.example.fitnesscontrol.setting.SettingsScreen
import ui.CreateRoutine.CreateRoutineScreen
import ui.CreateRoutine.CreateRoutineViewModel
import ui.ExploreRoutines.ExploreRoutinesScreen
import ui.ExploreRoutines.ExploreRoutinesViewModel
import ui.home.HomeScreen
import ui.home.HomeViewModel
import ui.login.LoginScreen
import ui.profile.EditProfileScreen
import ui.profile.ProfileScreen
import ui.register.RegisterScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val nav = rememberNavController() // ← Este es tu navController
    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Rutas donde NO se muestra el navbar
    val routesWithoutNavBar = listOf(
        Route.Login.path,
        Route.Register.path
    )

    val showNavBar = currentRoute !in routesWithoutNavBar

    Scaffold(
        bottomBar = {
            if (showNavBar) {
                NavigationBar {
                    navBarItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                nav.navigate(item.route) {
                                    popUpTo(nav.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = nav, // ← Aquí se usa "nav"
            startDestination = Route.Login.path,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Route.Register.path) {
                RegisterScreen(
                    onBack = { nav.popBackStack() },
                    onRegistered = {
                        nav.navigate(Route.Login.path) {
                            popUpTo(Route.Login.path) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Route.Login.path) {
                LoginScreen(
                    onRegister = { nav.navigate(Route.Register.path) },
                    onLoginSuccess = {
                        nav.navigate(Route.Home.path) {
                            popUpTo(Route.Login.path) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Route.Home.path) {
                val vm: HomeViewModel = viewModel()
                HomeScreen(
                    onCreateRoutine = { nav.navigate(Route.CreateRoutine.path) },
                    onGenerateWithAI = { nav.navigate(Route.GenerateAI.path) },
                    onExploreRoutines = { nav.navigate(Route.ExploreRoutines.path) },
                    onMyRoutines = { nav.navigate(Route.MyRoutines.path) },
                    onRoutineSelected = { routineId ->
                        nav.navigate("${Route.RoutineDetail.path}/$routineId")
                    },
                    onContinueDraft = {
                        nav.navigate(Route.CreateRoutine.path)
                    },
                    vm = vm
                )
            }

            composable(Route.CreateRoutine.path) {
                val vm: CreateRoutineViewModel = viewModel()
                CreateRoutineScreen(
                    vm = vm,
                    onBack = { nav.popBackStack() }
                )
            }

            composable(Route.ExploreRoutines.path) {
                val vm: ExploreRoutinesViewModel = viewModel()
                ExploreRoutinesScreen(
                    navController = nav,
                    vm = vm
                )
            }

            composable(Route.Profile.path) {
                ProfileScreen(
//                    onEditprofile = {
//                        nav.navigate(Route.EditProfile.path)
//                    }
                )
            }

            composable(Route.Settings.path) {
                SettingsScreen(
                    onLogout = {
                        nav.navigate(Route.Login.path) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Route.EditProfile.path) {
                EditProfileScreen(
                    onBack = { nav.popBackStack() },
                    onSaveSuccess = {
                        nav.popBackStack()
                    }
                )
            }
        }
    }
}