package com.example.fixit.ui.components

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BottomBarViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    // Use SavedStateHandle to persist the selectedIndex
    private val _selectedIndex = MutableStateFlow(savedStateHandle.get<Int>("selectedIndex") ?: 0)
    val selectedIndex: StateFlow<Int> get() = _selectedIndex

    // Function to set selected tab index
    fun setSelectedIndex(index: Int) {
        _selectedIndex.value = index
        // Save state to SavedStateHandle for restoration across configuration changes
        savedStateHandle.set("selectedIndex", index)
    }
}
