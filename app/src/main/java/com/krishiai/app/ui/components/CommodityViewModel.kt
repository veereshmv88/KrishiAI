package com.krishiai.app.ui.components

import androidx.lifecycle.ViewModel
import com.krishiai.app.core.commodity.CommoditySearchEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CommodityViewModel @Inject constructor(
    val searchEngine: CommoditySearchEngine
) : ViewModel()
