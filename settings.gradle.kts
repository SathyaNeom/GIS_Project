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

rootProject.name = "GPS_Device_Proj"
include(":app")
include(":app-catalog")
