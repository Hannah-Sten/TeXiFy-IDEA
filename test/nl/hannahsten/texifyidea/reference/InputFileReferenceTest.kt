package nl.hannahsten.texifyidea.reference

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class InputFileReferenceTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/reference"
    }

    fun testRename() {
        myFixture.configureByFile("oldname.tex")
        myFixture.configureByText(LatexFileType, "\\input{oldname<caret>.tex}")
        myFixture.renameElementAtCaret("newname.tex")
        myFixture.checkResult("\\input{newname}")
    }

    fun testRenameInclude() {
        myFixture.configureByFile("oldname.tex")
        myFixture.configureByText(LatexFileType, "\\include{oldname<caret>}")
        myFixture.renameElementAtCaret("newname.tex")
        myFixture.checkResult("\\include{newname}")
    }

    fun testRenameSubfiles() {
        myFixture.configureByFiles("simplesubfile/subdir/onedown.tex", "simplesubfile/main.tex")
        myFixture.renameElementAtCaret("newmain.tex")
        myFixture.checkResultByFile("simplesubfile/subdir/onedown-after.tex")
    }
}