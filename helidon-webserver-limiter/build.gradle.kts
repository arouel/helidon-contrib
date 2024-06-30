plugins {
    `java-convention`
    `java-library`
}

dependencies {
    val helidonVersion = libs.versions.helidon.get()
    annotationProcessor("io.helidon.config:helidon-config-metadata-processor:${helidonVersion}")
    annotationProcessor("io.helidon.codegen:helidon-codegen-apt:${helidonVersion}")
    annotationProcessor("io.helidon.builder:helidon-builder-codegen:${helidonVersion}")
    annotationProcessor("io.helidon.codegen:helidon-codegen-helidon-copyright:${helidonVersion}")

    api(project(":helidon-common-concurrency-limits"))
    api("io.helidon.config:helidon-config:${helidonVersion}")
    api("io.helidon.webserver:helidon-webserver-context:${helidonVersion}")

    testImplementation(testLibs.assertJ)
    testImplementation(testLibs.bundles.helidonTestkit)
}
