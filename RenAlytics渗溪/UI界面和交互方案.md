# UI界面和交互方案

## 1. 设计理念

采用 **Material Design 3** 设计语言，结合医疗健康应用的专业性和易用性，打造清晰、直观、美观的用户界面。重点关注数据可视化、用户交互体验和动效设计，确保用户能够轻松理解和操作应用。

## 2. 技术选型

- **UI框架**：Jetpack Compose 1.4.0+
- **主题系统**：Material 3 主题
- **导航**：Jetpack Navigation
- **状态管理**：ViewModel + LiveData
- **动画**：Compose动画API
- **模糊效果**：API 31+ 使用RenderEffect，低版本使用Blurry库

## 3. 主要页面设计

### 3.1 首页仪表盘

#### 3.1.1 布局结构

```kotlin
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val latestMeasurement by viewModel.latestMeasurement.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("肾功能监测") },
                actions = {
                    IconButton(onClick = { /* 切换用户 */ }) {
                        Icon(Icons.Default.Person, contentDescription = "切换用户")
                    }
                    IconButton(onClick = { /* 设备管理 */ }) {
                        Icon(Icons.Default.Devices, contentDescription = "设备管理")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* 快速测量 */ }) {
                Icon(Icons.Default.Add, contentDescription = "快速测量")
            }
        }
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            latestMeasurement?.let { measurement ->
                DashboardContent(measurement)
            } ?: run {
                EmptyStateScreen()
            }
        }
    }
}
```

#### 3.1.2 卡片式布局

```kotlin
@Composable
fun DashboardContent(measurement: Measurement) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // eGFR卡片
        IndicatorCard(
            title = "eGFR",
            value = "${measurement.eGFR}",
            unit = "mL/min/1.73m²",
            trend = Trend.DECREASING, // 示例值
            status = getStatusForEGFR(measurement.eGFR),
            onClick = { /* 查看详情 */ }
        )
        
        // 肌酐卡片
        IndicatorCard(
            title = "血肌酐",
            value = "${measurement.creatinine}",
            unit = "μmol/L",
            trend = Trend.STABLE, // 示例值
            status = getStatusForCreatinine(measurement.creatinine),
            onClick = { /* 查看详情 */ }
        )
        
        // 尿素氮卡片
        IndicatorCard(
            title = "尿素氮",
            value = "${measurement.bun}",
            unit = "mmol/L",
            trend = Trend.INCREASING, // 示例值
            status = getStatusForBUN(measurement.bun),
            onClick = { /* 查看详情 */ }
        )
        
        // 其他指标卡片...
        
        // CKD分期卡片
        CKDStageCard(measurement.ckdStage)
        
        // 快捷入口
        QuickActions()
    }
}
```

#### 3.1.3 进度环组件

```kotlin
@Composable
fun ProgressRing(progress: Float, size: Dp = 120.dp, strokeWidth: Dp = 12.dp) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutQuad)
    )
    
    Box(
        modifier = Modifier
            .size(size)
            .wrapContentSize(Alignment.Center)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val width = size.toPx()
            val height = size.toPx()
            val radius = minOf(width, height) / 2 - strokeWidth.toPx() / 2
            
            // 背景圆环
            drawCircle(
                color = Color.LightGray,
                radius = radius,
                style = Stroke(width = strokeWidth.toPx())
            )
            
            // 进度圆环
            drawArc(
                color = Color.Green,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx())
            )
        }
        
        Text(
            text = "${(progress * 100).toInt()}%",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
```

### 3.2 详情页面

#### 3.2.1 布局结构

```kotlin
@Composable
fun DetailScreen(viewModel: DetailViewModel, indicatorType: IndicatorType) {
    val measurements by viewModel.getMeasurementsByType(indicatorType).collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(getIndicatorTitle(indicatorType)) },
                navigationIcon = {
                    IconButton(onClick = { /* 返回 */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            // 图表视图
            ChartView(measurements, indicatorType)
            
            // 列表视图
            ListView(measurements, indicatorType)
            
            // CKD分期提示
            CKDStageInfo()
        }
    }
}
```

#### 3.2.2 图表视图

```kotlin
@Composable
fun ChartView(measurements: List<Measurement>, indicatorType: IndicatorType) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp)
    ) {
        when (indicatorType) {
            IndicatorType.EGFR, IndicatorType.CREATININE, IndicatorType.BUN -> {
                LineChart(measurements, indicatorType)
            }
            IndicatorType.PROTEINURIA, IndicatorType.URINE_VOLUME -> {
                BarChart(measurements, indicatorType)
            }
            else -> {
                LineChart(measurements, indicatorType)
            }
        }
    }
}
```

#### 3.2.3 列表视图

