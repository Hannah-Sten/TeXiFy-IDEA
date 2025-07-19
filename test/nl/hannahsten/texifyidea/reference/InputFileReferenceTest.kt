package nl.hannahsten.texifyidea.reference

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.index.LatexProjectStructure

class InputFileReferenceTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/reference"
    }

    fun testRename() {
        myFixture.configureByFile("oldname.tex")
        myFixture.configureByText(LatexFileType, "\\input{oldname<caret>.tex}")
        LatexProjectStructure.testOnlyUpdateFilesets(project)
        myFixture.renameElementAtCaret("newname.tex")
        myFixture.checkResult("\\input{newname}")
    }

    fun testRenameInclude() {
        myFixture.configureByFile("oldname.tex")
        myFixture.configureByText(LatexFileType, "\\include{oldname<caret>}")
        LatexProjectStructure.testOnlyUpdateFilesets(project)
        myFixture.renameElementAtCaret("newname.tex")
        myFixture.checkResult("\\include{newname}")
    }

    fun testRenameSubfiles() {
        myFixture.configureByFiles("simplesubfile/subdir/onedown.tex", "simplesubfile/main.tex")
        LatexProjectStructure.testOnlyUpdateFilesets(project)
        myFixture.renameElementAtCaret("newmain.tex")
        myFixture.checkResultByFile("simplesubfile/subdir/onedown-after.tex")
    }

    fun testRenameSubfix() {
        myFixture.configureByFiles("subfix/main.tex", "subfix/subdir2/chapter2.tex", "subfix/subdir2/references.bib")
        myFixture.renameElementAtCaret("newreferences.bib")
        LatexProjectStructure.testOnlyUpdateFilesets(project)
        myFixture.checkResultByFile("subfix/main-after.tex")
    }
}