// 作用：声明当前模块的插件类型，这里用 com.android.library 表示这是一个 Android 库模块（生成 AAR 包，可被其他 Android 模块依赖）。
// * 如果是 Android 应用模块，通常会用 com.android.application。
plugins {
    id("com.android.library")
}

// 作用：这是一个自定义的 Gradle 方法（需在脚本其他位置或依赖的脚本中定义），一般用于抽取公共配置逻辑（比如通用的依赖、编译选项等），让构建脚本更简洁、复用性更高。
setupCommon()

// android { }：Android Gradle 插件的核心配置域，用于定义 Android 构建的具体参数（如编译版本、签名、资源、命名空间等）。
// * namespace：
//  * 是 Android Gradle 插件 7.0+ 引入的命名空间配置，替代旧版的 applicationId（库模块无 applicationId，但需统一资源、R 类的命名空间）。
//  * 作用是避免资源、类名冲突，确保库的 R 类、Manifest 中组件的命名空间唯一（这里为 com.topjohnwu.shared）。
android {
    namespace = "com.fastjien.shared"
}
