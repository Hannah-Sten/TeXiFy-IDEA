import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.changelog.Changelog

fun properties(key: String) = project.findProperty(key).toString()

// Include the Gradle plugins which help building everything.
// Supersedes the use of "buildscript" block and "apply plugin:"
plugins {
    id("org.jetbrains.intellij") version "1.13.3"
    kotlin("jvm") version ("1.8.0")
    kotlin("plugin.serialization") version ("1.8.0")

    // Plugin which can check for Gradle dependencies, use the help/dependencyUpdates task.
    id("com.github.ben-manes.versions") version "0.46.0"

    // Plugin which can update Gradle dependencies, use the help/useLatestVersions task.
    id("se.patrikerdes.use-latest-versions") version "0.2.18"

    // Used to debug in a different IDE
    id("de.undercouch.download") version "5.4.0"

    // Test coverage
    id("org.jetbrains.kotlinx.kover") version "0.7.0-ALPHA"

    // Linting
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"

    // Vulnerability scanning
    id("org.owasp.dependencycheck") version "8.2.1"

    id("org.jetbrains.changelog") version "2.0.0"
}

group = "nl.hannahsten"
version = properties("pluginVersion")

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
java.sourceCompatibility = JavaVersion.VERSION_17

// Specify the right jvm target for Kotlin
tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

