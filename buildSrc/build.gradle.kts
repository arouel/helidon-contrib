plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

repositories {
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

dependencies {
    implementation("com.diffplug.eclipse.apt:com.diffplug.eclipse.apt.gradle.plugin:3.44.0")
    implementation("gradle.plugin.io.morethan.jmhreport:gradle-jmh-report:0.9.6")
}
