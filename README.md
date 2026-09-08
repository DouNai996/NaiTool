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
| 👁️ **夜视模式**（NightVision） | 提供 Gamma 和药水两种夜视模式，带总开关，点击即可循环切换模式 |
| 🏃 **自动冲刺**（Sprint） | 自动保持冲刺状态，支持严格模式（仅前方）和自由模式（全方向） |

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

---

## 🙏 致谢

- [**MaLiLib**](https://github.com/maruohon/malilib) — 提供强大的配置、GUI 与快捷键框架，由 maruohon 开发维护
- [**Fabric**](https://fabricmc.net/) — 轻量级模组加载器与 API 生态

---

<div align="center">

**NaiTool** · Made with ❤️ by DouNai996!

</div>