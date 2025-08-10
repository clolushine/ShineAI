# ShineAI

## 公告
该项目停止更新，本人精力有限不再维护。

### ShineAI 介绍
- 本项目是个人idea插件练习实践项目，项目不完善，不会发布到jetbrains插件市场(不商用)。
- 项目整合了市面上主流的AI服务商，项目暂只完成了文本生成这块功能。就是主流的AI聊天功能。
- 项目使用的是java swing开发前端，项目存在一些性能问题还未解决，尤其是在流式渲染时，该问题可能会影响使用。
- <strong> 项目后端未开源。</strong>
- 不同的AI服务商数据及接口整合有一定的工作量，<strong>本项目后端接入了账号登入，及涉及一些个人数据上传与保存(数据隐私问题等)，未作开源。</strong>
- 因为是个人项目，且涉及到未开源API，<strong>建议有能力者自己实现后端接口。</strong>

#### 工具版本
- 语言：java 17
- 构建工具：gradle 8.7
- 适配idea版本：222.2680.4-243.*

### 安装和使用
+ 下载插件: 首先，你需要下载插件的 zip 文件或 jar 文件。(请到releases处下载)
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
+ 支持聊天内容检索
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
本项目中的**代码**以 [**GPL 许可证**](LICENSE) 授权发布。
