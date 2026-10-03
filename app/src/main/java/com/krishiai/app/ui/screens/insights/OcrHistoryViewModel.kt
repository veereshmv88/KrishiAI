package com.krishiai.app.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishiai.app.data.local.dao.OcrHistoryDao
import com.krishiai.app.data.local.entity.OcrHistoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OcrHistoryViewModel @Inject constructor(
    private val ocrHistoryDao: OcrHistoryDao
) : ViewModel() {

    val history: StateFlow<List<OcrHistoryEntity>> = ocrHistoryDao.getAllHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteHistoryItem(id: String) {
        viewModelScope.launch {
            ocrHistoryDao.deleteHistoryById(id)
        }
    }
}
