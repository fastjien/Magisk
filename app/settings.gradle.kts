@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    // 设为 FAIL_ON_PROJECT_REPOS ，若子模块（如 :apk、:core ）私自配置仓库，构建会失败，强制依赖仓库统一管理。
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    // repositories：声明项目可用的依赖仓库：
    //  google()：Google 官方 Maven 仓库，用于下载 Android 相关依赖（如 androidx 库 ）。
    //  mavenCentral()：Maven 中央仓库，是 Java/Android 依赖的主要来源之一。
    //  maven(url = "https://jitpack.io")：JitPack 仓库，可从 GitHub 直接拉取开源项目依赖（很多未发布到中央仓库的库会用 ）。
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.aliyun.com/repository/public")
    }
}
// 设置根项目名称为 Sunny ，影响 Gradle 项目在构建日志、IDE 里的显示名称。
rootProject.name = "Sunny"
// 声明项目包含的子模块，这里有
// * :apk（可能是 App 主模块 ）、
// * :core（核心逻辑 ）、
// * :shared（公共代码 ）、
// * :stub（占位 / 存根模块 ）、
// * :test（测试模块 ）。
// Gradle 会根据这些声明，加载对应模块的 build.gradle.kts 配置，实现多模块构建。
include(":apk", ":core", ":shared", ":stub", ":test")
