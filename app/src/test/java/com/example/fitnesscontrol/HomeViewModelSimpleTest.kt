package com.example.fitnesscontrol

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import data.AppDatabase
import data.network.AuthPreferencesManager
import data.room.DraftRoutineDao
import data.room.DraftRoutineEntity
import kotlinx.coroutines.flow.first
import ui.home.HomeViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelSimpleTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var mockApplication: Application

    @MockK
    private lateinit var mockDatabase: AppDatabase

    @MockK
    private lateinit var mockDraftDao: DraftRoutineDao

    @MockK
    private lateinit var mockAuthDataStore: AuthPreferencesManager

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        // Mock del Application
        every { mockApplication.applicationContext } returns mockApplication

        // Mock de AppDatabase
        mockkObject(AppDatabase)
        every { AppDatabase.getDatabase(any()) } returns mockDatabase

        // Mock del DAO
        every { mockDatabase.draftRoutineDao() } returns mockDraftDao
        coEvery { mockDraftDao.getDraft() } returns flowOf(null)

        // Mock de AuthPreferencesManager - necesitamos mockear el constructor
        mockkObject(AuthPreferencesManager)
        every { AuthPreferencesManager(any()) } returns mockAuthDataStore

        // Mock de datos de autenticación
        coEvery { mockAuthDataStore.getToken() } returns null
        coEvery { mockAuthDataStore.username.first() } returns null

        // Crear ViewModel
        viewModel = HomeViewModel(mockApplication)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `initial state should be loading`() = runTest {
        // Then
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `draft routine should be null initially`() = runTest {
        // Then
        assertNull(viewModel.uiState.value.draftRoutine)
    }

    @Test
    fun `my routines should be empty initially`() = runTest {
        // Then
        assertTrue(viewModel.uiState.value.myRoutines.isEmpty())
    }

    @Test
    fun `today workout should be null initially`() = runTest {
        // Then
        assertNull(viewModel.uiState.value.todayWorkout)
    }
}