import org.gradle.plugins.ide.eclipse.model.AbstractClasspathEntry
import org.gradle.plugins.ide.eclipse.model.Classpath
import java.nio.file.Files

plugins {
    `java-convention`
    application
    id("org.graalvm.buildtools.native") version "0.10.2"
}

graalvmNative {
    agent {
        defaultMode = "standard"
    }
    binaries.all {
        buildArgs("-H:+AddAllCharsets")
        buildArgs("-march=compatibility")
        resources.autodetect()
    }
    toolchainDetection = false
}

dependencies {
    implementation(project(":helidon-webserver-limiter"))
    implementation(libs.helidonConfig)
    implementation(libs.helidonLogging)
    implementation(libs.helidonWebserver)
    implementation(libs.helidonWebserverObserveHealth)
    implementation(libs.helidonWebserverObserveMetrics)

    runtimeOnly(libs.helidonMetricsSystemMeters)
    
    testImplementation(testLibs.assertJ)
    testImplementation(testLibs.bundles.helidonTestkit)
}

application {
    mainClass.set("io.helidon.webserver.tests.Main")
}
