import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

// Include the Gradle plugins which help building everything.
// Supersedes the use of "buildscript" block and "apply plugin:"
plugins {
    id("org.jetbrains.intellij") version "0.7.2"
    kotlin("jvm") version("1.4.30-M1")

    // Plugin which can check for Gradle dependencies, use the help/dependencyUpdates task.
    id("com.github.ben-manes.versions") version "0.38.0"

    // Plugin which can update Gradle dependencies, use the help/useLatestVersions task.
    id("se.patrikerdes.use-latest-versions") version "0.2.15"

    // Used to debug in a different IDE
    id("de.undercouch.download") version "4.1.1"

    // Test coverage
    jacoco

    // Linting
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

group = "nl.hannahsten"
version = "0.8.0-alpha.1"

repositories {
    mavenCentral()
}

sourceSets {
    getByName("main").apply {
        java.srcDirs("src", "gen")
        resources.srcDirs("resources")
    }

    getByName("test").apply {
        java.srcDirs("test")
        resources.srcDirs("test/resources")
    }
}

// Java target version
java.sourceCompatibility = JavaVersion.VERSION_11

// Specify the right jvm target for Kotlin
tasks.compileKotlin {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-Xjvm-default=enable")
        useIR = true // https://blog.jetbrains.com/kotlin/2021/02/the-jvm-backend-is-in-beta-let-s-make-it-stable-together
    }
}

// Same for Kotlin tests
tasks.compileTestKotlin {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-Xjvm-default=enable")
        useIR = true
    }
}

dependencies {
    // Local dependencies
    implementation(files("lib/pretty-tools-JDDE-2.1.0.jar"))
    implementation(files("lib/JavaDDE.dll"))
    implementation(files("lib/JavaDDEx64.dll"))

    // D-Bus Java bindings
    implementation("com.github.hypfvieh:dbus-java:3.3.0")
    implementation("org.slf4j:slf4j-simple:2.0.0-alpha1")

    // Unzipping tar.xz/tar.bz2 files on Windows containing dtx files
    implementation("org.codehaus.plexus:plexus-component-api:1.0-alpha-33")
    implementation("org.codehaus.plexus:plexus-container-default:2.1.0")
    implementation("org.codehaus.plexus:plexus-archiver:4.2.4")

    // Parsing json
    implementation("com.beust:klaxon:5.5")

    // Comparing versions
    implementation("org.apache.maven:maven-artifact:3.8.1")

    // Test dependencies

    // Also implementation junit 4, just in case
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.8.0-M1")

    // Use junit 5 for test cases
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.0-M1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.0-M1")

    // Enable use of the JUnitPlatform Runner within the IDE
    testImplementation("org.junit.platform:junit-platform-runner:1.8.0-M1")

    // just in case
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime")

    testImplementation("io.mockk:mockk:1.11.0")

    // Add custom ruleset from github.com/slideclimb/ktlint-ruleset
    ktlintRuleset(files("lib/ktlint-ruleset-0.2.jar"))
}

// Special resource dependencies
tasks.processResources {
    from("lib") {
        include("pretty-tools-JDDE-2.1.0.jar")
        include("JavaDDE.dll")
        include("JavaDDEx64.dll")
    }
}

// https://plugins.jetbrains.com/docs/intellij/dynamic-plugins.html#diagnosing-leaks
tasks.runIde {
    jvmArgs = mutableListOf("-XX:+UnlockDiagnosticVMOptions")

    // Set to true to generate hprof files on unload fails
    systemProperty("ide.plugins.snapshot.on.unload.fail", "false")
}

intellij {
    pluginName = "TeXiFy-IDEA"

    // indices plugin doesn't work in tests
    setPlugins("tanvd.grazi", "java") // , "com.firsttimeinforever.intellij.pdf.viewer.intellij-pdf-viewer:0.10.0") // , "com.jetbrains.hackathon.indices.viewer:1.12")

    // Use the since build number from plugin.xml
    updateSinceUntilBuild = false
    // Keep an open until build, to avoid automatic downgrades to very old versions of the plugin
    sameSinceUntilBuild = true

    // Comment out to use the latest EAP snapshot
    // Docs: https://github.com/JetBrains/gradle-intellij-plugin#intellij-platform-properties
    // All snapshot versions: https://www.jetbrains.com/intellij-repository/snapshots/
    version = "2021.1"
//    version = "PY-203.5419.8-EAP-SNAPSHOT"
//    type = "PY"

    // Example to use a different, locally installed, IDE
    // If you get the error "Cannot find builtin plugin java for IDE", remove the "java" plugin above
    // Also disable "version" above
    // If it doesn't work (Could not resolve all files for configuration ':detachedConfiguration4'.), specify 'version' instead
//    localPath = "/home/thomas/.local/share/JetBrains/Toolbox/apps/PyCharm-P/ch-1/203.5419.8/"
}

// Allow publishing to the Jetbrains repo via a Gradle task
// This requires to put a Jetbrains Hub token, see http://www.jetbrains.org/intellij/sdk/docs/tutorials/build_system/deployment.html for more details
// Generate a Hub token at https://hub.jetbrains.com/users/me?tab=authentification
// You should provide it either via environment variables (ORG_GRADLE_PROJECT_intellijPublishToken) or Gradle task parameters (-Dorg.gradle.project.intellijPublishToken=mytoken)
tasks.publishPlugin {
    token(properties["intellijPublishToken"])

    // Specify channel as per the tutorial.
    // More documentation: https://github.com/JetBrains/gradle-intellij-plugin/blob/master/README.md#publishing-dsl
    channels("alpha")
}

tasks.test {
    // For MockK. Make sure it finds the libattach.so in the lib folder.
    jvmArgs = listOf("-Djdk.attach.allowAttachSelf=true", "-Djava.library.path=lib/")
    // Enable JUnit 5 (Gradle 4.6+).
    useJUnitPlatform()
    // Show test results
    testLogging {
        events(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
        exceptionFormat = TestExceptionFormat.FULL
    }
}

// Test coverage reporting
tasks.jacocoTestReport {
    // Enable xml for codecov
    reports {
        html.isEnabled = true
        xml.isEnabled = true
        xml.destination = file("$buildDir/reports/jacoco/test/jacocoTestReport.xml")
    }

    sourceSets(project.sourceSets.getByName("main"))
}

ktlint {
    verbose.set(true)
}