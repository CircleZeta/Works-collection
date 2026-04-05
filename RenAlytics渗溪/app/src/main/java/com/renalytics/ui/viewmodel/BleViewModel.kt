package com.renalytics.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renalytics.data.ble.BleService
import com.renalytics.data.store.AppDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BleViewModel(
    private val appDataStore: AppDataStore
) : ViewModel() {
    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _bluetoothDeviceAddress = MutableStateFlow<String?>(null)
    val bluetoothDeviceAddress = _bluetoothDeviceAddress.asStateFlow()

    private val _bluetoothDeviceName = MutableStateFlow<String?>(null)
    val bluetoothDeviceName = _bluetoothDeviceName.asStateFlow()

    private val _connectionState = MutableStateFlow<BleService.BleConnectionState>(BleService.BleConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    init {
        loadSavedDevice()
    }

    private fun loadSavedDevice() {
        viewModelScope.launch {
            appDataStore.bluetoothDeviceAddress.collect {
                _bluetoothDeviceAddress.value = it
            }
        }

        viewModelScope.launch {
            appDataStore.bluetoothDeviceName.collect {
                _bluetoothDeviceName.value = it
            }
        }
    }

    fun saveBluetoothDevice(address: String, name: String) {
        viewModelScope.launch {
            appDataStore.saveBluetoothDevice(address, name)
        }
    }

    fun clearBluetoothDevice() {
        viewModelScope.launch {
            appDataStore.clearBluetoothDevice()
        }
    }

    fun setScanning(isScanning: Boolean) {
        _isScanning.value = isScanning
    }

    fun setConnectionState(state: BleService.BleConnectionState) {
        _connectionState.value = state
    }
}
