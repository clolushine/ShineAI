# ShineAI

### 介绍
- 本项目是idea插件项目。
- 项目整合了市面上主流的AI服务商，项目暂集成了文本生成这块功能。就是主流的AI聊天功能。
- 项目使用的是java swing开发前端，使用原生swing渲染，**没有使用嵌入浏览器**。
  - 由于是原生渲染，包体积和内存占用都偏小，但渲染html的性能差，且不支持现代web样式。
  - AI文档已支持 **代码块复制**，**表格渲染**，**数学公式渲染**。<em>代码高亮功能暂无法实现</em>
- **项目后端未开源。**
- **本项目后端接入了账号登录，相应的一些数据处理在后端进行的， 未作开源。**
- 项目后端会涉及聊天内容的上传及存储，图片及附件存储。
- 因为是个人项目，无法支撑商用。**强烈建议有能力者自己实现后端接口，** 不同的AI服务商数据及接口整合有一定的工作量。

### 工具版本
- 语言：java 17
- 构建工具：gradle 8.7
- 适配idea版本：222.2680.4-243.*

### 安装和使用
+ 下载插件: 首先，你需要下载插件的 zip 文件或 jar 文件。(请到releases处下载或源码构建)
+ 打开插件设置: 在 IntelliJ IDEA 中，打开 "Settings/Preferences" (快捷键通常是 Ctrl+Alt+S 或 Cmd+,).
+ 选择 "Plugins": 在 "Settings/Preferences" 对话框中，选择 "Plugins"。
+ 点击齿轮图标： 在 "Plugins" 页面，点击右上角的齿轮图标。
+ 选择 "Install Plugin from Disk...": 在弹出的菜单中，选择 "Install Plugin from Disk..."。
+ 选择插件文件： 在文件选择对话框中，选择你下载的插件 zip 文件或 jar 文件，然后点击 "OK"。
+ 重启 IntelliJ IDEA: IntelliJ IDEA 会提示你重启 IDE 以使插件生效。 点击 "Restart IDE" 重启 IDE。

### 功能和用法
+ 支持AI聊天，支持主流的AI大模型，支持AI服务商切换
+ <strong> 支持多AI API KEYS调用，且支持权重设置</strong>
+ 支持文本内容生成，支持流式输出聊天内容
+ 支持AI图文能力，图片预览功能
+ **支持聊天内容检索**
+ 支持多提示词功能，提示词管理等
+ 支持数据记录保存，导出等
+ 可以查看以下演示图例了解

#### 已支持的 AI 服务商
+ OpenAI
+ Google AI
+ Anthropic AI
+ Cloudflare Worker AI
+ Groq AI
+ OpenRouter AI

### 图例展示
![shine ai sample](/shine_ai_sample.gif)

#### API KEYS
![shine ai sample2](/shine_ai_sample2.gif)

### UniShine for h5
- h5版地址： [https://unishine.pages.dev/#/pages/index/index](https://unishine.pages.dev/#/pages/index/index)

### 许可证 (License)
本项目中的**前端代码**以 [**GPL 许可证**](LICENSE) 版本 2 (GNU GENERAL PUBLIC LICENSE Version 2, June 1991) 授权发布。
Copyright (C) 2016-2025 Shine Zhong
**重要注意事项及说明：**
1.  **关于后端：** 本项目的后端代码**未开源**。这意味着前端插件在某些功能上（如与AI服务商的数据及接口整合、用户登录、个人数据上传与保存等）可能需要一个配套的后端服务。请注意，GPL v2 许可证仅适用于本项目中已开源的前端代码部分（即 IntelliJ IDEA 插件），不适用于未开源的后端服务。
2.  **关于分发限制：** GPL v2 许可证旨在确保软件的自由使用、修改和分发。根据 GPL v2 许可证的条款（特别是第 6 条），**您对本项目代码的任何复制、分发和修改都必须遵循 GPL v2 许可证，并且不得施加任何额外的限制**。这意味着，若您遵守 GPL v2 许可证的条款，**您可以将基于本项目代码的插件发布到任何平台，包括 JetBrains Marketplace。
如果您希望在您的项目中使用此代码，请确保您理解并遵守 GPL v2 许可证的全部条款。有关 GPL v2 许可证的详细条款，请参阅本项目根目录下的 `LICENSE` 文件。
