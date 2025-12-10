package nl.hannahsten.texifyidea.settings.sdk

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for [LatexProjectSdkSetupValidator].
 */
class LatexProjectSdkSetupValidatorTest : BasePlatformTestCase() {

    private lateinit var validator: LatexProjectSdkSetupValidator

    override fun setUp() {
        super.setUp()
        validator = LatexProjectSdkSetupValidator()
    }

    fun testIsApplicableForLatexFile() {
        val file = myFixture.addFileToProject("test.tex", "\\documentclass{article}")
        val result = validator.isApplicableFor(project, file.virtualFile)
        assertTrue("Expected validator to be applicable for .tex files", result)
    }

    fun testIsApplicableForNonLatexFile() {
        val file = myFixture.addFileToProject("test.java", "public class Test {}")
        val result = validator.isApplicableFor(project, file.virtualFile)
        assertFalse("Expected validator to not be applicable for .java files", result)
    }

    fun testIsApplicableForTextFile() {
        val file = myFixture.addFileToProject("test.txt", "Hello world")
        val result = validator.isApplicableFor(project, file.virtualFile)
        assertFalse("Expected validator to not be applicable for .txt files", result)
    }

    /**
     * Test that getErrorMessage returns correct result based on pdflatex availability.
     * This test checks both branches - either pdflatex is in PATH (no error) or not (error shown).
     */
    fun testGetErrorMessageWhenNoSdkConfigured() {
        val file = myFixture.addFileToProject("test.tex", "\\documentclass{article}")
        val errorMessage = validator.getErrorMessage(project, file.virtualFile)

        if (LatexProjectSdkSetupValidator.Cache.isPdflatexInPath) {
            // If pdflatex is in PATH, no error should be shown
            assertNull("Expected no error when pdflatex is in PATH", errorMessage)
        }
        else {
            // If pdflatex is not in PATH and no SDK configured, an error should be shown
            assertNotNull("Expected error message when no SDK and pdflatex not in PATH", errorMessage)
            assertTrue(
                "Error message should mention LaTeX SDK setup",
                errorMessage?.contains("LaTeX SDK") == true || errorMessage?.contains("LaTeX installation") == true
            )
        }
    }

    fun testGetFixHandlerReturnsNonNull() {
        val file = myFixture.addFileToProject("test.tex", "\\documentclass{article}")
        val handler = validator.getFixHandler(project, file.virtualFile)
        assertNotNull("Expected getFixHandler to return a non-null handler", handler)
    }

    fun testIsApplicableForBibtexFile() {
        val file = myFixture.addFileToProject("references.bib", "@article{key,}")
        // BibTeX files should also be handled by LaTeX tooling
        // This is just to ensure no exception is thrown
        validator.isApplicableFor(project, file.virtualFile)
    }
}
