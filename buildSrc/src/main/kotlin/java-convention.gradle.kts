import com.diffplug.gradle.eclipse.apt.GenerateEclipseJdtApt
import org.gradle.plugins.ide.eclipse.model.AbstractClasspathEntry
import org.gradle.plugins.ide.eclipse.model.Classpath
import org.gradle.plugins.ide.eclipse.model.Library
import org.gradle.plugins.ide.eclipse.model.SourceFolder

plugins {
    checkstyle
    eclipse
    java
    id("com.diffplug.eclipse.apt")
}

checkstyle {
    toolVersion = "10.17.0"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs = listOf(
        "-parameters",
        "--enable-preview",
    )
    options.encoding = "UTF-8"
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter("5.10.3")
            targets {
                all {
                    testTask.configure {
                        options {
                            jvmArgs("--enable-preview")
                            // increase parallel test execution to the machines capacity
                            maxParallelForks = Runtime.getRuntime().availableProcessors()
                        }
                    }
                }
            }
        }
    }
}

eclipse {

    fun addAptGeneratedEntry(): Action<Classpath> = Action<Classpath> {
        val name = ".apt_generated"
        val entry = this.entries.find {
            it is AbstractClasspathEntry && it.path.contains(name)
        }
        if (entry == null) {
            val e = SourceFolder(name, null)
            e.entryAttributes["ignore_optional_problems"] = "true"
            this.entries.add(e)
        }
    }

    fun addAptGeneratedTestsEntry(): Action<Classpath> = Action<Classpath> {
        val name = ".apt_generated_tests"
        val entry = this.entries.find {
            it is AbstractClasspathEntry && it.path.contains(name)
        }
        if (entry == null) {
            val e = SourceFolder(name, "bin/test")
            e.entryAttributes["ignore_optional_problems"] = "true"
            e.entryAttributes["test"] = "true"
            e.entryAttributes["optional"] = "true"
            this.entries.add(e)
        }
    }

    classpath {
        defaultOutputDir = file("build/classes-main-ide")

        file {
            // filter out pom files
            whenMerged(
                Action<Classpath> {
                    entries =
                        entries.filter { entry -> entry.kind != "lib" || !(entry as Library).path.endsWith("pom") }
                },
            )

            // ignore warnings for generated code
            whenMerged.add(addAptGeneratedEntry())
            whenMerged.add(addAptGeneratedTestsEntry())
        }
    }

    val eclipseJdtPrepare = tasks.register("eclipseJdtPrepare", Copy::class) {
        from(rootProject.file("config/eclipse"))
        into(project.file(".settings/"))
        include("*.prefs")
    }
    tasks.named<GenerateEclipseJdt>(EclipsePlugin.ECLIPSE_JDT_TASK_NAME) {
        dependsOn(eclipseJdtPrepare)
    }
    tasks.named<GenerateEclipseJdtApt>("eclipseJdtApt") {
        dependsOn(eclipseJdtPrepare)
    }
}
