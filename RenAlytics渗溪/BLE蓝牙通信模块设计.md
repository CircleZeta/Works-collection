# BLE蓝牙通信模块设计

## 1. 模块概述

BLE（低功耗蓝牙）通信模块是肾功能监测设备Android应用的核心组件，负责与便携式生化分析仪进行数据交互。该模块实现了设备扫描、连接、数据传输等功能，确保应用能够实时获取和处理设备测量数据。

## 2. 核心类设计

### 2.1 BleManager

**职责**：蓝牙管理核心类，处理设备扫描、连接、服务发现等操作。

**主要方法**：
- `startScan()`：开始扫描BLE设备
- `stopScan()`：停止扫描
- `connect(device: BleDevice)`：连接到指定设备
- `disconnect(device: BleDevice)`：断开连接
- `getConnectedDevices()`：获取已连接设备列表
- `requestMtu(device: BleDevice, mtu: Int)`：请求MTU大小

**代码实现**：

```kotlin
class BleManager(private val context: Context) {
    private val bluetoothManager: BluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager.adapter
    }
    private var scanCallback: ScanCallback? = null
    private val connectedDevices = mutableMapOf<String, BleDevice>()
    private val operationQueue = BleOperationQueue()
    
    // 检查蓝牙是否可用
    fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter != null && bluetoothAdapter?.isEnabled == true
    }
    
    // 开始扫描
    fun startScan(callback: (BleDevice) -> Unit) {
        if (!isBluetoothAvailable()) return
        
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = BleDevice(result.device, result.rssi, result.scanRecord)
                callback(device)
            }
            
            override fun onScanFailed(errorCode: Int) {
                // 处理扫描失败
            }
        }
        
        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString(SERVICE_UUID))
                .build()
        )
        
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        
        bluetoothAdapter?.bluetoothLeScanner?.startScan(filters, settings, scanCallback)
    }
    
    // 停止扫描
    fun stopScan() {
        scanCallback?.let {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(it)
        }
    }
    
    // 连接设备
    fun connect(device: BleDevice, callback: BleGattCallback) {
        val gatt = device.device.connectGatt(context, false, callback)
        device.gatt = gatt
        connectedDevices[device.address] = device
    }
    
    // 断开连接
    fun disconnect(device: BleDevice) {
        device.gatt?.disconnect()
        connectedDevices.remove(device.address)
    }
    
    // 请求MTU
    fun requestMtu(device: BleDevice, mtu: Int) {
        operationQueue.addOperation {
            device.gatt?.requestMtu(mtu)
        }
    }
    
    companion object {
        private const val SERVICE_UUID = "0000180f-0000-1000-8000-00805f9b34fb" // 示例UUID
    }
}
```

### 2.2 BleDevice

**职责**：设备模型，包含设备信息和操作方法。

**主要属性**：
- `device`：BluetoothDevice实例
- `rssi`：信号强度
- `scanRecord`：扫描记录
- `gatt`：BluetoothGatt实例
- `services`：已发现的服务

**主要方法**：
- `readCharacteristic(characteristic: BluetoothGattCharacteristic)`：读取特征值
- `writeCharacteristic(characteristic: BluetoothGattCharacteristic, value: ByteArray)`：写入特征值
- `setCharacteristicNotification(characteristic: BluetoothGattCharacteristic, enabled: Boolean)`：设置特征值通知

**代码实现**：

```kotlin
class BleDevice(
    val device: BluetoothDevice,
    val rssi: Int,
    val scanRecord: ScanRecord?
) {
    var gatt: BluetoothGatt? = null
    var services: List<BluetoothGattService>? = null
    
    val name: String get() = device.name ?: "未知设备"
    val address: String get() = device.address
    
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        gatt?.readCharacteristic(characteristic)
    }
    
    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        gatt?.writeCharacteristic(characteristic, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
    }
    
    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic, enabled: Boolean) {
        gatt?.setCharacteristicNotification(characteristic, enabled)
        if (enabled) {
            val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
            descriptor?.value = if (enabled) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            gatt?.writeDescriptor(descriptor)
        }
    }
}
```

### 2.3 BleGattCallback

