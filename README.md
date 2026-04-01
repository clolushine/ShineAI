---

# <p align="center">🚀 ShineAI</p>

<p align="center">
<strong> JetBrains IDEA AI 助手 (JetBrains IDEA AI Assistant)</strong>
</p>

<p align="center">
<img src="https://img.shields.io/github/stars/clolushine/ShineAI?style=social" alt="GitHub Stars">
<img src="https://img.shields.io/github/forks/clolushine/ShineAI?style=social" alt="GitHub Forks">
<img src="https://img.shields.io/github/license/clolushine/ShineAI" alt="Licnese">
<img src="https://img.shields.io/badge/JetBrains%20IDEs%20Version-222.2680.4%2B-blue" alt="IDEA Compatibility">
</p>

---

***<p align="center">📢 [公告] JetBrains IDEA 插件版本已停止维护。如需可迁移至功能更强大、跨平台支持的全新版本：[点击前往](https://github.com/clolushine/Next-ShineAI-Releases/releases)</p>***

---

### 💡 项目简介 (What is ShineAI?)

ShineAI 是一款易用的 IntelliJ IDEA 插件，旨在通过集成 AI 功能，提升您的开发效率。它将主流 AI 模型的直接引入您的 IDE，提供全面的文本生成、聊天等功能。无需频繁切换应用——在您编写代码时即可获得即时 AI 协助！

#### ✨ 核心亮点 (Key Highlights)

*   **⚡️ 原生性能 (Native Performance)：** 采用原生 Java Swing 开发，包体积小，内存占用低，运行更流畅。**没有使用嵌入浏览器**，软件包不臃肿，资源消耗更少！
*   **🌐 多 AI 服务商支持：** 集成了 OpenAI、Google AI、Claude AI 等主流 AI 服务。
*   **💰 灵活高效：** 支持**多 AI API Key 调用，并可设置权重**，帮助您优化不同服务商的使用成本和策略。
*   **📚 丰富的显示效果：** 支持**主题切换**，已支持**代码块复制**、**表格渲染**、**数学公式渲染**。
*   **🔍 智能功能：** AI 聊天、流式输出、图片与附件支持及预览、**聊天内容检索**，以及强大的提示词管理等。

---

### 🚀 功能演示 (Demos)

#### 💬 聊天界面与核心功能

![shine ai sample](/shine_ai_sample.gif)

#### 🔑 API Key 管理

![shine ai sample2](/shine_ai_sample2.gif)

---

### 📢 重要：关于后端 API 服务信息 (Important Notice)

> [!CAUTION]
> **服务终止通知**
> **ShineAI 的后端 API 服务现已正式关停。** 本插件依赖的后端服务已停止运行，由于插件核心功能强依赖于该后端，**插件已无法连接至 AI 服务，目前已无法正常使用。**

*   **项目现状**：作为个人开发项目，ShineAI 的配套 API 服务资源已于近期正式下线，不再提供任何后端支持。
*   **隐私与数据**：鉴于服务已终止，所有历史存储在后端的数据（如有）均已清理。
*   **替代方案**：如您仍有 AI 辅助开发需求，请迁移至我们的全新跨平台版本：[👉 点击访问全新系统](https://5205203.xyz/)。

*   **开源性质**：ShineAI 前端代码保持开源（[Apache 2.0](LICENSE)），如果您具备开发能力，仍可通过下载源码后，修改后端接口地址链接至您自行部署的服务进行二次开发。

**使用本插件及其关联服务，即表示您已同意：** [隐私政策与使用协议](https://ai.5205203.xyz/#/pages/login/agreement/index)

---

### 🛠️ 安装与使用 (Installation & Usage)

1.  **下载插件：** 从 **[Releases](https://github.com/clolushine/ShineAI/releases)** 页面下载 `zip` 或 `jar` 文件（或自行构建）。
2.  **设置路径：** 打开 IntelliJ IDEA 的 `Settings/Preferences` (快捷键 `Ctrl+Alt+S` 或 `Cmd+,`)。
3.  **插件管理：** 选择左侧 `Plugins` 菜单。
4.  **从本地安装：** 点击右上角齿轮图标（⚙️），选择 `Install Plugin from Disk...`。
5.  **选择文件：** 选择下载的插件文件并确认。
6.  **重启生效：** 点击 `Restart IDE` 重启 IntelliJ IDEA。

---

### 🔍 功能一览 (Features)

*   **智能交互：** 支持流式输出、AI 服务商切换、AI 参数配置。
*   **API 管理：** **支持多 API KEYS 调用及权重设置。**
*   **多媒体支持：** 支持图文能力、图片预览、附件处理。
*   **效率工具：** **支持聊天内容检索**、多提示词管理、数据记录保存与导出。

#### 🌐 已支持的 AI 服务商

*   OpenAI / Google AI / Anthropic AI
*   Cloudflare Worker AI / Groq AI / OpenRouter AI
*   DeepSeek AI / Nvidia AI

---

### ⚠️ 第三方库与许可证 (Third-Party Libraries & Licenses)

ShineAI 项目利用了以下第三方库以提供增强功能：

#### JLaTeXMath

本项目使用 [JLaTeXMath 库](https://github.com/opencollab/jlatexmath) 进行数学公式渲染。JLaTeXMath 根据 **GNU General Public License Version 2 或更高版本 (GPL-2.0-or-later)** 发布，并包含一个**特殊链接例外 (Special Linking Exception)**。

**关于链接例外：** 此例外允许 ShineAI（一个 Apache 2.0 许可的项目）链接和使用 JLaTeXMath 库，而不会导致 ShineAI 的整体代码被 GPL 传染。 这意味着您的 ShineAI 代码仍保持 Apache 2.0 许可证。然而，您仍需遵守 GPL 的以下义务：

*   当您分发 ShineAI 时，必须提供 JLaTeXMath 库的完整源代码。
*   如果您（或任何使用者）对 JLaTeXMath 库本身进行了修改，这些修改后的 JLaTeXMath 代码也必须在 GPL-2.0-or-later 许可下发布。

您可以访问 [JLaTeXMath 的 GitHub 仓库](https://github.com/opencollab/jlatexmath) 获取其完整的许可证文本和源代码。

#### JLaTeXMath 所用字体许可证

JLaTeXMath 库中包含了多种字体，它们各自拥有不同的许可证：

*   **OFL (Open Font License):** `eufb10.ttf`, `eufm10.ttf`, `msam10.ttf`, `msbm10.ttf`
*   **自由协议 (Free License):** `dsrom10.ttf`
*   **公共领域 (Public Domain - PD):** `stmary10.ttf`, `rsfs10.ttf`
*   **Knuth License:** `cmbsy10.ttf`, `cmbx10.ttf`, `cmbxti10.ttf`, `cmex10.ttf`, `cmmi10.ttf`, `cmr10.ttf`, `cmss10.ttf`, `cmssbx10.ttf`, `cmsy10.ttf`, `cmtt10.ttf` 以及所有西里尔 (Cyrillic) 字体 (`wnbx10.ttf` 等)
*   **GNU GPL version 2 (GPL-2.0):** 希腊语 (Greek) 字体 (`fcmbipg.ttf`, `fcmripg.tff`, `fcmrpg.ttf` 等)

您可以在 JLaTeXMath 库的 `org/scilab/forge/jlatexmath/fonts/licences` 路径下找到这些字体的具体许可证文本。

---

### 💻 项目规范 (Project Specifications)

*   **开发语言：** Java 17
*   **构建工具：** Gradle 8.7
*   **兼容版本：** JetBrains IDEs 222.2680.4 - 243.*
*   **兼容范围：** 本项目主要在 IntelliJ IDEA 开发测试，理论兼容 PyCharm, WebStorm, Android Studio, Rider 等支持 JVM 插件的 IDE。

---

### 🤝 贡献与支持 (Contribute & Support)

*   **⭐ 点亮星标：** 如果您觉得 ShineAI 有用，请给本项目一个 Star！
*   **🐞 报告问题：** 发现 Bug？本项目不再维护了，可自行clone/fork解决。
*   **💡 提出建议：** 有新功能点子？本项目不再维护了，可自行clone/fork添加功能。

> **关于贡献的重要说明：**
> 鉴于个人精力有限及保持理念统一的考量，**本项目不接受外部 Pull Request (PR) 贡献**。感谢您的理解！

---

### ✨ 相关链接 (Related Links)

*   **全平台网页版本：** [点击访问](https://5205203.xyz/)
*   **全平台桌面端版本：** [点击访问](https://github.com/clolushine/Next-ShineAI-Releases/releases)
*   **开源许可证：** 本前端代码以 **[Apache 2.0](LICENSE)** 授权发布。

---

<p align="center">Copyright (C) 2024-2026 Shine Zhong</p>
