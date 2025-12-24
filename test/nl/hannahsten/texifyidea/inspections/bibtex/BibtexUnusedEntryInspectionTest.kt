package nl.hannahsten.texifyidea.inspections.bibtex

import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import nl.hannahsten.texifyidea.configureByFilesAndBuildFilesets
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand

class BibtexUnusedEntryInspectionTest : TexifyInspectionTestBase(BibtexUnusedEntryInspection()) {

    override fun getTestDataPath(): String = "test/resources/inspections/bibtex/unusedbibentry"

    fun `test warnings where needed`() {
        try {
            myFixture.configureByFilesAndBuildFilesets("references.bib", "main.tex")
            myFixture.checkHighlighting()
        }
        finally {
            clearAllMocks()
            unmockkAll()
        }
    }

    fun `test quick fix`() {
        try {
            myFixture.configureByFilesAndBuildFilesets("references-before.bib", "main-quick-fix.tex")
            val quickFixes = myFixture.getAllQuickFixes()
            assertEquals("Expected number of quick fixes:", 2, quickFixes.size)
            writeCommand(myFixture.project) {
                quickFixes.firstOrNull()?.invoke(myFixture.project, myFixture.editor, myFixture.file)
            }

            myFixture.checkResultByFile("references-after.bib")
        }
        finally {
            clearAllMocks()
            unmockkAll()
        }
    }
}