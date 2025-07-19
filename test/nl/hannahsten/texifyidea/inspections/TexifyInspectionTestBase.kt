package nl.hannahsten.texifyidea.inspections

import com.intellij.codeInspection.InspectionsBundle
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.configureByFilesAndBuildFilesets
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.testutils.writeCommand

abstract class TexifyInspectionTestBase(vararg val inspections: LocalInspectionTool) : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(*inspections)
    }

    protected fun configureByFilesAndBuildFilesets(vararg files: String): Array<out PsiFile> {
        return myFixture.configureByFilesAndBuildFilesets(*files)
    }

    protected fun testHighlighting(text: String) {
        myFixture.configureByText(LatexFileType, text)
        myFixture.checkHighlighting()
    }

    protected fun testQuickFix(
        before: String,
        after: String,
        numberOfFixes: Int = 1,
        selectedFix: Int = 1,
        fileName: String = "test.tex"
    ) {
        myFixture.configureByText(fileName, before)
        // Collect the quick fixed before going into write action, to avoid AssertionError: Must not start highlighting from within write action.
        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals("Expected number of quick fixes:", numberOfFixes, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes[selectedFix - 1]?.invoke(myFixture.project, myFixture.editor, myFixture.file)
        }
        myFixture.checkResult(after)
    }

    protected fun testNamedQuickFix(before: String, after: String, quickFixName: String, numberOfFixes: Int = 1) {
        myFixture.configureByText(LatexFileType, before)
        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals("Expected number of quick fixes:", numberOfFixes, quickFixes.size)
        val selectedFix = quickFixes.firstOrNull { it.text == quickFixName } ?: quickFixes.firstOrNull()
        writeCommand(myFixture.project) {
            selectedFix?.invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult(after)
    }

    protected fun testQuickFixAll(before: String, after: String, quickFixName: String, numberOfFixes: Int) {
        myFixture.configureByText(LatexFileType, before)
        assertEquals("Expected number of quick fixes:", numberOfFixes, myFixture.getAllQuickFixes().size)

        // Find the fix all problems in this file intention for this inspection.
        val fixAllIntention = myFixture.getAvailableIntention(
            InspectionsBundle.message("fix.all.inspection.problems.in.file", quickFixName)
        ) ?: return
        myFixture.launchAction(fixAllIntention)

        myFixture.checkResult(after)
    }
}