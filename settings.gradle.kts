rootProject.name = "TeXiFy-IDEA"

// https://github.com/JetBrains/intellij-platform-gradle-plugin/issues/1750#issuecomment-3695942245
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://www.jetbrains.com/intellij-repository/releases")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
}