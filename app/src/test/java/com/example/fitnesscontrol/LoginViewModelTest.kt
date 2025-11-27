package com.example.fitnesscontrol

import android.app.Application
import data.network.AuthPreferencesManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ui.login.LoginViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var mockApplication: Application

    @MockK
    private lateinit var mockAuthDataStore: AuthPreferencesManager

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        // Mock del Application
        every { mockApplication.applicationContext } returns mockApplication

        // Mock de AuthPreferencesManager con flujos
        val emptyTokenFlow = MutableStateFlow<String?>(null)
        coEvery { mockAuthDataStore.authToken } returns emptyTokenFlow
        coEvery { mockAuthDataStore.clearAuthData() } returns Unit
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== TESTS DE ESTADO INICIAL ==========

    @Test
    fun `initial state should have empty fields`() = runTest {
        // When
        viewModel = LoginViewModel(mockApplication)

        // Then
        val state = viewModel.ui.value
        assertEquals("", state.username)
        assertEquals("", state.password)
        assertFalse(state.loading)
        assertNull(state.error)
        assertNull(state.message)
        assertFalse(state.loggedIn)
    }

    // ========== TESTS DE CAMBIOS DE INPUT ==========

    @Test
    fun `onUsernameChange should update username`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)

        // When
        viewModel.onUsernameChange("testuser")

        // Then
        assertEquals("testuser", viewModel.ui.value.username)
    }

    @Test
    fun `onUsernameChange should clear error`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)
        viewModel.submit() // Esto generará un error
        advanceUntilIdle()

        // When
        viewModel.onUsernameChange("test")

        // Then
        assertNull(viewModel.ui.value.error)
    }

    @Test
    fun `onPasswordChange should update password`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)

        // When
        viewModel.onPasswordChange("password123")

        // Then
        assertEquals("password123", viewModel.ui.value.password)
    }

    @Test
    fun `onPasswordChange should clear error`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)
        viewModel.submit() // Esto generará un error
        advanceUntilIdle()

        // When
        viewModel.onPasswordChange("test")

        // Then
        assertNull(viewModel.ui.value.error)
    }

    // ========== TESTS DE VALIDACIÓN ==========

    @Test
    fun `submit with empty username should show error`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)
        viewModel.onPasswordChange("password123")

        // When
        viewModel.submit()

        // Then
        assertEquals("El nombre de usuario es requerido", viewModel.ui.value.error)
        assertFalse(viewModel.ui.value.loading)
        assertFalse(viewModel.ui.value.loggedIn)
    }

    @Test
    fun `submit with blank username should show error`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)
        viewModel.onUsernameChange("   ")
        viewModel.onPasswordChange("password123")

        // When
        viewModel.submit()

        // Then
        assertEquals("El nombre de usuario es requerido", viewModel.ui.value.error)
    }

    @Test
    fun `submit with empty password should show error`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)
        viewModel.onUsernameChange("testuser")

        // When
        viewModel.submit()

        // Then
        assertEquals("La contraseña es requerida", viewModel.ui.value.error)
        assertFalse(viewModel.ui.value.loading)
        assertFalse(viewModel.ui.value.loggedIn)
    }

    @Test
    fun `submit with blank password should show error`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)
        viewModel.onUsernameChange("testuser")
        viewModel.onPasswordChange("   ")

        // When
        viewModel.submit()

        // Then
        assertEquals("La contraseña es requerida", viewModel.ui.value.error)
    }

    @Test
    fun `submit with both fields empty should show username error first`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)

        // When
        viewModel.submit()

        // Then
        assertEquals("El nombre de usuario es requerido", viewModel.ui.value.error)
    }

    // ========== TESTS DE LOADING STATE ==========

    @Test
    fun `submit with valid credentials should set loading true initially`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)
        viewModel.onUsernameChange("testuser")
        viewModel.onPasswordChange("password123")

        // When
        viewModel.submit()

        // Then - Verificar que loading se establece a true inmediatamente
        assertTrue(viewModel.ui.value.loading)
    }

    @Test
    fun `loading should be false after validation error`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)

        // When
        viewModel.submit()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.ui.value.loading)
    }

    // ========== TESTS DE CHECK LOGIN STATUS ==========

    @Test
    fun `checkLoginStatus with empty token should keep loggedIn false`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)
        val emptyTokenFlow = MutableStateFlow<String?>(null)
        coEvery { mockAuthDataStore.authToken } returns emptyTokenFlow

        // When
        viewModel.checkLoginStatus()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.ui.value.loggedIn)
    }

    @Test
    fun `checkLoginStatus with valid token should set loggedIn true`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)
        val tokenFlow = MutableStateFlow<String?>("valid_token_12345")
        coEvery { mockAuthDataStore.authToken } returns tokenFlow

        // When
        viewModel.checkLoginStatus()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.ui.value.loggedIn)
    }

    @Test
    fun `checkLoginStatus with null token should keep loggedIn false`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)
        val nullTokenFlow = MutableStateFlow<String?>(null)
        coEvery { mockAuthDataStore.authToken } returns nullTokenFlow

        // When
        viewModel.checkLoginStatus()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.ui.value.loggedIn)
    }

    // ========== TESTS DE LOGOUT ==========

    @Test
    fun `logout should clear auth data`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)

        // When
        viewModel.logout()
        advanceUntilIdle()

        // Then
        coVerify { mockAuthDataStore.clearAuthData() }
    }

    @Test
    fun `logout should set loggedIn to false`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)
        // Simular que el usuario está logueado
        viewModel.onUsernameChange("testuser")
        viewModel.onPasswordChange("password123")

        // When
        viewModel.logout()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.ui.value.loggedIn)
    }

    @Test
    fun `logout should clear username and password`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)
        viewModel.onUsernameChange("testuser")
        viewModel.onPasswordChange("password123")

        // When
        viewModel.logout()
        advanceUntilIdle()

        // Then
        assertEquals("", viewModel.ui.value.username)
        assertEquals("", viewModel.ui.value.password)
    }

    @Test
    fun `logout should show logout message`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)

        // When
        viewModel.logout()
        advanceUntilIdle()

        // Then
        assertEquals("Sesión cerrada", viewModel.ui.value.message)
    }

    // ========== TESTS DE MESSAGE CONSUMPTION ==========

    @Test
    fun `messageConsumed should clear message`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)
        viewModel.logout() // Esto establece un mensaje
        advanceUntilIdle()
        assertNotNull(viewModel.ui.value.message)

        // When
        viewModel.messageConsumed()

        // Then
        assertNull(viewModel.ui.value.message)
    }

    @Test
    fun `messageConsumed on null message should not crash`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)
        assertNull(viewModel.ui.value.message)

        // When
        viewModel.messageConsumed()

        // Then
        assertNull(viewModel.ui.value.message)
    }

    // ========== TESTS DE ERROR HANDLING ==========

    @Test
    fun `error should be null initially`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)

        // Then
        assertNull(viewModel.ui.value.error)
    }

    @Test
    fun `validation error should not affect other fields`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)
        viewModel.onUsernameChange("")
        viewModel.onPasswordChange("password123")

        // When
        viewModel.submit()

        // Then
        assertNotNull(viewModel.ui.value.error)
        assertEquals("", viewModel.ui.value.username)
        assertEquals("password123", viewModel.ui.value.password)
        assertFalse(viewModel.ui.value.loggedIn)
    }

    // ========== TESTS DE ESTADO MÚLTIPLE ==========

    @Test
    fun `multiple username changes should update correctly`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)

        // When
        viewModel.onUsernameChange("user1")
        assertEquals("user1", viewModel.ui.value.username)

        viewModel.onUsernameChange("user2")
        assertEquals("user2", viewModel.ui.value.username)

        viewModel.onUsernameChange("finaluser")

        // Then
        assertEquals("finaluser", viewModel.ui.value.username)
    }

    @Test
    fun `state changes should be independent`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)

        // When
        viewModel.onUsernameChange("testuser")
        viewModel.onPasswordChange("testpass")

        // Then
        assertEquals("testuser", viewModel.ui.value.username)
        assertEquals("testpass", viewModel.ui.value.password)
        assertNull(viewModel.ui.value.error)
        assertFalse(viewModel.ui.value.loading)
    }
}