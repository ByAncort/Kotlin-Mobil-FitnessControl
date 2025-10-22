package repository

import data.Routine

class RoutineRepository {

    private val routines = mutableListOf<Routine>()
    private var nextId = 1

    fun saveRoutine(routine: Routine): Routine {
        val newRoutine = routine.copy(id = nextId.toString())
        routines.add(newRoutine)
        nextId++
        return newRoutine
    }

    fun getRoutines(): List<Routine> {
        return routines.toList()
    }

    fun getRoutineById(id: String): Routine? {
        return routines.find { it.id == id }
    }

    fun deleteRoutine(id: String): Boolean {
        return routines.removeIf { it.id == id }
    }
}