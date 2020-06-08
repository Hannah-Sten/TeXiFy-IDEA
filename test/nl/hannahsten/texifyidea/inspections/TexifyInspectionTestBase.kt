package nl.hannahsten.texifyidea.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.testutils.writeCommand

abstract class TexifyInspectionTestBase(vararg val inspections: LocalInspectionTool) : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(*inspections)
    }

    protected fun testQuickFix(before: String, after: String) {
        myFixture.configureByText(LatexFileType, before)
        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals("Expected number of quick fixes:", 1, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult(after)
    }
}