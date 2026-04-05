# 10_IMPLEMENTATION_BACKLOG.md

## 说明
本文件用于描述 `v0.1` 冻结后，Codex 在不突破边界的前提下可以继续处理的事项。优先级按 P0 / P1 / P2 划分。

---

## P0：当前最优先

### P0-1 工程可编译
- 检查 `build.gradle.kts` / `settings.gradle.kts` / KSP / Compose 版本兼容
- 检查 `AndroidManifest.xml` 是否完整
- 检查 Theme / Typography / Material3 是否完整可用
- 检查 Room 相关注解、导入、数据库版本是否一致

### P0-2 主链路可运行
- 建议页可导入模拟数据
- 建议页可生成建议
- 建议列表可展示
- 首页可展示摘要
- 趋势页可展示 7 天聚合列表

### P0-3 数据一致性
- 重复导入模拟数据时避免无限累加
- 建议缓存可覆盖更新
- 首页与趋势页读到的是同一批基线数据

---

## P1：稳定性补齐

### P1-1 时间工具统一
建议抽出：
- `startOfToday()`
- `startOfDay(time)`
- `daysAgo()`
- `formatDayLabel()`

### P1-2 错误态与空态
- 首页无数据提示
- 趋势页空态提示
- 建议页生成失败提示

### P1-3 轻量测试
优先可补：
- `SuggestionFusionEngine` 单元测试
- `ImportMockDataUseCase` 数据数量测试
- `HealthDataRepositoryImpl.buildSummary()` 基础测试

---

## P2：冻结后预研，不默认执行
以下内容仅记录，不属于当前默认交付：
- 图表库接入
- Retrofit 远程 Agent
- 蓝牙设备接入
- Settings 中 Agent 开关真实生效
- WorkManager 周期刷新
- Hilt 替换手动依赖装配

---

## 不建议当前做的事
- 大规模包重构
- 切换到多模块 Gradle 工程
- 引入真实账号系统
- 引入复杂 MVI 框架
- 接入真实医学模型