// Same for Kotlin tests
tasks.compileTestKotlin {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

dependencies {
    // Local dependencies
    implementation(files("lib/pretty-tools-JDDE-2.1.0.jar"))
    implementation(files("lib/JavaDDE.dll"))
    implementation(files("lib/JavaDDEx64.dll"))

    // D-Bus Java bindings
    implementation("com.github.hypfvieh:dbus-java:3.3.2")
    implementation("org.slf4j:slf4j-simple:2.0.7")

    // Unzipping tar.xz/tar.bz2 files on Windows containing dtx files
    implementation("org.codehaus.plexus:plexus-component-api:1.0-alpha-33")
    implementation("org.codehaus.plexus:plexus-container-default:2.1.1")
    implementation("org.codehaus.plexus:plexus-archiver:4.6.3")

    // Parsing json
    implementation("com.beust:klaxon:5.6")

    // Parsing xml
    implementation("com.fasterxml.jackson.core:jackson-core:2.14.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.14.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")

    // Http requests
    implementation("io.ktor:ktor-client-core:2.2.4")
    implementation("io.ktor:ktor-client-cio:2.2.4")
    implementation("io.ktor:ktor-client-auth:2.2.4")
    implementation("io.ktor:ktor-client-content-negotiation:2.2.4")
    implementation("io.ktor:ktor-server-core:2.3.0")
    implementation("io.ktor:ktor-server-jetty:2.2.4")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.2.4")

    // Comparing versions
    implementation("org.apache.maven:maven-artifact:4.0.0-alpha-5")

    // LaTeX rendering for preview
    implementation("org.scilab.forge:jlatexmath:1.0.7")
    // https://stackoverflow.com/questions/11677572/dealing-with-xerces-hell-in-java-maven
    implementation("org.apache.xmlgraphics:batik-codec:1.16") {
        exclude("xml-apis", "xml-apis")
        exclude("xml-apis", "xml-apis-ext")
    }

    // Test dependencies
    // No version specified, it equals the kotlin version
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Also implementation junit 4, just in case
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.9.2")

    // Use junit 5 for test cases
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    // Enable use of the JUnitPlatform Runner within the IDE
    testImplementation("org.junit.platform:junit-platform-runner:1.9.2")

    testImplementation("io.mockk:mockk:1.13.5")

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
    jvmArgs = mutableListOf("-XX:+UnlockDiagnosticVMOptions", "-Xmx2g", "-Djava.system.class.loader=com.intellij.util.lang.PathClassLoader")

    // Set to true to generate hprof files on unload fails
    systemProperty("ide.plugins.snapshot.on.unload.fail", "false")
    // Some warning asked for this to be set explicitly
    systemProperty("idea.log.path", file("build/idea-sandbox/system/log").absolutePath)
}

tasks.test {
    // https://intellij-support.jetbrains.com/hc/en-us/community/posts/4407334950290-jarFiles-is-not-set-for-PluginDescriptor
    systemProperty("idea.force.use.core.classloader", "true")
}

// Avoid ClassNotFoundException: com.maddyhome.idea.copyright.psi.UpdateCopyrightsProvider
tasks.buildSearchableOptions {
    jvmArgs = listOf("-Djava.system.class.loader=com.intellij.util.lang.PathClassLoader")
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.set(listOf("Added", "Fixed"))
    repositoryUrl.set("https://github.com/Hannah-Sten/TeXiFy-IDEA")
    itemPrefix.set("*")
}

tasks.patchPluginXml {
    // Required to run pluginVerifier
    sinceBuild.set(properties("pluginSinceBuild"))

    // Get the latest available change notes from the changelog file
    changeNotes.set(
        provider {
            with(changelog) {
                renderItem(
                    // When publishing alpha versions, we want the unreleased changes to be shown, otherwise we assume that patchChangelog has been run and we need to get the latest released version (otherwise it will show 'Unreleased' as title)
                    if (properties("pluginVersion").split("-").size != 1) changelog.getUnreleased()
                    else getOrNull(properties("pluginVersion")) ?: getLatest(),
                    Changelog.OutputType.HTML,
                )
            }
        }
    )
}

intellij {
    pluginName.set("TeXiFy-IDEA")

    // indices plugin doesn't work in tests
    plugins.set(
        listOf(
            "tanvd.grazi",
            "java",
            "com.firsttimeinforever.intellij.pdf.viewer.intellij-pdf-viewer:0.14.0",
            "com.jetbrains.hackathon.indices.viewer:1.23"
        )
    )

    // Use the since build number from plugin.xml
    updateSinceUntilBuild.set(false)
    // Keep an open until build, to avoid automatic downgrades to very old versions of the plugin
    sameSinceUntilBuild.set(true)

    // Comment out to use the latest EAP snapshot
    // Docs: https://github.com/JetBrains/gradle-intellij-plugin#intellij-platform-properties
    // All snapshot versions: https://www.jetbrains.com/intellij-repository/snapshots/
    version.set("2023.1")
//    type = "PY"

    // Example to use a different, locally installed, IDE
    // If you get the error "Cannot find builtin plugin java for IDE", remove the "java" plugin above
    // Also disable "version" above
    // If it doesn't work (Could not resolve all files for configuration ':detachedConfiguration4'.), specify 'version' instead
//    localPath.set("/home/thomas/.local/share/JetBrains/Toolbox/apps/PyCharm-P/ch-0/213.6777.50/")
}

// Allow publishing to the Jetbrains repo via a Gradle task
// This requires to put a Jetbrains Hub token, see http://www.jetbrains.org/intellij/sdk/docs/tutorials/build_system/deployment.html for more details
// Generate a Hub token at https://hub.jetbrains.com/users/me?tab=authentification
// You should provide it either via environment variables (ORG_GRADLE_PROJECT_intellijPublishToken) or Gradle task parameters (-Dorg.gradle.project.intellijPublishToken=mytoken)
tasks.publishPlugin {
    dependsOn("patchChangelog")
    dependsOn("useLatestVersions")
    dependsOn("dependencyCheckAnalyze")

    token.set(properties["intellijPublishToken"].toString())

    // Specify channel as per the tutorial.
    // More documentation: https://github.com/JetBrains/gradle-intellij-plugin/blob/master/README.md#publishing-dsl
    channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "stable" }.split('.').first()))
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

ktlint {
    verbose.set(true)
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// https://github.com/ben-manes/gradle-versions-plugin
fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.dependencyUpdates {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

tasks.useLatestVersions {
    // Do not update this ktlint plugin, it is unmaintained and newer versions are usually broken
    updateBlacklist = listOf("org.jlleitschuh.gradle.ktlint")
}