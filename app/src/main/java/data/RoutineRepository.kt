package data

import data.local.dao.RoutineDao
import data.local.entity.Routine
import kotlinx.coroutines.flow.Flow

class RoutineRepository(private val routineDao: RoutineDao) {

    val allRoutines: Flow<List<Routine>> = routineDao.getAllRoutines()

    suspend fun getRoutineById(id: Long): Routine? {
        return routineDao.getRoutineById(id)
    }

    suspend fun insertRoutine(routine: Routine): Long {
        return routineDao.insertRoutine(routine)
    }

    suspend fun updateRoutine(routine: Routine) {
        routineDao.updateRoutine(routine)
    }

    suspend fun deleteRoutine(routine: Routine) {
        routineDao.deleteRoutine(routine)
    }

    suspend fun deleteRoutineById(id: Long) {
        routineDao.deleteRoutineById(id)
    }

    suspend fun getRoutineCount(): Int {
        return routineDao.getRoutineCount()
    }
}
