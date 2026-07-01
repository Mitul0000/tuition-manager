package com.digifello.tuitionmanager.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.digifello.tuitionmanager.ui.batchdetail.BatchDetailViewModel

/**
 * BatchDetailViewModel needs a batchId passed in at creation time
 * (it's not something it can look up on its own — it comes from
 * which batch the user tapped on). Compose's default viewModel()
 * function can't pass custom constructor arguments, so this Factory
 * bridges that gap.
 */
class BatchDetailViewModelFactory(private val batchId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BatchDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BatchDetailViewModel(batchId = batchId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}