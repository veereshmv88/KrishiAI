package com.krishiai.app.ui.components

import androidx.lifecycle.ViewModel
import com.krishiai.app.core.location.LocationHelper
import com.krishiai.app.core.location.LocationSearchEngine
import com.krishiai.app.data.local.dao.LocationDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    val searchEngine: LocationSearchEngine,
    val locationDao: LocationDao,
    val locationHelper: LocationHelper
) : ViewModel()
