<div align="center">

# 🛠️ NaiTool

**Minecraft 生存辅助客户端模组**

[![Minecraft](https://img.shields.io/badge/Minecraft-26.2-green.svg)](https://www.minecraft.net/)
[![Fabric Loader](https://img.shields.io/badge/Fabric_Loader-0.19.5-blue.svg)](https://fabricmc.net/)
[![MaLiLib](https://img.shields.io/badge/MaLiLib-0.29.3-orange.svg)](https://github.com/maruohon/malilib)
[![Java](https://img.shields.io/badge/Java-25-red.svg)]()
[![License](https://img.shields.io/badge/License-GPL--3.0-purple.svg)](LICENSE)

</div>

---

## 📖 项目介绍

NaiTool 是一个基于 **Fabric** 的 Minecraft 客户端生存辅助模组（Utility Mod），使用 **MaLiLib** 框架开发。

本模组专注于提供实用的生存辅助功能，通过 Mixin 注入实现轻量级、低侵入的游戏增强体验。所有功能均可在游戏内通过 GUI 界面自由配置开关与快捷键绑定。

> 当前适配版本：**Minecraft 26.2** · **Fabric Loader 0.19.5** · **Java 25**

---

## ✨ 功能列表

### 功能模块

| 功能 | 说明 |
| :--- | :--- |
| 🎆 **鞘翅加速**（ElytraBoost） | 滑翔时使用烟花加速，支持防消耗模式、自定义烟花飞行时长、音效开关 |
| 🌈 **鞘翅尾迹**（ElyTrails） | 滑翔时生成炫酷粒子尾迹，支持 5 种模式、自定义颜色/密度/扩散、烟花隐藏、视角优化 |
| 👁️ **夜视模式**（NightVision） | 提供 Gamma 和药水两种夜视模式，带总开关，点击即可循环切换模式 |
| 🏃 **自动冲刺**（Sprint） | 自动保持冲刺状态，支持严格模式（仅前方）和自由模式（全方向） |

### 鞘翅尾迹模式

| 模式 | 效果 | 说明 |
| :--- | :--- | :--- |
| 🟢 **普通** | 单色贝塞尔曲线尾迹 | 经典单线，颜色增亮 |
| 🌈 **彩虹** | 颜色随时间+位置渐变 | 全彩虹色循环，带波动效果 |
| 🌀 **螺旋** | 3 股螺旋缠绕 | 3 条螺旋线绕飞行方向旋转，各自彩虹渐变 |
| ✨ **多线** | 5 条平行彩虹线 | 5 条等距平行线，每条颜色偏移 |
| 🦋 **翅膀** | 两侧翼尖拖尾 | 左右翼尖各一条彩虹尾迹，带拍打动画 + 中线增强 |

**尾迹附加功能**：
- 🔇 **隐藏烟花特效** — 尾迹开启时自动过滤烟花白色粒子，推力不受影响
- 📐 **尾迹下移** — 可调节 Y 轴偏移，避免第一人称视角下尾迹遮挡视野

### 快捷键

| 快捷键 | 功能 |
| :--- | :--- |
| `X` + `N` | 打开设置界面 |
| `F` | 鞘翅加速（滑翔时按下触发） |

> 所有快捷键均可在设置界面中自定义修改。

---

## 🔨 编译构建

### 环境要求

| 依赖 | 版本要求 |
| :--- | :--- |
| JDK | **25** 或更高 |
| Gradle | 项目自带 Gradle Wrapper（无需手动安装） |

### 构建步骤
bash
1. 克隆仓库
   git clone https://github.com/DouNai996/NaiTool.git cd NaiTool
2. 编译构建（Windows）
   gradlew.bat build
2. 编译构建（Linux / macOS）
   ./gradlew build
3. 构建产物位于 `build/libs/` 目录下，选择带 `-dev` 后缀以外的 JAR 文件即可。

---

## 📦 安装与使用

### 前置依赖

在安装 NaiTool 之前，请确保已安装以下依赖：

| 依赖 | 说明 |
| :--- | :--- |
| [Fabric Loader](https://fabricmc.net/use/) | ≥ 0.19.5 |
| [Fabric API](https://modrinth.com/mod/fabric-api) | 与 Minecraft 版本匹配的最新版 |
| [MaLiLib](https://github.com/maruohon/malilib/releases) | ≥ 0.29.3（选择对应 MC 版本的 Fabric 版） |

### 安装步骤

1. 安装 [Fabric Loader](https://fabricmc.net/use/) 并启动一次 Minecraft 以生成模组目录
2. 将以下文件放入 `.minecraft/mods/` 目录：
   - `fabric-api-*.jar`
   - `malilib-fabric-*.jar`
   - `naitool-*.jar`（本项目编译产物）
3. 启动 Minecraft（使用 Fabric 配置文件）

### 游戏内使用

- 按 `X` + `N` 打开 NaiTool 设置界面
- 在设置界面中开关功能、调整参数、绑定快捷键
- 配置会自动保存至 `.minecraft/config/naitool.json`

---

## ⚙️ 配置说明

NaiTool 使用 MaLiLib 提供的 GUI 配置系统，设置界面包含三个标签页：

| 标签页 | 内容 |
| :--- | :--- |
| **全部** | 同时展示功能配置与快捷键设置 |
| **功能** | 所有功能模块的开关与参数调整 |
| **快捷键** | 快捷键绑定与修改 |

### 鞘翅尾迹配置项

| 配置项 | 说明 | 默认值 |
| :--- | :--- | :--- |
| 鞘翅尾迹 | 功能总开关 | `false` |
| 尾迹模式 | 普通 / 彩虹 / 螺旋 / 多线 / 翅膀 | `彩虹` |
| 尾迹颜色 | 基础颜色（RGB 十六进制），彩虹模式下自动覆盖 | `0x55FF55` |
| 尾迹持续时间 | 粒子显示时长（刻） | `120` |
| 尾迹扩散 | 扩散范围，影响各模式宽度和随机性 | `1.0` |
| 尾迹密度 | 每 tick 粒子数量 | `30` |
| 隐藏烟花特效 | 尾迹开启时过滤烟花白色粒子 | `true` |
| 尾迹下移距离 | Y 轴偏移量，避免遮挡视角 | `0.5` |

---

## 🙏 致谢

- [**MaLiLib**](https://github.com/maruohon/malilib) — 提供强大的配置、GUI 与快捷键框架，由 maruohon 开发维护
- [**Fabric**](https://fabricmc.net/) — 轻量级模组加载器与 API 生态
- [**Elytra Trails**](https://github.com/DreamFall-Studio/elytra-trails) — 鞘翅尾迹功能的灵感来源与贝塞尔曲线粒子插值算法参考

---

<div align="center">

**NaiTool** · Made with ❤️ by DouNai996!

</div>