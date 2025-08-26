// 插件配置
plugins {
    // 标记这是一个Android库，而非可安装的App
    id("com.android.library")
    kotlin("android")
    kotlin("plugin.parcelize")
    id("dev.zacsweers.moshix")  // MoshiX插件，增强Moshi JSON库的功能（如代码生成）
    id("com.google.devtools.ksp")
}

// 调用自定义函数
setupCoreLib()

ksp {
    // 向Room注解处理器传递参数，指定生成Kotlin代码（默认可能生成Java代码）
    arg("room.generateKotlin", "true")
}

android {
    // 库的命名空间，用于资源和类的隔离
    namespace = "com.fastjien.sunny.core"

    defaultConfig {
        // 生成BuildConfig类的字段，供代码中访问这些配置
        buildConfigField("String", "APP_PACKAGE_NAME", "\"com.fastjien.sunny\"")
        buildConfigField("int", "APP_VERSION_CODE", "${Config.versionCode}")
        buildConfigField("String", "APP_VERSION_NAME", "\"${Config.version}\"")
        buildConfigField("int", "STUB_VERSION", Config.stubVersion)

        // 提供给依赖此库的模块使用的混淆规则
        consumerProguardFile("proguard-rules.pro")
    }

    // 构建特性
    buildFeatures {
        // 启用AIDL（Android接口定义语言），用于跨进程通信
        aidl = true
        // 启用BuildConfig类生成（与上面的buildConfigField配合）
        buildConfig = true
    }

    compileOptions {
        // 启用核心库脱糖，支持低版本系统使用Java 8+特性
        isCoreLibraryDesugaringEnabled = true
    }

    lint {
//        disable("MissingTranslation")
        disable += "MissingTranslation"
    }
}

dependencies {
    // 以api方式依赖":shared"模块，意味着依赖此库的模块也能访问shared模块的类
    api(project(":shared"))
    // 核心库脱糖依赖，支持Java新特性
    coreLibraryDesugaring(libs.jdk.libs)

    api(libs.timber)          // 日志工具库，简化日志输出（api方式暴露给依赖模块）
    api(libs.markwon.core)    // Markdown解析库，用于处理Markdown文本（如Magisk的说明文档）
    implementation(libs.bcpkix)            // BouncyCastle的PKIX库，提供加密、证书处理功能（Magisk需要签名验证）
    implementation(libs.commons.compress)  // Apache的压缩库，支持多种压缩格式（如处理Magisk模块的压缩包）

    api(libs.libsu.core)      // libsu核心库，处理root权限请求和操作
    api(libs.libsu.service)   // libsu的服务组件，支持后台root操作
    api(libs.libsu.nio)       // libsu的NIO扩展，提供root环境下的文件操作

    // 网络请求库
    implementation(libs.retrofit)            // Retrofit网络框架，用于RESTful API请求
    implementation(libs.retrofit.moshi)      // Retrofit的Moshi适配器，处理JSON数据
    implementation(libs.retrofit.scalars)    // Retrofit的标量适配器，处理字符串等简单数据

    implementation(libs.okhttp)              // OkHttp网络客户端（Retrofit的底层依赖）
    implementation(libs.okhttp.logging)      // OkHttp的日志拦截器，调试网络请求
    implementation(libs.okhttp.dnsoverhttps) // OkHttp的DNS over HTTPS支持，更安全的DNS解析

    implementation(libs.room.runtime)  // Room数据库运行时
    implementation(libs.room.ktx)      // Room的Kotlin扩展（如协程支持）
    ksp(libs.room.compiler)            // Room的注解处理器（通过KSP处理）

    implementation(libs.core.splashscreen)  // 启动屏组件
    implementation(libs.core.ktx)           // AndroidX Core的Kotlin扩展
    implementation(libs.activity)           // Activity组件
    implementation(libs.collection.ktx)     // 集合框架的Kotlin扩展
    implementation(libs.profileinstaller)   // 应用启动优化工具

    // We also implement all our tests in this module.
    // However, we don't want to bundle test dependencies.
    // That's why we make it compileOnly.
    compileOnly(libs.test.junit)        // JUnit测试框架（仅编译时依赖，不打包到最终库）
    compileOnly(libs.test.uiautomator)  // UI自动化测试库（仅编译时依赖）
}

tasks.matching { it.name == "generateDebugLintModel" }.configureEach {
    dependsOn("syncDebugAssets")
}

tasks.matching { it.name == "lintAnalyzeDebug" }.configureEach {
    dependsOn("syncDebugAssets")
}

tasks.matching { it.name == "generateDebugLintReportModel" }.configureEach {
    dependsOn("syncDebugAssets")
}