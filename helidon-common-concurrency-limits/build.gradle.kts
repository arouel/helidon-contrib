plugins {
    `java-convention`
    `java-library`
    `jmh-convention`
}

dependencies {
    val helidonVersion = libs.versions.helidon.get()
    annotationProcessor("io.helidon.config:helidon-config-metadata-processor:${helidonVersion}")
    annotationProcessor("io.helidon.codegen:helidon-codegen-apt:${helidonVersion}")
    annotationProcessor("io.helidon.builder:helidon-builder-codegen:${helidonVersion}")
    annotationProcessor("io.helidon.codegen:helidon-codegen-helidon-copyright:${helidonVersion}")

    api("io.helidon.config:helidon-config:${helidonVersion}")
    implementation("io.helidon.metrics:helidon-metrics-api:${helidonVersion}")

    testImplementation(testLibs.assertJ)

    jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    jmhImplementation("io.helidon.config:helidon-config:${helidonVersion}")
}
