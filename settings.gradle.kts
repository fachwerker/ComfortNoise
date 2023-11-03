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
        //maven { url = uri("https://mvn.0110.be/releases") }
        //maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "ComfortNoise"
include(":app")
