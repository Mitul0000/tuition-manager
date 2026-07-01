package com.digifello.tuitionmanager.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.digifello.tuitionmanager.ui.addeditbatch.AddEditBatchViewModel

class AddEditBatchViewModelFactory(private val batchId: String?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditBatchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddEditBatchViewModel(existingBatchId = batchId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}