
---

# <p align="center">🚀 ShineAI</p>

<p align="center">
<strong> JetBrains IDEA AI 助手 (JetBrains IDEA AI Assistant)</strong>
</p>

<p align="center">
<img src="https://img.shields.io/github/stars/clolushine/ShineAI?style=social" alt="GitHub Stars"> 
<img src="https://img.shields.io/github/forks/clolushine/ShineAI?style=social" alt="GitHub Forks"> 
<img src="https://img.shields.io/github/license/clolushine/ShineAI" alt="License"> 
<img src="https://img.shields.io/badge/JetBrains%20IDEs%20Version-222.2680.4%2B-blue" alt="IDEA Compatibility">
</p>

---

### 💡 项目简介 (What is ShineAI?)

ShineAI 是一款强大的 IntelliJ IDEA 插件，旨在通过集成 AI 功能，极大地提升您的开发效率。它将主流 AI 模型的强大能力直接引入您的 IDE，提供全面的文本生成、聊天等功能。无需频繁切换应用——在您编写代码时即可获得即时 AI 协助！

#### ✨ 核心亮点 (Key Highlights)

* **⚡️ 原生性能 (Native Performance)：** 采用 Java Swing 开发，包体积小，内存占用低，运行更流畅。**没有使用嵌入浏览器**，因此启动更快，资源消耗更少！
* **🌐 多 AI 服务商支持：** 集成了 OpenAI、Google AI、Claude AI 等主流 AI 服务。
* **💰 灵活高效：** 支持**多 AI API Key 调用，并可设置权重**，帮助您优化不同服务商的使用成本和策略。
* **📚 丰富的显示效果：** 支持**主题切换**，已支持**代码块复制**、**表格渲染**、**数学公式渲染**。（代码高亮功能当前框架下无法实现）
* **🔍 智能功能：** AI 聊天、流式输出、图片与附件支持及预览、**聊天内容检索**，以及强大的提示词管理等。

---

### 🚀 功能演示 (Demos)

#### 💬 聊天界面与核心功能
![shine ai sample](/shine_ai_sample.gif)

#### 🔑 API Key 管理
![shine ai sample2](/shine_ai_sample2.gif)

---

### 📢 重要：关于后端 API 服务信息 (Important Notice)

> [!IMPORTANT]
> ShineAI 的核心功能依赖于我个人维护的**非开源后端 API 服务**。

* **后端状态：** 本项目的后端代码**未开源**。
* **数据处理：** 后端会处理并存储您的聊天内容、图片和附件，以支持聊天历史记录和多设备访问。
* **隐私与规模声明：**
* **个人项目性质**：此服务由我个人开发维护，主要面向**个人、非商业、实验或体验性质的使用**。
* **不支持商业规模**：**由于服务器资源有限，无法支撑商业规模、高并发量或企业级工作负载**。大规模使用可能导致服务中断或访问受限。
* **隐私与数据安全**：作为个人项目，已尽力保护数据，但**无法提供企业级 SLA 或 100% 数据安全保障**。**强烈建议避免上传高度敏感、机密商业数据或专有信息。**
* **商业需求建议**：如果需要高可用、可扩展或定制集成，**强烈建议根据前端插件自行实现后端解决方案**。



**使用本插件及其关联服务，即表示您已同意：** [隐私政策与使用协议](https://ai.5205203.xyz/#/pages/login/agreement/index)

---

### 🛠️ 安装与使用 (Installation & Usage)

1. **下载插件：** 从 **[Releases](https://github.com/clolushine/ShineAI/releases)** 页面下载 `zip` 或 `jar` 文件（或自行构建）。
2. **设置路径：** 打开 IntelliJ IDEA 的 `Settings/Preferences` (快捷键 `Ctrl+Alt+S` 或 `Cmd+,`)。
3. **插件管理：** 选择左侧 `Plugins` 菜单。
4. **从本地安装：** 点击右上角齿轮图标（⚙️），选择 `Install Plugin from Disk...`。
5. **选择文件：** 选择下载的插件文件并确认。
6. **重启生效：** 点击 `Restart IDE` 重启 IntelliJ IDEA。

---

### 🔍 功能一览 (Features)

* **智能交互：** 支持流式输出、AI 服务商切换、AI 参数配置。
* **API 管理：** **支持多 API KEYS 调用及权重设置。**
* **多媒体支持：** 支持图文能力、图片预览、附件处理。
* **效率工具：** **支持聊天内容检索**、多提示词管理、数据记录保存与导出。

#### 🌐 已支持的 AI 服务商

* OpenAI / Google AI / Anthropic AI
* Cloudflare Worker AI / Groq AI / OpenRouter AI

---

### 💻 项目规范 (Project Specifications)

* **开发语言：** Java 17
* **构建工具：** Gradle 8.7
* **兼容版本：** JetBrains IDEs 222.2680.4 - 243.*
* **兼容范围：** 本项目主要在 IntelliJ IDEA 开发测试，理论兼容 PyCharm, WebStorm, Android Studio, Rider 等支持 JVM 插件的 IDE。

---

### 🤝 贡献与支持 (Contribute & Support)

* **⭐ 点亮星标：** 如果您觉得 ShineAI 有用，请给本项目一个 Star！
* **🐞 报告问题：** 发现 Bug？请提交 [Issue](https://github.com/clolushine/ShineAI/issues)。
* **💡 提出建议：** 有新功能点子？欢迎通过 [Issue](https://github.com/clolushine/ShineAI/issues) 告诉开发者。

> **关于贡献的重要说明：**
> 鉴于个人精力有限及保持理念统一的考量，**本项目暂不接受外部 Pull Request (PR) 贡献**。感谢您的理解！

---

### ✨ 相关链接 (Related Links)

* **UniShine H5 版本：** [点击访问](https://ai.5205203.xyz/#/pages/index/index)
* **开源许可证：** 本前端代码以 **[GPL-2.0](LICENSE)** 授权发布。

---

<p align="center">Copyright (C) 2016-2026 Shine Zhong</p>