**职责**：GATT操作回调处理，处理连接状态、服务发现、数据传输等事件。

**主要方法**：
- `onConnectionStateChange()`：连接状态变化回调
- `onServicesDiscovered()`：服务发现回调
- `onCharacteristicRead()`：特征值读取回调
- `onCharacteristicWrite()`：特征值写入回调
- `onCharacteristicChanged()`：特征值变化通知回调
- `onMtuChanged()`：MTU变更回调

**代码实现**：

```kotlin
class BleGattCallback(
    private val onConnected: (BleDevice) -> Unit,
    private val onDisconnected: (BleDevice) -> Unit,
    private val onServicesDiscovered: (BleDevice, List<BluetoothGattService>) -> Unit,
    private val onDataReceived: (BleDevice, ByteArray) -> Unit,
    private val onMtuChanged: (BleDevice, Int) -> Unit
) : BluetoothGattCallback() {
    
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        if (gatt == null) return
        
        val device = BleDevice(gatt.device, 0, null).apply { this.gatt = gatt }
        
        when (newState) {
            BluetoothProfile.STATE_CONNECTED -> {
                gatt.discoverServices()
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                onDisconnected(device)
            }
        }
    }
    
    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        if (gatt == null || status != BluetoothGatt.GATT_SUCCESS) return
        
        val device = BleDevice(gatt.device, 0, null).apply { 
            this.gatt = gatt
            this.services = gatt.services
        }
        
        onServicesDiscovered(device, gatt.services)
        onConnected(device)
    }
    
    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        if (gatt == null || characteristic == null || status != BluetoothGatt.GATT_SUCCESS) return
        
        val device = BleDevice(gatt.device, 0, null).apply { this.gatt = gatt }
        onDataReceived(device, characteristic.value)
    }
    
    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        if (gatt == null || characteristic == null) return
        
        val device = BleDevice(gatt.device, 0, null).apply { this.gatt = gatt }
        onDataReceived(device, characteristic.value)
    }
    
    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        if (gatt == null || status != BluetoothGatt.GATT_SUCCESS) return
        
        val device = BleDevice(gatt.device, 0, null).apply { this.gatt = gatt }
        onMtuChanged(device, mtu)
    }
}
```

### 2.4 BleOperationQueue

**职责**：蓝牙操作队列管理，确保蓝牙操作按顺序执行，避免并发操作冲突。

**主要方法**：
- `addOperation(operation: () -> Unit)`：添加操作到队列
- `start()`：开始执行队列
- `stop()`：停止执行队列

**代码实现**：

```kotlin
class BleOperationQueue {
    private val queue = LinkedList<() -> Unit>()
    private var isRunning = false
    
    fun addOperation(operation: () -> Unit) {
        queue.add(operation)
        if (!isRunning) {
            start()
        }
    }
    
    private fun start() {
        isRunning = true
        processNext()
    }
    
    private fun processNext() {
        if (queue.isEmpty()) {
            isRunning = false
            return
        }
        
        val operation = queue.removeFirst()
        operation()
        
        // 延迟执行下一个操作，确保蓝牙操作有足够时间完成
        Handler(Looper.getMainLooper()).postDelayed({
            processNext()
        }, 100)
    }
    
    fun stop() {
        queue.clear()
        isRunning = false
    }
}
```

### 2.5 BleDataParser

**职责**：数据解析器，处理设备返回的原始数据，转换为应用可用的测量数据。

**主要方法**：
- `parseData(data: ByteArray)`：解析原始数据
- `getMeasurementData()`：获取解析后的测量数据

**代码实现**：

