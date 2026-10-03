package com.krishiai.app.core.auth

import com.krishiai.app.data.local.prefs.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoleManager @Inject constructor(
    private val dataStoreManager: DataStoreManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val activeRole: StateFlow<UserRole> = dataStoreManager.userRoleFlow
        .map { roleString -> UserRole.fromString(roleString) }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = UserRole.UNKNOWN
        )

    fun setRole(role: UserRole) {
        scope.launch {
            dataStoreManager.setUserRole(role.name)
        }
    }
}
