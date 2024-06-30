import io.morethan.jmhreport.gradle.JmhReportExtension

plugins {
    id("io.morethan.jmhreport")
}

configure<JavaPluginExtension> {
    configure<SourceSetContainer> {
        register("jmh") {
            java.srcDir(file("src/jmh/java"))
            resources.srcDir(file("src/jmh/resources"))
            compileClasspath += getByName(SourceSet.MAIN_SOURCE_SET_NAME).output + getByName(SourceSet.TEST_SOURCE_SET_NAME).output
            runtimeClasspath += output + compileClasspath
        }

        tasks.register("jmh", JavaExec::class) {
            description = "Run the JMH bemchmarks."
            group = "benchmarks"
            classpath = getByName("jmh").runtimeClasspath
            mainClass.set("org.openjdk.jmh.Main")
            finalizedBy(tasks["jmhReport"])

            val verbosity = project.findProperty("verbosity") ?: "NORMAL" // [SILENT, NORMAL, EXTRA]
            val resultFormat = project.findProperty("format") ?: "json"
            val resultFile = file("build/reports/jmh/jmh.$resultFormat")

            val argumentList: MutableList<String> = mutableListOf()

            fun addToArguments(vararg properties: (Any)) {
                properties.forEach { argumentList.add(it.toString()) }
            }

            project.findProperty("include")?.let { addToArguments(it) }
            project.findProperty("exclude")?.let { addToArguments("-e", it) }
            project.findProperty("profilers")?.let {
                it.toString().split(",").forEach { profiler -> addToArguments("-prof", profiler) }
            }
            project.findProperty("verify")?.let {
                // Execute the benchmarks with the minimum amount of executions
                // Used to check if all benchmarks are still working.
                addToArguments("-f", 1, "-wi", 1, "-i", 1)
            }
            if (project.findProperty("ignoreFailures") == null) {
                addToArguments("-foe", "true")
            }

            addToArguments("-v", verbosity)
            addToArguments("-rf", resultFormat)
            addToArguments("-rff", resultFile)
            addToArguments("-jvmArgsPrepend", "-Xms2048m")
            addToArguments("-jvmArgsPrepend", "-Xmx2048m")
            project.findProperty("jvmArgs")?.let {
                it.toString().split(" ").forEach { jvmArg -> addToArguments("-jvmArgsPrepend", jvmArg) }
            }

            args = argumentList

            doFirst {
                println("\nExecuting JMH with: $args \n")
                resultFile.parentFile.mkdirs()
            }
        }

        tasks.register("jmhProfilers", JavaExec::class) {
            description = "Lists the available profilers for the jmh task."
            group = "benchmarks"
            classpath = getByName("jmh").runtimeClasspath
            mainClass.set("org.openjdk.jmh.Main")
            args = listOf("-lprof")
        }

        tasks.register("jmhHelp") {
            description = "Print help for the jmh task."
            group = "benchmarks"
            doLast {
                println("")
                println("Usage of jmh task:")
                println("")

                println("Only execute specific benchmark(s):")
                println("\t./gradlew jmh -Pinclude=\".*MyBenchmark.*\"")

                println("")
                println("Specify extra profilers:")
                println("\t./gradlew jmh -Pprofilers=\"gc,stack\"")

                println("")
                println("Prominent profilers (for full list call jmhProfilers task):")
                println("\tgc - print garbage collection stats")
                println("\tcomp - JitCompilations, tune your iterations")
                println("\tstack - which methods used most time")
                println("\ths_thr - thread usage")

                println("")
                println("Change report format from JSON to one of [CSV, JSON, NONE, SCSV, TEXT]:")
                println("\t./gradlew jmh -Pformat=csv")

                println("")
                println("Specify JVM arguments:")
                println("\t./gradlew jmh -PjvmArgs=\"-Duser.timezone=Europe/Sofia\"")

                println("")
                println("Run in verification mode (execute benchmarks with minimum of fork/warmup-/benchmark-iterations):")
                println("\tgw jmh -Pverify")

                println("")
                println("Continue executing the benchmark suite in case of failures:")
                println("\t./gradlew jmh -PignoreFailures")

                println("")
                println("Configure verbosity: []")
                println("\t./gradlew jmh -PignoreFailures")

                println("")
                println("Resources:")
                println("\thttp://tutorials.jenkov.com/java-performance/jmh.html (Introduction)")
                println(
                    "\thttp://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/ (Samples)"
                )
            }
        }
    }
}

configure<JmhReportExtension> {
    jmhResultPath = project.file("build/reports/jmh/jmh.json").absolutePath
    jmhReportOutput = project.file("build/reports/jmh").absolutePath
}

project.dependencies.add("jmhImplementation", "org.openjdk.jmh:jmh-core:1.37")
project.dependencies.add("jmhAnnotationProcessor", "org.openjdk.jmh:jmh-generator-annprocess:1.37")
