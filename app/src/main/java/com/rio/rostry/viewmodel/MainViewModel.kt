package com.rio.rostry.viewmodel

import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    fun crash() {
        throw RuntimeException("Test Crash")
    }
}
