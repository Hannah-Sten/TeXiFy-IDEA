package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.util.runWriteAction

class LatexIgnoredFileMasksTest : BasePlatformTestCase() {

    private lateinit var originalMasks: String

    override fun setUp() {
        super.setUp()
        originalMasks = FileTypeManagerEx.getInstanceEx().ignoredFilesList
    }

    override fun tearDown() {
        runCatching {
            runWriteAction {
                FileTypeManagerEx.getInstanceEx().ignoredFilesList = originalMasks
            }
        }
        super.tearDown()
    }

    fun testParseMasksTrimsAndDeduplicates() {
        val parsed = LatexIgnoredFileMasks.parseMasks("*.aux; *.log ;*.aux;;")

        assertEquals(linkedSetOf("*.aux", "*.log"), parsed)
    }

    fun testMergeWithPresetKeepsCustomAndAddsMissingPreset() {
        val merged = LatexIgnoredFileMasks.mergeWithPreset(linkedSetOf("*.custom", "*.aux"))

        assertTrue("*.custom" in merged)
        assertTrue("*.aux" in merged)
        assertTrue("*.bbl" in merged)
    }

    fun testFindMissingMasksEmptyWhenPresetAlreadyPresent() {
        val missing = LatexIgnoredFileMasks.findMissingMasks(LatexIgnoredFileMasks.presetMasks)

        assertTrue(missing.isEmpty())
    }

    fun testApplyMasksDoesNotDropExistingCustomMasks() {
        LatexIgnoredFileMasks.applyMasks(linkedSetOf("*.mytmp"))

        val merged = LatexIgnoredFileMasks.mergeWithPreset(LatexIgnoredFileMasks.getCurrentMasks())
        LatexIgnoredFileMasks.applyMasks(merged)

        assertTrue("*.mytmp" in LatexIgnoredFileMasks.getCurrentMasks())
        assertTrue("*.aux" in LatexIgnoredFileMasks.getCurrentMasks())
    }
}