```kotlin
class BleDataParser {
    fun parseData(data: ByteArray): MeasurementData {
        // 解析设备返回的原始数据
        // 这里根据实际设备的协议进行解析
        
        val measurementData = MeasurementData()
        
        // 示例解析逻辑，实际需根据设备协议调整
        if (data.size >= 24) {
            // 解析eGFR
            measurementData.eGFR = ByteBuffer.wrap(data, 0, 4).getFloat()
            
            // 解析血肌酐
            measurementData.creatinine = ByteBuffer.wrap(data, 4, 4).getFloat()
            
            // 解析尿素氮
            measurementData.bun = ByteBuffer.wrap(data, 8, 4).getFloat()
            
            // 解析胱抑素C
            measurementData.cystatinC = ByteBuffer.wrap(data, 12, 4).getFloat()
            
            // 解析尿酸
            measurementData.uricAcid = ByteBuffer.wrap(data, 16, 4).getFloat()
            
            // 解析尿蛋白
            measurementData.proteinuria = ByteBuffer.wrap(data, 20, 4).getFloat()
        }
        
        // 计算CKD分期
        measurementData.ckdStage = calculateCkdStage(measurementData.eGFR)
        
        return measurementData
    }
    
    private fun calculateCkdStage(eGFR: Float): Int {
        return when {
            eGFR >= 90 -> 1
            eGFR >= 60 -> 2
            eGFR >= 45 -> 3
            eGFR >= 30 -> 4
            eGFR >= 15 -> 5
            else -> 5
        }
    }
    
    data class MeasurementData(
        var eGFR: Float = 0f,
        var creatinine: Float = 0f,
        var bun: Float = 0f,
        var cystatinC: Float = 0f,
        var uricAcid: Float = 0f,
        var proteinuria: Float = 0f,
        var albuminuria: Float = 0f,
        var urineVolume: Float = 0f,
        var ckdStage: Int = 0
    )
}
```

## 3. 权限处理

### 3.1 Android 6.0+ 权限

需要获取位置权限，用于BLE扫描：

```kotlin
// 检查位置权限
fun checkLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

// 请求位置权限
fun requestLocationPermission(activity: Activity, requestCode: Int) {
    ActivityCompat.requestPermissions(
        activity,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        requestCode
    )
}
```

### 3.2 Android 12+ 权限

需要获取蓝牙相关权限：

```kotlin
// 检查蓝牙权限
fun checkBluetoothPermissions(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

// 请求蓝牙权限
fun requestBluetoothPermissions(activity: Activity, requestCode: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            ),
            requestCode
        )
    }
}
```

## 4. 后台同步服务

使用前台服务实现后台数据同步：

```kotlin
class BleSyncService : Service() {
    private lateinit var bleManager: BleManager
    private val notificationId = 1
    private val channelId = "ble_sync_channel"
    
    override fun onCreate() {
        super.onCreate()
        bleManager = BleManager(this)
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(notificationId, notification)
        
        // 开始同步数据
        syncData()
        
        return START_STICKY
    }
    
    private fun syncData() {
        // 实现数据同步逻辑
        // 1. 扫描设备
        // 2. 连接设备
        // 3. 获取数据
        // 4. 存储数据
        // 5. 发送通知
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "BLE同步服务",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("肾功能监测")
            .setContentText("正在同步设备数据")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .build()
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
```

## 5. 使用示例

### 5.1 扫描设备

```kotlin
val bleManager = BleManager(context)

// 检查权限
if (!bleManager.isBluetoothAvailable()) {
    // 提示用户开启蓝牙
    return
}

// 开始扫描
bleManager.startScan { device ->
    // 处理发现的设备
    Log.d("BLE", "发现设备: ${device.name}, ${device.address}")
}

// 停止扫描
// bleManager.stopScan()
```

### 5.2 连接设备

```kotlin
val gattCallback = BleGattCallback(
    onConnected = { device ->
        Log.d("BLE", "设备已连接: ${device.name}")
        // 连接成功后，可进行服务发现和数据交互
    },
    onDisconnected = { device ->
        Log.d("BLE", "设备已断开: ${device.name}")
    },
    onServicesDiscovered = { device, services ->
        Log.d("BLE", "服务已发现: ${services.size}")
        // 处理发现的服务
        services.forEach { service ->
            Log.d("BLE", "服务: ${service.uuid}")
            service.characteristics.forEach {
                Log.d("BLE", "特征值: ${it.uuid}")
            }
        }
    },
    onDataReceived = { device, data ->
        Log.d("BLE", "收到数据: ${data.size} bytes")
        // 解析数据
        val parser = BleDataParser()
        val measurementData = parser.parseData(data)
        // 处理解析后的数据
    },
    onMtuChanged = { device, mtu ->
        Log.d("BLE", "MTU已变更: $mtu")
    }
)

// 连接设备
bleManager.connect(device, gattCallback)
```

