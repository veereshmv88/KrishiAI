package com.krishiai.app.domain.repository

import com.krishiai.app.data.common.Resource
import kotlinx.coroutines.flow.Flow

interface CropRepository {
    fun getListings(): Flow<Resource<List<Any>>>
}

interface UserRepository {
    fun getUserProfile(uid: String): Flow<Resource<Any>>
}
