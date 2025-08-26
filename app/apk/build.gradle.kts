// 插件配置
plugins {
    id("com.android.application")  // Android应用插件，标记这是一个可安装的应用模块
    kotlin("android")  // Kotlin Android插件，支持在Android中使用kotlin
    kotlin("plugin.parcelize")  // Kotlin Parcelize插件，简化实现Parcelable接口（用于组件间数据传递）
    kotlin("kapt")  // Kotlin注解处理插件，用于处理编译时注解（如Dagger、Room等）
    id("androidx.navigation.safeargs.kotlin")  // Jetpack Navigation的Safe Args插件，生成类型安全的导航参数
}

// 调用自定义函数？？？TODO
setupMainApk()

// KAPT 配置（kapt块）
// kapt是 Kotlin 的注解处理器工具，这里的配置用于优化注解处理的效率和错误提示。
kapt {
    // 启用更严格的类型检查，帮助捕获注解处理中的类型错误
    correctErrorTypes = true
    // 启用构建缓存，加速注解处理过程
    useBuildCache = true
    // 将注解处理器的错误信息映射到原始源代码位置，便于调试
    mapDiagnosticLocations = true
    javacOptions {
        // 设置Java编译器最大错误输出数量为1000（默认较少，可能截断错误信息）
        option("-Xmaxerrs", "1000")
    }
}

// Android构建配置
android {
    buildFeatures {
        // 启用DataBinding（数据绑定库），支持XML布局与代码的双向绑定
        dataBinding = true
    }

    compileOptions {
        // 启用核心库脱糖（Core Library Desugaring）
        isCoreLibraryDesugaringEnabled = true
    }

    buildTypes {
        // 发布版本配置
        release {
            // 启用代码混淆（通过ProGuard或R8）
            isMinifyEnabled = true
            // 启用资源压缩（移除未使用的图片、XML等资源）
            isShrinkResources = true
        }
    }
}

dependencies {
    // 依赖本地模块 :core
    implementation(project(":core"))
    coreLibraryDesugaring(libs.jdk.libs)

    implementation(libs.indeterminate.checkbox)
    implementation(libs.rikka.layoutinflater)
    implementation(libs.rikka.insets)
    implementation(libs.rikka.recyclerview)

    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)
    implementation(libs.recyclerview)
    implementation(libs.transition)
    implementation(libs.fragment.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)

    // Make sure kapt runs with a proper kotlin-stdlib
    kapt(kotlin("stdlib"))
}
