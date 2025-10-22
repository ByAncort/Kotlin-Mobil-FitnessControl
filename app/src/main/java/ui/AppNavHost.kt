package ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ui.CreateRoutine.CreateRoutineScreen
import ui.CreateRoutine.CreateRoutineViewModel
import ui.home.HomeScreen
import ui.home.HomeViewModel
import ui.login.LoginScreen
import ui.register.RegisterScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Route.Login.path) {


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
                onRoutineSelected = { routineId -> nav.navigate("${Route.RoutineDetail.path}/$routineId") },
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
     }
}
