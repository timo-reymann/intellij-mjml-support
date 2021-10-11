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
    kotlin("jvm") version "1.5.30"
    id("org.jetbrains.intellij") version "1.2.0"
    id("com.palantir.git-version") version "0.12.3"
    id("com.adarshr.test-logger") version "3.0.0"
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
            "com.jetbrains.php:211.7142.45",
            "com.jetbrains.hackathon.indices.viewer:1.13"
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

        // Prevent "File access outside allowed roots" in multi module tests, because modules each have an .iml
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
                "PS-212.5080.71", // 2021.2.1
                "PS-212.4746.100", // 2021.2
                "PS-211.7628.25", // 2021.1.4
                "PS-211.7442.50", // 2021.1.3
                "PS-211.7142.44", // 2021.1.2

                // IU - IntelliJ IDEA Ultimate
                "IU-212.5080.55", // 2021.2.1
                "IU-212.4746.92", // 2021.2
                "IU-211.7628.21", // 2021.1.3
                "IU-211.7442.40", // 2021.1.2
                "IU-211.7142.45", // 2021.1.1

                // IC - IntelliJ IDEA Community Edition
                "IC-212.5080.55", // 2021.2.1
                "IC-212.4746.92", // 2021.2
                "IC-211.7628.21", // 2021.1.3
                "IC-211.7442.40", // 2021.1.2
                "IC-211.7142.45" // 2021.1.1
            )
        )
        failureLevel.set(
            listOf(
                org.jetbrains.intellij.tasks.RunPluginVerifierTask.FailureLevel.INVALID_PLUGIN
            )
        )
    }
}
