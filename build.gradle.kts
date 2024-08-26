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
    // Define a JVM target with Java interoperability
    jvm {
        withJava()
    }

    // For now, we focus on JVM as the target
}

tasks {
    // Task to compile the native code
    val compileNative by creating(Exec::class) {
        group = "build"
        description = "Compiles the native C++ code into a shared library"

        val outputDir = layout.buildDirectory.dir("libs").get().asFile
        outputs.dir(outputDir)

        // Compile command for Linux/MacOS
        commandLine("g++", "-shared", "-fPIC", "-I${System.getenv("JAVA_HOME")}/include", "-I${System.getenv("JAVA_HOME")}/include/linux", "-o", "$outputDir/libvsockk.so", "src/main/cpp/VSockImpl.cpp")

        // For Windows users can build the dll by uncommenting this:
        // commandLine("g++", "-shared", "-o", "$outputDir/vsockk.dll", "-I%JAVA_HOME%/include", "-I%JAVA_HOME%/include/win32", "src/main/cpp/VSockImpl.cpp")
    }

    // Compile the Kotlin classes before JNI
    named("compileKotlinJvm") {
        dependsOn(compileNative)
    }

    // Add the native library in the JAR
    named<Jar>("jvmJar") {
        from(layout.buildDirectory.dir("libs").get().asFile) {
            include("libvsockk.so")  // For Linux/MacOS
            // include("vsockk.dll")  // Windows
        }
    }
}

// Publish the library to a local Maven repository or JitPack
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["kotlin"])

            // Add compiled native libraries to the published artifacts
            artifact(layout.buildDirectory.file("libs/libvsockk.so").get().asFile) { // For Linux/MacOS
                // artifact(layout.buildDirectory.file("libs/vsockk.dll").get().asFile)  // Windows
                classifier = ""
                extension = "so"  // Use "dll" for Windows
            }

            groupId = project.group.toString()
            artifactId = "vsockk"
            version = project.version.toString()
        }
    }

    repositories {
        maven {
            // We'll use JitPack repository for publishing
            url = uri("https://jitpack.io")
        }
    }
}

// Tests are coming
