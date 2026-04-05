package com.renalytics.data.ble

import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class BleService : Service() {
    private val TAG = "BleService"

    private val binder = LocalBinder()
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null

    private val _connectionState = MutableSharedFlow<BleConnectionState>()
    val connectionState = _connectionState.asSharedFlow()

    private val _measurementData = MutableSharedFlow<ByteArray>()
    val measurementData = _measurementData.asSharedFlow()

    enum class BleConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING
    }

    inner class LocalBinder : Binder() {
        fun getService(): BleService = this@BleService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        disconnect()
    }

    fun connect(address: String): Boolean {
        bluetoothAdapter?.let {adapter ->
            val device = adapter.getRemoteDevice(address)
            if (device == null) {
                Log.e(TAG, "Device not found.  Unable to connect.")
                return false
            }

            serviceScope.launch {
                _connectionState.emit(BleConnectionState.CONNECTING)
            }

            bluetoothGatt = device.connectGatt(this, false, gattCallback)
            return true
        }
        return false
    }

    fun disconnect() {
        serviceScope.launch {
            _connectionState.emit(BleConnectionState.DISCONNECTING)
        }
        bluetoothGatt?.disconnect()
    }

    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, value: ByteArray): Boolean {
        return bluetoothGatt?.writeCharacteristic(characteristic) ?: false
    }

    fun readCharacteristic(characteristic: BluetoothGattCharacteristic): Boolean {
        return bluetoothGatt?.readCharacteristic(characteristic) ?: false
    }

    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic, enabled: Boolean): Boolean {
        if (bluetoothGatt == null) return false

        if (!bluetoothGatt!!.setCharacteristicNotification(characteristic, enabled)) {
            return false
        }

        val descriptor = characteristic.getDescriptor(
            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        )
        if (descriptor != null) {
            descriptor.value = if (enabled) {
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            } else {
                BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            }
            return bluetoothGatt!!.writeDescriptor(descriptor)
        }
        return false
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val intentAction: String
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Connected to GATT server.")
                    serviceScope.launch {
                        _connectionState.emit(BleConnectionState.CONNECTED)
                    }
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Disconnected from GATT server.")
                    serviceScope.launch {
                        _connectionState.emit(BleConnectionState.DISCONNECTED)
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // 发现服务后，可以获取特征并设置通知
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                serviceScope.launch {
                    _measurementData.emit(characteristic.value)
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            serviceScope.launch {
                _measurementData.emit(characteristic.value)
            }
        }
    }
}
