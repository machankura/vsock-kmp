import java.util.*

plugins {
    kotlin("multiplatform") version "2.0.0"
    `maven-publish`
}

group = "machankura.vsockk"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        withJava()
        compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
            }
        }
    }

    // For now, we focus on JVM as the target

    sourceSets {
        val jvmMain by getting {
            kotlin.srcDir("src/main/kotlin")
        }
        val jvmTest by getting {
            kotlin.srcDir("src/test/kotlin")
        }
    }
}

tasks {
    // Task to configure the native code build
    val configureNative by creating(Exec::class) {
        group = "build"
        description = "Configures the native C++ code build using CMake"

        val buildDir = layout.buildDirectory.dir("cmake-build").get().asFile
        val outputDir = layout.buildDirectory.dir("cmake-build/libs").get().asFile
        outputs.dir(outputDir)

        doFirst {
            buildDir.mkdirs()
        }

        // CMake configuration
        commandLine("cmake", "-S", ".", "-B", buildDir.absolutePath)
    }

    // Task to actually build the native library
    val buildNative by creating(Exec::class) {
        group = "build"
        description = "Builds the native C++ library using the configured CMake files"

        val buildDir = layout.buildDirectory.dir("cmake-build").get().asFile

        doFirst {
            buildDir.mkdirs()
        }

        val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
        when {
            osName.contains("win") -> {
                commandLine("cmake", "--build", buildDir.absolutePath, "--config", "Release")
            }
            osName.contains("nix") || osName.contains("nux") || osName.contains("mac") -> {
                commandLine("cmake", "--build", buildDir.absolutePath)
            }
            else -> {
                throw GradleException("Unsupported OS: $osName")
            }
        }

        dependsOn(configureNative)
    }

    // Ensure tests depend on native build
    withType<Test> {
        useJUnitPlatform()
        dependsOn(buildNative)
        testLogging {
            events("passed", "skipped", "failed")
        }

        // Directory containing the native library added to the JVM library path
        val libDir = layout.buildDirectory.dir("cmake-build/libs").get().asFile
        systemProperty("java.library.path", libDir)

        // Build should fail immediately if any test fails
        //failFast = true
    }

    // Disable test tasks if the 'skipTests' property is set
    gradle.taskGraph.whenReady {
        if (gradle.startParameter.taskNames.contains("skipTests")) {
            withType<Test>().configureEach {
                enabled = false
            }
        }
    }

    // Skip tests - does not work yet, same as test alone but build run tests
    val skipTests by creating {
        group = "build"
        description = "Builds the project without running tests"
        dependsOn("build")
    }

    // Compile kotlin classes after the native stuff
    named("compileKotlinJvm") {
        dependsOn(buildNative)
    }

    // Create the JAR file including compiled classes and native libraries
    named<Jar>("jvmJar") {
        dependsOn(buildNative)

        from(layout.buildDirectory.dir("classes/kotlin/jvm/main"))
        from(layout.buildDirectory.dir("cmake-build/libs"))
    }
}

// Publish the library to a local Maven repository or JitPack
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["kotlin"])

            // Add also the compiled native libraries to the published artifacts
            val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
            if (osName.contains("win")) {
                artifact(layout.buildDirectory.file("libs/vsock-kmp.dll").get().asFile) {
                    classifier = ""
                    extension = "dll"
                }
            } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("mac")) {
                artifact(layout.buildDirectory.file("libs/libvsock-kmp.so").get().asFile) {
                    classifier = ""
                    extension = "so"
                }
            }

            groupId = project.group.toString()
            artifactId = "vsock-kmp"
            version = project.version.toString()
        }
    }

    repositories {
        maven {
            // We'll use JitPack repository for publishing, maybe for now
            url = uri("https://jitpack.io")
        }
    }
}
