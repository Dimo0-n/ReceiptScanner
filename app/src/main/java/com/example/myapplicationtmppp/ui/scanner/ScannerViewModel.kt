package com.example.myapplicationtmppp.ui.scanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScannerViewModel : ViewModel() {
    private val _navigateToScanner = MutableLiveData<Boolean>()
    val navigateToScanner: LiveData<Boolean> get() = _navigateToScanner

    fun startScanning() {
        _navigateToScanner.value = true
    }

    fun doneNavigating() {
        _navigateToScanner.value = false
    }
}
