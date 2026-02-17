package nl.hannahsten.texifyidea.startup

import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.runWriteAction
import nl.hannahsten.texifyidea.util.files.LatexIgnoredFileMasks
import nl.hannahsten.texifyidea.util.isLatexProject

class LatexIgnoredMasksPromptActivityTest : BasePlatformTestCase() {

    private lateinit var originalMasks: String
    private var originalSuppressed: Boolean = false

    override fun setUp() {
        super.setUp()
        originalMasks = FileTypeManagerEx.getInstanceEx().ignoredFilesList
        originalSuppressed = TexifySettings.getState().suppressIgnoredMasksPrompt
        mockkStatic("nl.hannahsten.texifyidea.util.ProjectsKt")
    }

    override fun tearDown() {
        runCatching {
            runWriteAction {
                FileTypeManagerEx.getInstanceEx().ignoredFilesList = originalMasks
            }
            TexifySettings.getState().suppressIgnoredMasksPrompt = originalSuppressed
            unmockkAll()
        }
        super.tearDown()
    }

    fun testShouldPromptFalseForNonLatexProject() {
        every { project.isLatexProject() } returns false
        TexifySettings.getState().suppressIgnoredMasksPrompt = false
        LatexIgnoredFileMasks.applyMasks(emptySet())

        assertFalse(LatexIgnoredMasksPromptActivity().shouldPrompt(project))
    }

    fun testShouldPromptFalseWhenNothingMissing() {
        every { project.isLatexProject() } returns true
        TexifySettings.getState().suppressIgnoredMasksPrompt = false
        LatexIgnoredFileMasks.applyMasks(LatexIgnoredFileMasks.presetMasks)

        assertFalse(LatexIgnoredMasksPromptActivity().shouldPrompt(project))
    }

    fun testShouldPromptTrueWhenLatexProjectAndMissingMasks() {
        every { project.isLatexProject() } returns true
        TexifySettings.getState().suppressIgnoredMasksPrompt = false
        LatexIgnoredFileMasks.applyMasks(linkedSetOf("*.aux"))

        assertTrue(LatexIgnoredMasksPromptActivity().shouldPrompt(project))
    }

    fun testShouldPromptFalseWhenSuppressed() {
        every { project.isLatexProject() } returns true
        TexifySettings.getState().suppressIgnoredMasksPrompt = true
        LatexIgnoredFileMasks.applyMasks(linkedSetOf("*.aux"))

        assertFalse(LatexIgnoredMasksPromptActivity().shouldPrompt(project))
    }
}
