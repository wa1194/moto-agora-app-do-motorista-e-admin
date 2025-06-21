pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // CORREÇÃO: Esta linha permite que o projeto encontre o osmdroid-bonuspack
        maven { url = uri("https://jitpack.io") }
    }
}
rootProject.name = "Moto Agora"
include(":app")
