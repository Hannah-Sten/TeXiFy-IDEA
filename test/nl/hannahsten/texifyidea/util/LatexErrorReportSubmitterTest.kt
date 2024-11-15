package nl.hannahsten.texifyidea.util

import nl.hannahsten.texifyidea.LatexErrorReportSubmitter
import org.junit.Assert.assertEquals
import org.junit.Test

class LatexErrorReportSubmitterTest {

    @Test
    fun testLongStacktrace() {
        val stacktrace = """
            com.intellij.diagnostic.PluginException: Cannot create extension (class=nl.hannahsten.texifyidea.index.file.LatexExternalEnvironmentIndex) [Plugin: nl.rubensten.texifyidea]
                at com.intellij.serviceContainer.ComponentManagerImpl.createError(ComponentManagerImpl.kt:980)
                at com.intellij.openapi.extensions.impl.XmlExtensionAdapter.doCreateInstance(XmlExtensionAdapter.kt:73)
                at com.intellij.openapi.extensions.impl.XmlExtensionAdapter.createInstance(XmlExtensionAdapter.kt:33)
                at com.intellij.openapi.extensions.impl.ExtensionPointImpl.processAdapter(ExtensionPointImpl.kt:403)
                at com.intellij.openapi.extensions.impl.ExtensionPointImpl.createExtensionInstances(ExtensionPointImpl.kt:376)
                at com.intellij.openapi.extensions.impl.ExtensionPointImpl.getExtensionList(ExtensionPointImpl.kt:222)
                at com.intellij.openapi.extensions.ExtensionPointName.getExtensionList(ExtensionPointName.kt:54)
                at com.intellij.indexing.shared.platform.api.IdeSharedIndexesState.<init>(IdeSharedIndexesState.java:38)
                at nl.hannahsten.texifyidea.settings.codestyle.LatexCodeStyleSettingsProvider.createCustomSettings(LatexCodeStyleSettingsProvider.kt:21)
                at nl.hannahsten.texifyidea.settings.codestyle.LatexCodeStyleSettingsProvider.createCustomSettings(LatexCodeStyleSettingsProvider.kt:19)
                at com.intellij.psi.codeStyle.CustomCodeStyleSettingsManager.addCustomSettings(CustomCodeStyleSettingsManager.java:32)
                at com.intellij.indexing.shared.platform.api.SharedIndexInfrastructureVersion.getIdeVersion(SharedIndexInfrastructureVersion.java:68)
                at com.intellij.indexing.shared.platform.impl.SharedIndexStorageUtil.openFileBasedIndexChunks(SharedIndexStorageUtil.java:54)
                at nl.hannahsten.texifyidea.settings.codestyle.LatexCodeStyleSettingsProvider.createCustomSettings(LatexCodeStyleSettingsProvider.kt:21)
                at com.intellij.indexing.shared.platform.impl.SharedIndexChunkConfigurationImpl.openFileBasedIndexChunk(SharedIndexChunkConfigurationImpl.java:657)
                at com.intellij.indexing.shared.platform.impl.SharedIndexChunkConfigurationImpl.registerChunk(SharedIndexChunkConfigurationImpl.java:618)
                at com.intellij.indexing.shared.platform.impl.SharedIndexChunkConfigurationImpl.openChunkForProject(SharedIndexChunkConfigurationImpl.java:484)
            Caused by: java.util.concurrent.ExecutionException: java.lang.InterruptedException
                at java.base/java.util.concurrent.CompletableFuture.reportGet(CompletableFuture.java:396)
                at java.base/java.util.concurrent.CompletableFuture.get(CompletableFuture.java:2096)
                at org.jetbrains.concurrency.AsyncPromise.get(AsyncPromise.kt:51)
                at org.jetbrains.concurrency.AsyncPromise.blockingGet(AsyncPromise.kt:130)
                at nl.hannahsten.texifyidea.settings.codestyle.LatexCodeStyleSettingsProvider.createCustomSettings(LatexCodeStyleSettingsProvider.kt:21)
                at nl.hannahsten.texifyidea.settings.codestyle.LatexCodeStyleSettingsProvider.createCustomSettings(LatexCodeStyleSettingsProvider.kt:19)
        """.trimIndent()
        val trimmedStacktrace = LatexErrorReportSubmitter.Util.filterInterestingLines(stacktrace)
        val expected = """
            com.intellij.diagnostic.PluginException: Cannot create extension (class=nl.hannahsten.texifyidea.index.file.LatexExternalEnvironmentIndex) [Plugin: nl.rubensten.texifyidea]
                at com.intellij.serviceContainer.ComponentManagerImpl.createError(ComponentManagerImpl.kt:980)
                at com.intellij.openapi.extensions.impl.XmlExtensionAdapter.doCreateInstance(XmlExtensionAdapter.kt:73)
                at com.intellij.openapi.extensions.impl.XmlExtensionAdapter.createInstance(XmlExtensionAdapter.kt:33)
                at com.intellij.openapi.extensions.impl.ExtensionPointImpl.processAdapter(ExtensionPointImpl.kt:403)
                at com.intellij.openapi.extensions.impl.ExtensionPointImpl.createExtensionInstances(ExtensionPointImpl.kt:376)
                at com.intellij.openapi.extensions.impl.ExtensionPointImpl.getExtensionList(ExtensionPointImpl.kt:222)
                at com.intellij.openapi.extensions.ExtensionPointName.getExtensionList(ExtensionPointName.kt:54)
                at com.intellij.indexing.shared.platform.api.IdeSharedIndexesState.<init>(IdeSharedIndexesState.java:38)
                at nl.hannahsten.texifyidea.settings.codestyle.LatexCodeStyleSettingsProvider.createCustomSettings(LatexCodeStyleSettingsProvider.kt:21)
                at nl.hannahsten.texifyidea.settings.codestyle.LatexCodeStyleSettingsProvider.createCustomSettings(LatexCodeStyleSettingsProvider.kt:19)
                at com.intellij.psi.codeStyle.CustomCodeStyleSettingsManager.addCustomSettings(CustomCodeStyleSettingsManager.java:32)
                (...)
                at com.intellij.indexing.shared.platform.impl.SharedIndexStorageUtil.openFileBasedIndexChunks(SharedIndexStorageUtil.java:54)
                at nl.hannahsten.texifyidea.settings.codestyle.LatexCodeStyleSettingsProvider.createCustomSettings(LatexCodeStyleSettingsProvider.kt:21)
                at com.intellij.indexing.shared.platform.impl.SharedIndexChunkConfigurationImpl.openFileBasedIndexChunk(SharedIndexChunkConfigurationImpl.java:657)
                (...)
                at com.intellij.indexing.shared.platform.impl.SharedIndexChunkConfigurationImpl.openChunkForProject(SharedIndexChunkConfigurationImpl.java:484)
            Caused by: java.util.concurrent.ExecutionException: java.lang.InterruptedException
                at java.base/java.util.concurrent.CompletableFuture.reportGet(CompletableFuture.java:396)
                (...)
                at org.jetbrains.concurrency.AsyncPromise.blockingGet(AsyncPromise.kt:130)
                at nl.hannahsten.texifyidea.settings.codestyle.LatexCodeStyleSettingsProvider.createCustomSettings(LatexCodeStyleSettingsProvider.kt:21)
                at nl.hannahsten.texifyidea.settings.codestyle.LatexCodeStyleSettingsProvider.createCustomSettings(LatexCodeStyleSettingsProvider.kt:19)
        """.trimIndent()
        assertEquals(expected, trimmedStacktrace)
    }
}