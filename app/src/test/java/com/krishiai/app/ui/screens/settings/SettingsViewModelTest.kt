package com.krishiai.app.ui.screens.settings

import com.krishiai.app.data.local.prefs.DataStoreManager
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
class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel
    private val dataStoreManager: DataStoreManager = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { dataStoreManager.isDarkModeFlow } returns flowOf(false)
        every { dataStoreManager.languageFlow } returns flowOf("en")
        every { dataStoreManager.notificationsFlow } returns flowOf(true)
        every { dataStoreManager.offlineSyncFlow } returns flowOf(true)
        viewModel = SettingsViewModel(dataStoreManager, mockk(relaxed = true))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `isDarkMode defaults to false`() = runTest {
        advanceUntilIdle()
        assertFalse(viewModel.isDarkMode.value)
    }

    @Test
    fun `currentLanguage defaults to en`() = runTest {
        advanceUntilIdle()
        assertEquals("en", viewModel.currentLanguage.value)
    }

    @Test
    fun `notificationsEnabled defaults to true`() = runTest {
        advanceUntilIdle()
        assertTrue(viewModel.notificationsEnabled.value)
    }

    @Test
    fun `setDarkMode calls DataStoreManager`() = runTest {
        viewModel.setDarkMode(true)
        advanceUntilIdle()
        coVerify { dataStoreManager.setDarkMode(true) }
    }

    @Test
    fun `setLanguage calls DataStoreManager`() = runTest {
        viewModel.setLanguage("kn")
        advanceUntilIdle()
        coVerify { dataStoreManager.setLanguage("kn") }
    }

    @Test
    fun `setNotificationsEnabled calls DataStoreManager`() = runTest {
        viewModel.setNotificationsEnabled(false)
        advanceUntilIdle()
        coVerify { dataStoreManager.setNotificationsEnabled(false) }
    }

    @Test
    fun `setOfflineSync calls DataStoreManager`() = runTest {
        viewModel.setOfflineSync(false)
        advanceUntilIdle()
        coVerify { dataStoreManager.setOfflineSync(false) }
    }
}


