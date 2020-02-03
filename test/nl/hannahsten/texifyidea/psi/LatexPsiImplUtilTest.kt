package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.util.firstChildOfType
import org.junit.Test

class LatexPsiImplUtilTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "test/resources/psi"
    }

    @Test
    fun testOptionalParameterSplitting() {
        // given
        myFixture.configureByFiles("OptionalParameters.tex")

        // when
        val psiFile = PsiDocumentManager.getInstance(myFixture.project).getPsiFile(myFixture.editor.document)!!
        val element = psiFile.children.first().firstChildOfType(LatexCommands::class)!!
        val optionalParameters = element.optionalParameters

        // then
        assertEquals("biber", optionalParameters["backend"])
        assertEquals("alphabetic order", optionalParameters["style"])
        assertEquals("", optionalParameters["optionwithoutvalue"])
    }

    @Test
    fun testOptionalParameterNameOrder() {
        // given
        myFixture.configureByFiles("OptionalParameters.tex")

        // when
        val psiFile = PsiDocumentManager.getInstance(myFixture.project).getPsiFile(myFixture.editor.document)!!
        val element = psiFile.children.first().firstChildOfType(LatexCommands::class)!!
        val optionalParameters = element.optionalParameters.keys.toList()

        // then
        assertEquals("backend", optionalParameters[0])
        assertEquals("style", optionalParameters[1])
        assertEquals("optionwithoutvalue", optionalParameters[2])
    }
}