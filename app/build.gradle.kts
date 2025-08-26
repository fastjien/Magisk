plugins {
    id("MagiskPlugin")
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)

    subprojects.forEach {
        dependsOn(":${it.name}:clean")
    }
}

tasks.matching { it.name == "generateDebugLintReportModel" }.configureEach {
    dependsOn("downloadDebugLsposed")
}
