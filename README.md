# 公告

该项目停止更新，本人精力有限不再维护。


# ShineAI

下载插件: 首先，你需要下载插件的 zip 文件或 jar 文件。(请到release处下载)

打开插件设置: 在 IntelliJ IDEA 中，打开 "Settings/Preferences" (快捷键通常是 Ctrl+Alt+S 或 Cmd+,).

选择 "Plugins": 在 "Settings/Preferences" 对话框中，选择 "Plugins"。

点击齿轮图标： 在 "Plugins" 页面，点击右上角的齿轮图标。

选择 "Install Plugin from Disk...": 在弹出的菜单中，选择 "Install Plugin from Disk..."。

选择插件文件： 在文件选择对话框中，选择你下载的插件 zip 文件或 jar 文件，然后点击 "OK"。

重启 IntelliJ IDEA: IntelliJ IDEA 会提示你重启 IDE 以使插件生效。 点击 "Restart IDE" 重启 IDE。


## ShineAI 介绍

本项目是个人idea插件练习实践项目，项目不完善，不会发布到jetbrains插件市场(不商用)。

项目整合了市面上主流的AI服务商，项目暂只完成了文本生成这块功能。就是主流的AI聊天功能。

项目使用的是java swing开发前端，项目存在一些性能问题还未解决，尤其是在流式渲染时，该问题可能会影响使用。

项目后端未开源，后端使用severless部署(解决API地区限制问题)。相关的接口需自己实现，
不同的AI服务商数据及接口整合需要一定的工作量，本项目后端接入了账号登入，及涉及一些个人数据上传与保存(数据隐私问题等)，不适合作开源。


## Unishine for h5 

- h5版预览地址： [https://unishine.pages.dev/#/pages/index/index](https://unishine.pages.dev/#/pages/index/index)
