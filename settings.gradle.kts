rootProject.name = "helidon-concurrency-limits"

include("helidon-common-concurrency-limits")
include("helidon-webserver-limiter")
include("helidon-webserver-tests")

buildCache {
    local {
        isEnabled = true
    }
}

val helidonVersion = "4.1.0-SNAPSHOT"

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            version("helidon", helidonVersion)
            library("helidonConfig", "io.helidon.config", "helidon-config-yaml").versionRef("helidon")
            library("helidonHealth", "io.helidon.health", "helidon-health-checks").versionRef("helidon")
            library("helidonLogging", "io.helidon.logging", "helidon-logging-jul").versionRef("helidon")
            library("helidonMediaJsonP", "io.helidon.http.media", "helidon-http-media-jsonp").versionRef("helidon")
            library("helidonMetricsSystemMeters", "io.helidon.metrics", "helidon-metrics-system-meters").versionRef("helidon")
            library("helidonWebserver", "io.helidon.webserver", "helidon-webserver").versionRef("helidon")
            library("helidonWebserverAccessLog", "io.helidon.webserver", "helidon-webserver-access-log").versionRef("helidon")
            library("helidonWebserverObserveHealth", "io.helidon.webserver.observe", "helidon-webserver-observe-health").versionRef("helidon")
            library("helidonWebserverObserveMetrics", "io.helidon.webserver.observe", "helidon-webserver-observe-metrics").versionRef("helidon")
            library("helidonWebserverSecurity", "io.helidon.webserver", "helidon-webserver-security").versionRef("helidon")
            bundle("helidon", listOf("helidonConfig", "helidonHealth", "helidonLogging", "helidonMediaJsonP", "helidonWebserver", "helidonWebserverAccessLog", "helidonWebserverObserveHealth", "helidonWebserverObserveMetrics", "helidonWebserverSecurity"))
        }
        create("testLibs") {
            version("junit", "5.10.2")
            library("assertJ", "org.assertj", "assertj-core").version("3.26.0")

            version("helidon", helidonVersion)
            library("helidonWebclient", "io.helidon.webclient", "helidon-webclient").versionRef("helidon")
            library("helidonWebserverTesting", "io.helidon.webserver.testing.junit5", "helidon-webserver-testing-junit5").versionRef("helidon")
            bundle("helidonTestkit", listOf("helidonWebclient", "helidonWebserverTesting"))
        }
    }
}
