package nl.hannahsten.texifyidea.psi

import com.intellij.openapi.paths.WebReference
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.util.firstChildOfType
import nl.hannahsten.texifyidea.util.lastChildOfType
import nl.hannahsten.texifyidea.util.requiredParameters
import org.junit.Test

class LatexPsiImplUtilTest : BasePlatformTestCase() {
    private val url = "https://github.com/Hannah-Sten/TeXiFy-IDEA"

    override fun getTestDataPath(): String {
        return "test/resources/psi"
    }

    val optionalParameters = """\usepackage[backend=biber,style={alphabetic order},optionwithoutvalue]{biblatex}"""

    @Test
    fun testOptionalParameterSplitting() {
        // given
        myFixture.configureByText(LatexFileType, optionalParameters)

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
        myFixture.configureByText(LatexFileType, optionalParameters)

        // when
        val psiFile = PsiDocumentManager.getInstance(myFixture.project).getPsiFile(myFixture.editor.document)!!
        val element = psiFile.children.first().firstChildOfType(LatexCommands::class)!!
        val optionalParameters = element.optionalParameters.keys.toList()

        // then
        assertEquals("backend", optionalParameters[0])
        assertEquals("style", optionalParameters[1])
        assertEquals("optionwithoutvalue", optionalParameters[2])
    }

    @Test
    fun testEmptyOptionalParameters() {
        // given
        myFixture.configureByText(LatexFileType, """\usepackage{bibtex}""")

        // when
        val psiFile = PsiDocumentManager.getInstance(myFixture.project).getPsiFile(myFixture.editor.document)!!
        val element = psiFile.children.first().firstChildOfType(LatexCommands::class)!!
        val optionalParameters = element.optionalParameters

        // then
        assertEmpty(optionalParameters.toList())
    }

    @Test
    fun testExtractUrlReferences() {
        myFixture.configureByText(LatexFileType, "\\url{$url} \\href{$url}{TeXiFy}")

        val psiFile = PsiDocumentManager.getInstance(myFixture.project).getPsiFile(myFixture.editor.document)!!
        val urlElement = psiFile.firstChildOfType(LatexCommands::class)!!
        val hrefElement = psiFile.lastChildOfType(LatexCommands::class)!!

        UsefulTestCase.assertContainsElements(
                urlElement.extractUrlReferences(urlElement.requiredParameters().first())
                        .map { (it as WebReference).url },
                url
        )

        UsefulTestCase.assertContainsElements(
                hrefElement.extractUrlReferences(hrefElement.requiredParameters().first())
                        .map { (it as WebReference).url },
                url
        )
    }
}