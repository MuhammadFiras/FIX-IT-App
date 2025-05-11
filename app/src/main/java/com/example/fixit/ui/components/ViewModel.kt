package com.example.fixit.ui.components

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel

class BottomBarViewModel : ViewModel() {
    // MutableStateFlow to hold the selected tab index
    private val _selectedIndex = MutableStateFlow(0) // Default to 0
    val selectedIndex: StateFlow<Int> get() = _selectedIndex

    // Function to set selected tab index
    fun setSelectedIndex(index: Int) {
        _selectedIndex.value = index
    }
}