```kotlin
@Composable
fun ListView(measurements: List<Measurement>, indicatorType: IndicatorType) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
    ) {
        items(measurements) {
            MeasurementItem(it, indicatorType)
        }
    }
}

@Composable
fun MeasurementItem(measurement: Measurement, indicatorType: IndicatorType) {
    val value = getIndicatorValue(measurement, indicatorType)
    val unit = getIndicatorUnit(indicatorType)
    val status = getIndicatorStatus(measurement, indicatorType)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { /* 展开详情 */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = formatDate(measurement.timestamp))
                Text(text = formatTime(measurement.timestamp))
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$value $unit",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = getStatusColor(status)
                )
                Text(text = getStatusText(status))
            }
        }
    }
}
```

### 3.3 设备管理页面

#### 3.3.1 布局结构

```kotlin
@Composable
fun DeviceManagementScreen(viewModel: DeviceViewModel) {
    val devices by viewModel.devices.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设备管理") }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            // 已绑定设备列表
            SectionTitle("已绑定设备")
            DeviceList(devices.filter { it.isDefault || it.bondState == BluetoothDevice.BOND_BONDED })
            
            // 扫描设备
            SectionTitle("可用设备")
            if (isScanning) {
                ScanningIndicator()
            } else {
                Button(
                    onClick = { viewModel.startScan() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("扫描设备")
                }
            }
            
            // 扫描结果
            DiscoveredDeviceList(viewModel.discoveredDevices)
        }
    }
}
```

### 3.4 用户管理页面

#### 3.4.1 布局结构

```kotlin
@Composable
fun UserManagementScreen(viewModel: UserViewModel) {
    val users by viewModel.users.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("用户管理") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* 添加用户 */ }) {
                Icon(Icons.Default.Add, contentDescription = "添加用户")
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            items(users) {
                UserItem(it, viewModel)
            }
        }
    }
}

@Composable
fun UserItem(user: User, viewModel: UserViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { viewModel.setDefaultUser(user.id) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = user.name, fontWeight = FontWeight.Bold)
                Text(text = "${user.age}岁, ${user.gender}")
            }
            if (user.isDefault) {
                Chip(
                    onClick = {},
                    colors = ChipDefaults.chipColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("默认")
                }
            }
        }
    }
}
```

### 3.5 设置页面

#### 3.5.1 布局结构

```kotlin
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val notificationEnabled by viewModel.notificationEnabled.collectAsState()
    val syncInterval by viewModel.syncInterval.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            // 通知设置
            item {
                SettingSection("通知设置")
                SwitchSetting(
                    title = "启用通知",
                    checked = notificationEnabled,
                    onCheckedChange = { viewModel.setNotificationEnabled(it) }
                )
            }
            
            // 同步设置
            item {
                SettingSection("同步设置")
                ListSetting(
                    title = "同步间隔",
                    value = "${syncInterval}分钟",
                    onClick = { /* 选择同步间隔 */ }
                )
            }
            
            // 外观设置
            item {
                SettingSection("外观设置")
                ListSetting(
                    title = "主题模式",
                    value = getThemeModeText(themeMode),
                    onClick = { /* 选择主题模式 */ }
                )
            }
            
            // 关于
            item {
                SettingSection("关于")
                ListSetting(
                    title = "版本信息",
                    value = "1.0.0",
                    onClick = { /* 显示版本信息 */ }
                )
                ListSetting(
                    title = "隐私政策",
                    onClick = { /* 查看隐私政策 */ }
                )
                ListSetting(
                    title = "免责声明",
                    onClick = { /* 查看免责声明 */ }
                )
            }
        }
    }
}
```

## 4. 交互设计

### 4.1 下拉刷新

```kotlin
@Composable
fun RefreshableContent(content: @Composable () -> Unit, onRefresh: () -> Unit) {
    val refreshState = rememberSwipeRefreshState(isRefreshing = false)
    
    SwipeRefresh(
        state = refreshState,
        onRefresh = {
            refreshState.isRefreshing = true
            onRefresh()
            // 刷新完成后设置为false
        }
    ) {
        content()
    }
}
```

### 4.2 卡片旋转效果

```kotlin
@Composable
fun RotatingCard(frontContent: @Composable () -> Unit, backContent: @Composable () -> Unit) {
    val rotationState = remember { mutableStateOf(0f) }
    val isFlipped = rotationState.value > 90f
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable {
                rotationState.value = if (isFlipped) 0f else 180f
            }
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = rotationState.value
                    cameraDistance = 8 * density
                }
                .animateRotation(rotationState.value)
        ) {
            if (!isFlipped) {
                frontContent()
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = 180f
                        }
                ) {
                    backContent()
                }
            }
        }
    }
}

private fun Modifier.animateRotation(rotation: Float): Modifier {
    return this.animateTransform {
        rotationY = rotation
    }
}
```

### 4.3 栈顶Activity模糊效果

```kotlin
@Composable
fun BlurredBackground(content: @Composable () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .drawWithContent {
                    val blurEffect = RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
                    this.drawContent()
                    this.drawRect(
                        color = Color.Black,
                        renderEffect = blurEffect
                    )
                }
        ) {
            content()
        }
    } else {
        // 使用Blurry库或其他第三方库实现
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            content()
        }
    }
}
```

