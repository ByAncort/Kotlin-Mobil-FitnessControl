package com.example.fitnesscontrol

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import data.AppDatabase

class FitnessApplication : Application() {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = "auth_datastore"
        )
    }

    val authDataStore: DataStore<Preferences> by lazy {
        applicationContext.dataStore
    }

    override fun onCreate() {
        super.onCreate()
        println("MyApplication inicializado")
    }

    val database by lazy { AppDatabase.getDatabase(this) }
}