package com.krishiai.app.ui.screens.auth

import com.krishiai.app.data.model.User
import com.krishiai.app.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private val testUser = User(
        uid = "test-uid",
        fullName = "Test User",
        email = "test@test.com",
        mobileNumber = "9876543210",
        role = "Farmer",
        district = "Bangalore",
        taluk = "Anekal"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { authRepository.getCurrentUser() } returns flowOf(null)
        viewModel = AuthViewModel(authRepository, mockk(relaxed = true))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        assertEquals(AuthState.Idle, viewModel.authState.value)
    }

    @Test
    fun `selectedRole defaults to Farmer`() {
        assertEquals("Farmer", viewModel.selectedRole.value)
    }

    @Test
    fun `selectRole updates role correctly`() {
        viewModel.selectRole("Buyer")
        assertEquals("Buyer", viewModel.selectedRole.value)
    }

    @Test
    fun `login with empty email returns error`() {
        viewModel.login("", "password123")
        assertEquals(
            AuthState.Error("Email and Password cannot be empty"),
            viewModel.authState.value
        )
    }

    @Test
    fun `login with empty password returns error`() {
        viewModel.login("test@test.com", "")
        assertEquals(
            AuthState.Error("Email and Password cannot be empty"),
            viewModel.authState.value
        )
    }

    @Test
    fun `login success updates state`() = runTest {
        coEvery { authRepository.login("test@test.com", "pass123") } returns Result.success(testUser)

        viewModel.login("test@test.com", "pass123")
        advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Success)
        assertEquals("Test User", (state as AuthState.Success).user.fullName)
    }

    @Test
    fun `login failure updates state with error`() = runTest {
        coEvery { authRepository.login("bad@test.com", "wrong") } returns Result.failure(Exception("Invalid credentials"))

        viewModel.login("bad@test.com", "wrong")
        advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertTrue((state as AuthState.Error).message.contains("Invalid credentials"))
    }

    @Test
    fun `signup with empty fields returns error`() {
        viewModel.signup(
            fullName = "",
            mobileNumber = "",
            email = "",
            password = "",
            district = ""
        )
        assertEquals(
            AuthState.Error("Please fill all mandatory fields"),
            viewModel.authState.value
        )
    }

    @Test
    fun `logout resets state to Idle`() = runTest {

        viewModel.logout()
        advanceUntilIdle()

        assertEquals(AuthState.Idle, viewModel.authState.value)
    }

    @Test
    fun `clearError resets state to Idle`() {
        viewModel.login("", "")
        assertTrue(viewModel.authState.value is AuthState.Error)

        viewModel.clearError()
        assertEquals(AuthState.Idle, viewModel.authState.value)
    }
}

