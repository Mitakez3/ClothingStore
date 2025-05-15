pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://jitpack.io") // Thêm dòng này
        flatDir {
            dirs ("libs")
        }

    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // Thêm nếu dùng JitPack
    }
}

rootProject.name = "ClothingStore"
include(":app")
