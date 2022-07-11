fun getVersionDetails(): com.palantir.gradle.gitversion.VersionDetails =
    (extra["versionDetails"] as groovy.lang.Closure<*>)() as com.palantir.gradle.gitversion.VersionDetails

val gitInfo = getVersionDetails()
var releaseChannels = arrayOf<String>()

when {
    properties.containsKey("snapshotVersion") -> {
        version = properties["snapshotVersion"]!!
        releaseChannels = arrayOf("snapshot")
    }
    gitInfo.isCleanTag -> {
        version = gitInfo.lastTag
        releaseChannels = arrayOf("default")
    }
    else -> {
        version = gitInfo.version
        releaseChannels = arrayOf("local")
    }
}

repositories {
    mavenCentral()
}

plugins {
    id("java")
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.intellij") version "1.6.0"
    id("com.palantir.git-version") version "0.15.0"
    id("com.adarshr.test-logger") version "3.2.0"
}

dependencies {
    implementation(kotlin("reflect"))
    testImplementation("junit", "junit", "4.12")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set(properties["idea-version"] as String)
    updateSinceUntilBuild.set(false)
    downloadSources.set(true)
    pluginName.set("MJML Support")
    plugins.set(
        listOf(
            "CSS",
            "HtmlTools",
            "JavaScript",
            "com.jetbrains.hackathon.indices.viewer:1.20"
        )
    )
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    compileJava {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }

    test {
        testLogging {
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }

        useJUnit()

        // Prevent "File access outside allowed roots" in multi-module tests, because modules each have an .iml
        environment("NO_FS_ROOTS_ACCESS_CHECK", "1")
    }

    patchPluginXml {
        setVersion(project.version)
    }

    publishPlugin {
        dependsOn("patchPluginXml")
        token.set(System.getenv("JB_TOKEN"))
        channels.set(releaseChannels.toList())
    }

    runPluginVerifier {
        ideVersions.set(
            // Top 3 used IDEs with latest 5 versions
            // Generated with https://github.com/timo-reymann/script-shelve/blob/master/jetbrains/query_ide_versions_for_verifier.py
            listOf(
                // PS - PhpStorm
                "PS-212.5457.49", // 2021.2.3
                "PS-211.7628.25", // 2021.1.4

                // IU - IntelliJ IDEA Ultimate
                "IU-212.5457.46", // 2021.2.3
                "IU-211.7628.21", // 2021.1.3

                // IC - IntelliJ IDEA Community Edition
                "IC-212.5457.46", // 2021.2.3
                "IC-211.7628.21", // 2021.1.3

                // WS - WebStorm
                "WS-212.5457.55", // 2021.2.3
                "WS-211.7628.25" // 2021.1.3
            )
        )
        failureLevel.set(
            listOf(
                org.jetbrains.intellij.tasks.RunPluginVerifierTask.FailureLevel.INVALID_PLUGIN
            )
        )
    }
}
