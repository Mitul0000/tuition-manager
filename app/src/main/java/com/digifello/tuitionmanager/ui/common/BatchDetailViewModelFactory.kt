package com.digifello.tuitionmanager.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.digifello.tuitionmanager.ui.batchdetail.BatchDetailViewModel

class BatchDetailViewModelFactory(private val batchId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BatchDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BatchDetailViewModel(batchId = batchId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}