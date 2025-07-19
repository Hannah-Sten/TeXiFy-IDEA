package nl.hannahsten.texifyidea.reference

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.updateFilesets

class InputFileReferenceTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/reference"
    }

    fun testRename() {
        myFixture.configureByFile("oldname.tex")
        myFixture.configureByText(LatexFileType, "\\input{oldname<caret>.tex}")
        myFixture.updateFilesets()

        myFixture.renameElementAtCaret("newname.tex")
        myFixture.checkResult("\\input{newname}")
    }

    fun testRenameInclude() {
        myFixture.configureByFile("oldname.tex")
        myFixture.configureByText(LatexFileType, "\\include{oldname<caret>}")
        myFixture.updateFilesets()

        myFixture.renameElementAtCaret("newname.tex")
        myFixture.checkResult("\\include{newname}")
    }

    fun testRenameSubfiles() {
        myFixture.configureByFiles("simplesubfile/subdir/onedown.tex", "simplesubfile/main.tex")
        myFixture.updateFilesets()

        myFixture.renameElementAtCaret("newmain.tex")
        myFixture.checkResultByFile("simplesubfile/subdir/onedown-after.tex")
    }

    fun testRenameSubfix() {
        myFixture.configureByFiles("subfix/main.tex", "subfix/subdir2/chapter2.tex", "subfix/subdir2/references.bib")
        myFixture.updateFilesets()
        myFixture.renameElementAtCaret("newreferences.bib")
        myFixture.checkResultByFile("subfix/main-after.tex")
    }
}