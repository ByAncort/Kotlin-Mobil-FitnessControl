package ui

sealed class Route(val path: String) {
    data object Register : Route("registrarse")
    data object Login : Route("login")
    data object Home : Route("Home")
    data object CreateRoutine : Route("create_routine")
    data object GenerateAI : Route("generate_ai")
    data object ExploreRoutines : Route("explore_routines")
    data object MyRoutines : Route("my_routines")
    data object RoutineDetail : Route("routine_detail") {
        fun createRoute(routineId: String) = "$path/$routineId"
    }
}