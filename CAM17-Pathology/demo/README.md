# CAMELYON17 MIL DEMO

## 项目简介

本项目实现了一个基于注意力机制的多实例学习（MIL）系统，用于处理和分析CAMELYON17病理切片数据。系统完整覆盖了从WSI读取到注意力可视化的全过程。

## 技术流程

```
WSI (slide-level label only)
        ↓
Patch extraction (256x256, 20X)
        ↓
ResNet50 特征编码
        ↓
MIL 注意力聚合
        ↓
Slide-level 预测
        ↓
注意力热力图生成
```

## 快速开始

### 方法一：一键运行

Windows：
```bash
run_demo.bat
```

Linux/Mac：
```bash
chmod +x demo.sh && ./demo.sh
```

### 方法二：手动运行

1. 环境设置：
   ```bash
   python setup_env.py
   ```

2. 运行完整演示：
   ```bash
   python run_demo.py
   ```

## 详细使用指南

### 1. 环境要求

- Python 3.8+
- 支持CPU运行（推荐GPU加速）
- 可选安装OpenSlide

### 2. 依赖安装

```bash
pip install -r requirements.txt
```

### 3. 数据准备

使用样本数据：
DEMO环节系统会自动生成20个模拟WSI样本，无需额外操作。

使用真实CAMELYON17数据：
1. 将CAMELYON17数据放入 `data/camelyon17/` 目录
2. 创建标签文件 `data/camelyon17/labels.csv`
3. 运行：
   ```bash
   python prepare_camelyon17.py --data_dir data/camelyon17 --label_file data/camelyon17/labels.csv
   ```

### 4. 模型训练

```bash
python train.py
```

训练参数：
- 训练轮数：50个epoch
- 批量大小：1
- 学习率：1e-4
- 评估指标：准确率、AUC

### 5. 注意力可视化

```bash
python visualize_attention.py
```

生成的可视化结果：
- 顶部注意力patch
- 注意力热力图
- WSI注意力叠加图

## 项目结构

```
.
├── preprocessing/       # WSI 处理模块
│   ├── wsi_reader.py    # WSI 读取器
│   └── patch_extractor.py  # Patch 提取器
├── datasets/            # 数据集定义
│   └── mil_dataset.py   # MIL 数据集
├── models/              # 模型定义
│   ├── encoder.py       # 特征编码器
│   └── mil_attention.py  # MIL 注意力模型
├── utils/               # 工具函数
│   └── io.py            # IO 工具
├── checkpoints/         # 生成的模型权重
├── visualizations/      # 生成的可视化结果
├── data/                # 数据目录
│   └── camelyon17_processed/  # 处理后的数据
├── requirements.txt     # 依赖列表
├── setup_env.py         # 环境设置脚本
├── run_demo.py          # DEMO手动运行脚本
├── run_demo.bat         # Windows 封装运行脚本
├── demo.sh              # Linux/Mac 封装运行脚本
└── README.md            # 项目说明
```

## 输出结果

### 模型权重
- `checkpoints/best_model.pth` - 训练过程中性能最好的模型

### 可视化结果
- `visualizations/*_top_attention.png` - 顶部注意力patch可视化
- `visualizations/*_heatmap.png` - 注意力热力图
- `visualizations/*_overlay.png` - WSI注意力叠加图

### 处理后的数据
- `data/camelyon17_processed/*_bag.pkl` - 每个slide对应的bag数据
- `data/camelyon17_processed/*_split.pkl` - 训练/验证/测试数据分割

## 注意事项

1. 环境配置：
   - 系统会自动检查并安装依赖
   - 即使OpenSlide未安装，也能通过样本数据运行演示

2. 数据处理：
   - 默认生成20个模拟WSI样本
   - 支持真实CAMELYON17数据处理
   - 数据处理结果会保存在 `data/camelyon17_processed/`

3. 训练配置：
   - 训练过程会在控制台输出详细日志
   - 自动保存最佳模型权重
   - 支持CPU/GPU训练（自动检测）

4. 可视化：
   - 生成的可视化结果保存在 `visualizations/` 目录
   - 使用matplotlib生成高质量图像
   - 包含多种视角的注意力可视化

## 故障排除

### 常见问题

1. 依赖安装失败：
   - 确保Python版本为3.8+
   - 尝试使用管理员权限运行
   - 检查网络连接

2. 训练过程报错：
   - 确保数据已正确准备
   - 检查GPU内存是否足够
   - 尝试使用CPU训练（自动检测）

3. 可视化结果为空：
   - 确保模型已成功训练
   - 检查 `checkpoints/` 目录是否有模型文件
   - 尝试重新运行 `visualize_attention.py`

### 日志和调试

- 所有脚本都会在控制台输出详细日志
- 错误信息会直接显示在控制台
- 关键步骤会有明确的提示信息