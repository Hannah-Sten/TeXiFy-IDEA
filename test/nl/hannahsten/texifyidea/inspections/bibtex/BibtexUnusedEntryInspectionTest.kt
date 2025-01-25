package nl.hannahsten.texifyidea.inspections.bibtex

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand
import nl.hannahsten.texifyidea.util.files.ReferencedFileSetService
import nl.hannahsten.texifyidea.util.files.rerunInspections

class BibtexUnusedEntryInspectionTest : TexifyInspectionTestBase(BibtexUnusedEntryInspection()) {

    override fun getTestDataPath(): String {
        return "test/resources/inspections/bibtex/unusedbibentry"
    }

    fun `test warnings where needed`() {
        myFixture.configureByFiles("references.bib", "main.tex")
        myFixture.checkHighlighting()
    }

    fun `test warnings where needed2`() = kotlinx.coroutines.test.runTest{
        val files = myFixture.configureByFiles("references.bib", "main.tex")
        files.forEach {
            ReferencedFileSetService.getInstance().forceRefreshCache(it)
            it.rerunInspections()
        }

        myFixture.checkHighlighting()
    }

    fun `test quick fix`() = kotlinx.coroutines.test.runTest {
        myFixture.configureByFiles("references-before.bib", "main-quick-fix.tex").forEach {
            // Refresh cache
//            ReferencedFileSetService.getInstance().referencedFileSetOf(it)
            ReferencedFileSetService.getInstance().forceRefreshCache(it)
        }
        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals("Expected number of quick fixes:", 2, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.firstOrNull()?.invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResultByFile("references-after.bib")
    }
}