### 4.4 长按复制

```kotlin
@Composable
fun CopyableText(text: String) {
    var showToast by remember { mutableStateOf(false) }
    
    Text(
        text = text,
        modifier = Modifier
            .clickable {}
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    // 复制到剪贴板
                    val clipboardManager = LocalContext.current.getSystemService(ClipboardManager::class.java)
                    val clip = ClipData.newPlainText("Copied text", text)
                    clipboardManager.setPrimaryClip(clip)
                    showToast = true
                }
            )
    )
    
    if (showToast) {
        Toast.makeText(LocalContext.current, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
        showToast = false
    }
}
```

## 5. 动效设计

### 5.1 渐入渐出效果

```kotlin
@Composable
fun FadeInOutContent(visible: Boolean, content: @Composable () -> Unit) {
    val opacity by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300)
    )
    
    if (opacity > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(opacity)
        ) {
            content()
        }
    }
}
```

### 5.2 平滑过渡

```kotlin
@Composable
fun SmoothTransitionExample() {
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1.0f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Box(
        modifier = Modifier
            .scale(scale)
            .animateContentSize()
    ) {
        // 内容
    }
}
```

### 5.3 微交互

```kotlin
@Composable
fun MicroInteractionButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    val pressed by remember { mutableStateOf(false) }
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .scale(if (pressed) 0.95f else 1f)
            .animateContentSize(),
        interactionSource = remember { MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect {
                        when (it) {
                            is PressInteraction.Press -> pressed = true
                            is PressInteraction.Release -> pressed = false
                            is PressInteraction.Cancel -> pressed = false
                        }
                    }
                }
            }
    ) {
        content()
    }
}
```

## 6. 响应式设计

### 6.1 不同屏幕尺寸适配

```kotlin
@Composable
fun ResponsiveLayout(content: @Composable (Constraints) -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    BoxWithConstraints {
        content(constraints)
    }
}

@Composable
fun DashboardContent(measurement: Measurement) {
    ResponsiveLayout {
        if (it.maxWidth < 600.dp) {
            // 手机布局：单列
            Column {
                // 卡片布局
            }
        } else {
            // 平板布局：双列或三列
            Row {
                // 卡片布局
            }
        }
    }
}
```

### 6.2 深色模式支持

```kotlin
@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF5F5F5),
    error = Color(0xFFB00020)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    error = Color(0xFFCF6679)
)
```

## 7. 无障碍设计

### 7.1 语义标签

```kotlin
@Composable
fun AccessibleCard(title: String, value: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .semantics {
                contentDescription = "$title: $value, $description"
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = title)
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(text = description, fontSize = 12.sp, color = Color.Gray)
        }
    }
}
```

### 7.2 键盘导航

```kotlin
@Composable
fun KeyboardNavigationExample() {
    val focusManager = LocalFocusManager.current
    val (firstFocus, secondFocus, thirdFocus) = remember { Triple(
        FocusRequester(),
        FocusRequester(),
        FocusRequester()
    ) }
    
    Column {
        TextField(
            value = text1,
            onValueChange = { text1 = it },
            label = { Text("First") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(firstFocus)
                .onKeyEvent {
                    if (it.key == Key.Tab && !it.isShiftPressed) {
                        secondFocus.requestFocus()
                        true
                    } else {
                        false
                    }
                }
        )
        // 其他TextField
    }
}
```

## 8. 性能优化

### 8.1 列表性能

```kotlin
@Composable
fun OptimizedList(items: List<Measurement>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = items,
            key = { it.id }
        ) {
            MeasurementItem(it)
        }
    }
}
```

### 8.2 记忆化组件

```kotlin
@Composable
fun RememberedComponent(data: Measurement) {
    val formattedValue = remember(data.value) {
        // 计算密集型操作
        formatValue(data.value)
    }
    
    Text(text = formattedValue)
}
```

### 8.3 协程与状态管理

```kotlin
@Composable
fun CoroutineExample() {
    val viewModel = viewModel<DataViewModel>()
    val data by viewModel.data.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
    
    // 显示数据
}

class DataViewModel : ViewModel() {
    private val _data = MutableStateFlow<List<Measurement>>(emptyList())
    val data: StateFlow<List<Measurement>> = _data
    
    fun loadData() {
        viewModelScope.launch {
            // 加载数据
            _data.value = repository.getMeasurements()
        }
    }
}
```

## 9. 结论

UI界面和交互方案采用现代化的Jetpack Compose框架，结合Material Design 3设计语言，打造了清晰、直观、美观的用户界面。通过精心设计的交互效果和动效，提升了用户体验，使应用更加专业和易用。同时，考虑了响应式设计、无障碍设计和性能优化，确保应用在不同设备上都能提供良好的用户体验。在实际开发中，还需要根据具体需求和用户反馈，对界面和交互进行进一步的调整和优化。