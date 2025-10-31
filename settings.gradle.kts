/**
 * @author Sathya Narayanan
 */
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Esri ArcGIS Maps SDK repository
        maven {
            url = uri("https://esri.jfrog.io/artifactory/arcgis")
        }
    }
}

rootProject.name = "Electronic Services"
include(":app")
include(":app-catalog")
include(":core")
include(":design-system")
include(":domain")
include(":data")
include(":feature_auth")
include(":feature_map")
include(":feature_jobs")