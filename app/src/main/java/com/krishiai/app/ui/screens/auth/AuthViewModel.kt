package com.krishiai.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishiai.app.data.model.User
import com.krishiai.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    data class Success(val user: User) : AuthState
    data class Error(val message: String) : AuthState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val roleManager: com.krishiai.app.core.auth.RoleManager
) : ViewModel() {

    // Shared Welcome Role selection: "Farmer" or "Buyer"
    private val _selectedRole = MutableStateFlow("Farmer")
    val selectedRole: StateFlow<String> = _selectedRole.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentUser: StateFlow<User?> = authRepository.getCurrentUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun selectRole(role: String) {
        _selectedRole.value = role
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and Password cannot be empty")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.login(email, password)
                .onSuccess { user ->
                    roleManager.setRole(com.krishiai.app.core.auth.UserRole.fromString(user.role))
                    _authState.value = AuthState.Success(user)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.localizedMessage ?: "Login failed. Please check credentials.")
                }
        }
    }

    fun signup(
        fullName: String,
        mobileNumber: String,
        email: String,
        password: String,
        district: String,
        taluk: String = "",
        city: String = "",
        businessName: String = "",
        profilePhotoBytes: ByteArray? = null
    ) {
        if (fullName.isBlank() || mobileNumber.isBlank() || email.isBlank() || password.isBlank() || district.isBlank()) {
            _authState.value = AuthState.Error("Please fill all mandatory fields")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            var photoUrl = ""
            if (profilePhotoBytes != null) {
                authRepository.uploadProfilePhoto(profilePhotoBytes)
                    .onSuccess { photoUrl = it }
                    .onFailure { /* Continue with empty photoUrl if upload fails */ }
            }

            val newUser = User(
                fullName = fullName,
                mobileNumber = mobileNumber,
                email = email,
                role = _selectedRole.value,
                district = district,
                taluk = taluk,
                city = city,
                businessName = businessName,
                profilePhotoUrl = photoUrl
            )

            authRepository.signup(newUser, password)
                .onSuccess { user ->
                    roleManager.setRole(com.krishiai.app.core.auth.UserRole.fromString(user.role))
                    _authState.value = AuthState.Success(user)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.localizedMessage ?: "Registration failed")
                }
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank()) {
            onError("Email address is required")
            return
        }
        viewModelScope.launch {
            authRepository.resetPassword(email)
                .onSuccess { onSuccess() }
                .onFailure { onError(it.localizedMessage ?: "Reset failed") }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Idle
        }
    }

    fun clearError() {
        _authState.value = AuthState.Idle
    }
}
