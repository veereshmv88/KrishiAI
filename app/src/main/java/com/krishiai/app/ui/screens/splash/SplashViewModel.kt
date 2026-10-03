package com.krishiai.app.ui.screens.splash

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishiai.app.domain.repository.AuthRepository
import com.krishiai.app.ui.navigation.Screen
import com.krishiai.app.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        checkNavigationTarget()
    }

    private fun checkNavigationTarget() {
        viewModelScope.launch {
            // Guarantee splash displays for a smooth transition animation
            delay(1800)

            val onboardingCompleted = sharedPreferences.getBoolean(Constants.KEY_ONBOARDING_COMPLETED, false)
            if (!onboardingCompleted) {
                _startDestination.value = Screen.Onboarding.route
                return@launch
            }

            try {
                // Collect first value of current user
                val currentUserProfile = authRepository.getCurrentUser().first()
                if (currentUserProfile != null && currentUserProfile.uid.isNotEmpty()) {
                    if (currentUserProfile.role.equals("Farmer", ignoreCase = true)) {
                        _startDestination.value = Screen.FarmerDashboard.route
                    } else if (currentUserProfile.role.equals("Buyer", ignoreCase = true)) {
                        _startDestination.value = Screen.BuyerDashboard.route
                    } else {
                        _startDestination.value = Screen.WelcomeRoleSelection.route
                    }
                } else {
                    _startDestination.value = Screen.WelcomeRoleSelection.route
                }
            } catch (e: Exception) {
                // Fallback on auth failure or offline start
                _startDestination.value = Screen.WelcomeRoleSelection.route
            }
        }
    }
}
