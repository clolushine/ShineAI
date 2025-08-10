
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

fun <T> Property<T>.set(it: String) = it

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij") version "1.17.4"
    // javafx
//    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
repositories {
    maven {
        setUrl("https://maven.aliyun.com/nexus/content/groups/public/")
        setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    mavenCentral()
    gradlePluginPortal()
}

// javafx {
//     version = "21.0.8"
//     modules = listOf("javafx.controls","javafx.web","javafx.swing")
// }

var graalVMVersion: String = "23.1.5";

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("cn.hutool:hutool-http:5.8.12")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.10.0")


    // implementation("com.vladsch.flexmark:flexmark-all:0.62.2")
    // flexmark 按需引入：
    implementation("com.vladsch.flexmark:flexmark:0.62.2") // 核心解析器和渲染器
    implementation("com.vladsch.flexmark:flexmark-util:0.62.2") // 包含 MutableDataSet, Node, Extension 等基础工具类
    implementation("com.vladsch.flexmark:flexmark-ext-tables:0.62.2") // 表格扩展
    implementation("com.vladsch.flexmark:flexmark-ext-typographic:0.62.2") // 排版扩展
    implementation("com.vladsch.flexmark:flexmark-ext-emoji:0.62.2") // Emoji 表情扩展

    implementation("net.coobird:thumbnailator:0.4.8")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    // 数学公式相关
    implementation("org.scilab.forge:jlatexmath:1.0.7")
    implementation("org.apache.commons:commons-text:1.10.0")

//    implementation("com.fifesoft:rsyntaxtextarea:3.5.1")
//    implementation("org.graalvm.polyglot:polyglot:${graalVMVersion}")
//    implementation("org.graalvm.polyglot:js:${graalVMVersion}")
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

tasks {
    // Set the JVM compatibility versions
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> {
            compilerOptions {
                jvmTarget.set(it)
            }
        }
    }


    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))
    }

    runPluginVerifier {
        downloadDir.set("D:/env/.pluginVerifier/ides")
    }

    publishPlugin {
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf("stable"))
    }
}