package com.example.fitnesscontrol

import android.app.Application
import data.AppDatabase
import data.RoutineRepository

class FitnessApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
}