### 5.3 读写数据

```kotlin
// 查找特征值
fun findCharacteristic(device: BleDevice, serviceUuid: UUID, charUuid: UUID): BluetoothGattCharacteristic? {
    return device.services?.find { it.uuid == serviceUuid }?.characteristics?.find { it.uuid == charUuid }
}

// 读取数据
val characteristic = findCharacteristic(device, SERVICE_UUID, READ_CHAR_UUID)
characteristic?.let { device.readCharacteristic(it) }

// 写入数据
val writeCharacteristic = findCharacteristic(device, SERVICE_UUID, WRITE_CHAR_UUID)
writeCharacteristic?.let { 
    val data = byteArrayOf(0x01, 0x02, 0x03) // 示例数据
    device.writeCharacteristic(it, data)
}

// 订阅通知
val notifyCharacteristic = findCharacteristic(device, SERVICE_UUID, NOTIFY_CHAR_UUID)
notifyCharacteristic?.let { device.setCharacteristicNotification(it, true) }
```

## 6. 错误处理与异常情况

### 6.1 常见错误处理

| 错误类型 | 处理策略 |
|---------|----------|
| 蓝牙不可用 | 提示用户开启蓝牙 |
| 权限不足 | 请求相应权限 |
| 连接失败 | 重试连接，最多3次 |
| 服务发现失败 | 断开重连 |
| 数据解析错误 | 记录错误，尝试重新获取 |
| MTU协商失败 | 使用默认MTU |

### 6.2 断线重连机制

```kotlin
class BleReconnectManager(private val bleManager: BleManager, private val device: BleDevice) {
    private var reconnectCount = 0
    private val maxReconnectAttempts = 3
    private val reconnectDelay = 5000L // 5秒
    
    fun startReconnect() {
        if (reconnectCount < maxReconnectAttempts) {
            reconnectCount++
            Handler(Looper.getMainLooper()).postDelayed({
                Log.d("BLE", "尝试重连，次数: $reconnectCount")
                bleManager.connect(device, createReconnectCallback())
            }, reconnectDelay)
        } else {
            Log.d("BLE", "重连失败，已达到最大尝试次数")
        }
    }
    
    private fun createReconnectCallback(): BleGattCallback {
        return BleGattCallback(
            onConnected = { 
                Log.d("BLE", "重连成功")
                reconnectCount = 0
            },
            onDisconnected = { 
                Log.d("BLE", "重连后断开，再次尝试")
                startReconnect()
            },
            onServicesDiscovered = { _, _ -> },
            onDataReceived = { _, _ -> },
            onMtuChanged = { _, _ -> }
        )
    }
}
```

## 7. 性能优化

### 7.1 扫描优化

- 使用过滤条件减少扫描结果
- 设置合理的扫描周期和间隔
- 扫描完成后及时停止

### 7.2 连接优化

- 实现连接池管理多个设备
- 使用MTU协商提高传输效率
- 合理处理连接状态，避免频繁重连

### 7.3 电量优化

- 使用低功耗扫描模式
- 后台同步使用前台服务
- 非必要时断开蓝牙连接

## 8. 测试策略

### 8.1 单元测试

- 测试BleDataParser的数据解析逻辑
- 测试BleOperationQueue的队列管理
- 测试权限处理逻辑

### 8.2 集成测试

- 测试设备扫描与发现
- 测试设备连接与断开
- 测试数据传输与解析
- 测试MTU协商
- 测试断线重连

### 8.3 兼容性测试

- 在不同Android版本上测试
- 在不同厂商设备上测试
- 测试不同网络环境下的性能

## 9. 结论

BLE蓝牙通信模块是肾功能监测设备Android应用的关键组件，通过合理的架构设计和实现，可以确保应用与设备之间的稳定通信。本设计考虑了各种异常情况和性能优化策略，为应用提供了可靠的蓝牙通信能力。在实际开发中，还需要根据具体设备的协议和特性进行调整和优化，以确保最佳的用户体验。