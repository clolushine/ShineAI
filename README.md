# ✨ ShineAI：您的智能JetBrains IDEA AI 助手 (Your Smart JetBrains IDEA AI Assistant) 🚀

![GitHub Stars](https://img.shields.io/github/stars/clolushine/ShineAI?style=social)
![GitHub Forks](https://img.shields.io/github/forks/clolushine/ShineAI?style=social)
![License](https://img.shields.io/github/license/clolushine/ShineAI)
![IDEA Compatibility](https://img.shields.io/badge/JetBrains%20IDEs%20Version-222.2680.4%2B-blue)

---

### 💡 ShineAI 是什么？ (What is ShineAI?)

ShineAI 是一款强大的 IntelliJ IDEA 插件，旨在通过集成 AI 功能，极大地提升您的开发效率。它将主流 AI 模型的强大能力直接引入您的 IDE，提供全面的文本生成、聊天等功能。无需频繁切换应用——在您编写代码时即可获得即时 AI 协助！

**主要亮点 (Key Highlights)：**

*   **⚡️ 原生性能 (Native Performance)：** 采用 Java Swing 开发，包体积小，内存占用低，运行更流畅。**没有使用嵌入式浏览器**，因此启动更快，资源消耗更少！
*   **🌐 多AI服务商支持 (Multi-AI Provider Support)：** 集成了 OpenAI、Google AI、Anthropic AI、Cloudflare Worker AI、Groq AI 和 OpenRouter AI 等主流 AI 服务。
*   **💰 灵活高效 (Flexible & Efficient)：** 支持**多AI API Key 调用，并可设置权重**，帮助您优化不同服务商的使用成本和策略。
*   **📚 丰富的显示效果 (Rich Content Display)：** AI 回复已支持**代码块复制**、**表格渲染**、**数学公式渲染**。（代码高亮功能目前暂无法实现）
*   **🔍 智能功能 (Smart Features)：** AI 聊天、流式输出、图片与附件支持及预览、**聊天内容检索**，以及强大的提示词管理等。

---

### 📢 重要：关于后端 API 服务信息 (Important: Backend API Service Information)

ShineAI 的核心功能，包括用户登录、聊天内容存储、图片/附件处理以及与AI服务商的集成，都依赖于一个由我个人维护的**非开源后端API服务**。

*   **后端状态 (Backend Status)：** 本项目的后端代码**未开源**。
*   **数据处理 (Data Handling)：** 后端会处理并存储您的聊天内容、图片和附件，以支持聊天历史记录和多设备访问等功能。
*   **隐私与规模声明 (Privacy & Scale Disclaimer)：**
  *   **个人项目性质 (Personal Project Nature)**：此后端API服务由我个人开发和维护，主要面向**个人、非商业、实验或体验性质的使用。** 它并非意在提供商业服务。
  *   **不支持商业规模 (Not for Commercial Scale)**：**由于我的服务器资源和精力有限，它无法支撑商业规模、高并发量或企业级工作负载。** 大规模商业使用可能导致性能下降、服务中断，甚至被限制访问。
  *   **隐私与数据安全 (Privacy & Data Security)**：作为个人项目，我已尽力采取措施保护您的数据，但**无法提供企业级的服务等级协议（SLA）或100%的数据安全保障**。**我强烈建议您避免上传高度敏感的个人信息、机密商业数据或任何专有信息到此API服务。**
  *   **商业需求建议 (Recommendation for Commercial Needs)**：如果贵公司或组织需要高可用、可扩展或定制的AI集成，**我强烈建议并鼓励您根据（开源的）前端插件自行实现后端解决方案**，并直接与AI服务提供商集成。这能确保您对数据的完全控制。

**使用本插件及其关联的API服务，即表示您已阅读、理解并同意我的[隐私政策与使用协议](https://ai.5205203.xyz/#/pages/login/agreement/index)。**

---

### 🛠️ 安装与使用 (Installation & Usage)

1.  **下载插件 (Download the Plugin)：** 您可以从项目的 [**Releases (发布)**](https://github.com/clolushine/ShineAI/releases) 页面下载插件的 `zip` 或 `jar` 文件，**或通过源码自行构建。**
2.  **打开插件设置 (Open Plugin Settings)：** 在 IntelliJ IDEA 中，打开 "Settings/Preferences"（通常快捷键为 `Ctrl+Alt+S` 或 `Cmd+,`）。
3.  **选择 "Plugins" (Select "Plugins")：** 在 "Settings/Preferences" 对话框中，选择 "Plugins"。
4.  **点击齿轮图标 (Click Gear Icon)：** 在 "Plugins" 页面，点击右上角的齿轮图标（⚙️）。
5.  **选择 "Install Plugin from Disk..." (Select "Install Plugin from Disk...")：** 在弹出的菜单中，选择 "Install Plugin from Disk..."。
6.  **选择插件文件 (Select Plugin File)：** 在文件选择对话框中，选择您下载的插件 `zip` 或 `jar` 文件，然后点击 "OK"。
7.  **重启 JetBrains IDE (Restart JetBrains IDE)：** IntelliJ IDEA 会提示您重启 IDE 以使插件生效。点击 "Restart IDE" 重启即可。

---

### 🚀 主要功能一览 (Key Features Overview)

*   支持AI聊天，支持主流的AI大模型，支持AI服务商切换。
*   **支持多AI API KEYS调用，且支持权重设置。**
*   **支持AI参数配置。**
*   支持文本内容生成，支持流式输出聊天内容。
*   支持AI图文能力，图片预览功能。
*   **支持聊天内容检索。**
*   支持多提示词功能，提示词管理等。
*   支持数据记录保存，导出等。

**查看以下演示图例，了解 ShineAI 的功能！ (Check out the demos below to see ShineAI in action!)**

#### 聊天界面与核心功能 (Chat Interface & Core Functionality)
![shine ai sample](/shine_ai_sample.gif)

#### API Key 管理 (API Key Management)
![shine ai sample2](/shine_ai_sample2.gif)

---

### 💻 项目规范 (Project Specifications)

*   **语言 (Language)：** Java 17
*   **构建工具 (Build Tool)：** Gradle 8.7
*   **兼容 JetBrains IDEs 版本 (Compatible JetBrains IDEs Versions)：** 222.2680.4 - 243.*。本项目主要在 IntelliJ IDEA 上开发和测试，但理论上兼容其他支持 JVM 插件的 JetBrains IDE，如 PyCharm, WebStorm, Android Studio, Rider 等。

### 🗺️ 项目更新与路线图 (Project Updates & Roadmap)

*   我将根据功能模块的重要程度、个人时间和精力，修复Bug或进行版本更新。

---

### 🤝 贡献与支持 (Contribute & Support)

ShineAI 是一个个人项目，您的支持对我意义重大！

*   **⭐ 点亮星标 (Star this Repo)**：如果您觉得 ShineAI 有用，请给本项目一个 Star！这将是对项目极大地帮助。
*   **🐞 报告问题 (Report Bugs)**：发现 Bug？请通过提交 [Issue](https://github.com/clolushine/ShineAI/issues) 来报告。
*   **💡 提出建议 (Suggest Features)**：如果您有新的功能点子，也欢迎通过提交 [Issue](https://github.com/clolushine/ShineAI/issues) 告诉我！

**关于贡献的重要说明 (Important Note on Contributions)：**
鉴于个人精力有限以及希望保持项目**个人所有权**和**理念统一**的考量，**本项目暂不接受外部的 `Pull Request` (PR) 贡献。** 感谢您的理解和支持！

---

### ✨ UniShine for H5

UniShine 的 H5 版本地址：[https://ai.5205203.xyz/#/pages/index/index](https://ai.5205203.xyz/#/pages/index/index)

---

### 📜 许可证 (License)

本项目中的**前端插件代码**以 [**GPL 许可证**](LICENSE) 版本 2 (GNU GENERAL PUBLIC LICENSE Version 2, June 1991) 授权发布。
Copyright (C) 2016-2025 Shine Zhong

**重要许可证说明 (Important License Notes)：**

1.  **前端代码 (GPLv2)**：IntelliJ IDEA 插件的客户端代码（即前端部分）根据 GPLv2 许可证发布。此许可证旨在确保您自由使用、修改和分发插件代码本身。依据 GPLv2 许可证的条款（特别是第 6 条），您对本项目代码的任何复制、分发和修改都**必须遵循 GPLv2 许可证**，并且**不得施加任何额外的限制**。这意味着，若您遵守 GPLv2 许可证的条款，**您可以将基于本项目代码的插件发布到任何平台，包括 JetBrains Marketplace。**
2.  **后端代码 (未开源)**：驱动登录、数据存储和AI服务集成的后端代码为**专有**且**未开源**。GPLv2 仅适用于插件的前端代码，**不适用于后端服务**。
3.  **API 服务使用须知 (API Service Usage Notice)**：您对依赖我后端API服务的集成AI功能的使用，受独立的[隐私政策与使用协议](https://ai.5205203.xyz/#/pages/login/agreement/index)约束，而非 GPLv2 许可证。本协议明确了服务的非商业性质、使用限制和隐私处理方式。

请确保您在使用 ShineAI 时，理解并遵守前端插件的 GPLv2 许可证以及后端 API 服务的相关条款。