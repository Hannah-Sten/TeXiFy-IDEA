package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.util.firstChildOfType
import org.junit.Test

class LatexPsiImplUtilTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testData/psi"
    }

    @Test
    fun testOptionalParameterSplitting() {
        // given
        val testName = getTestName(false)
        myFixture.configureByFiles("$testName.tex")

        // when
        val psiFile = PsiDocumentManager.getInstance(myFixture.project).getPsiFile(myFixture.editor.document)!!
        val element = psiFile.children.first().firstChildOfType(LatexCommands::class)!!
        val optionalParameters = element.optionalParameters

        // then
        assertEquals(optionalParameters[0], "backend")
        assertEquals(optionalParameters[1], "style")
        assertEquals(optionalParameters[2], "optionwithoutvalue")
    }
}