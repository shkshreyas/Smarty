package com.shk.smarty.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shk.smarty.di.ViewModelFactoryProvider

/**
 * Helper functions for creating ViewModels with assisted injection via Compose
 */
object ViewModelHelper {
    
    /**
     * Creates a ViewModel with assisted injection
     * 
     * @param factoryProducer Lambda to produce the ViewModelFactoryProvider
     * @return The requested ViewModel instance
     */
    @Composable
    inline fun <reified VM : ViewModel> createViewModel(
        noinline factoryProducer: @Composable () -> ViewModelFactoryProvider
    ): VM {
        val factory = produceFactory(factoryProducer)
        return viewModel(factory = factory)
    }
    
    /**
     * Produces a ViewModel factory using the ViewModelFactoryProvider
     * 
     * @param factoryProducer Lambda to produce the ViewModelFactoryProvider
     * @return A ViewModelProvider.Factory that can create assisted injected ViewModels
     */
    @Composable
    fun produceFactory(
        factoryProducer: @Composable () -> ViewModelFactoryProvider
    ): ViewModelProvider.Factory {
        val owner = LocalSavedStateRegistryOwner.current
        val factoryProvider = factoryProducer()
        return remember(owner, factoryProvider) {
            factoryProvider.create(owner)
        }
    }
} 