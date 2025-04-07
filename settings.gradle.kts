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
    }
}

rootProject.name = "PokemonApp"

// App modülü
include(":app")

// Core modülleri
include(":core:common")
include(":core:ui")
include(":core:network")
include(":core:database")
include(":core:testing")

// Feature modülleri
include(":feature:pokemon-list")
include(":feature:pokemon-detail")
include(":feature:pokemon-search")
include(":feature:pokemon-types")
include(":feature:pokemon-abilities")
include(":feature:pokemon-moves")
include(":feature:pokemon-teams")
 