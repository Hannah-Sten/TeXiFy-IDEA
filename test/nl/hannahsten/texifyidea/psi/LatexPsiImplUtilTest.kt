package nl.hannahsten.texifyidea.psi

import com.intellij.openapi.paths.WebReference
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.util.parser.*
import org.junit.Test

class LatexPsiImplUtilTest : BasePlatformTestCase() {

    private val url = "https://github.com/Hannah-Sten/TeXiFy-IDEA"

    override fun getTestDataPath(): String {
        return "test/resources/psi"
    }

    private val optionalParameters =
        """\usepackage[backend=biber,style={alphabetic order},optionwithoutvalue]{biblatex}"""

    private val requiredParameters =
        """\bibliography{library1,library2}"""

    @Test
    fun testRequiredParameterSplitting() {
        // given
        myFixture.configureByText(LatexFileType, requiredParameters)

        // when
        val requiredParameters = PsiDocumentManager.getInstance(myFixture.project)
            .getPsiFile(myFixture.editor.document)!!
            .children
            .first()
            .firstChildOfType(LatexCommands::class)!!
            .getRequiredParameters()
        // then
        assertEquals("library1,library2", requiredParameters[0])
    }

    @Test
    fun testOptionalParameterSplitting() {
        // given
        myFixture.configureByText(LatexFileType, optionalParameters)

        // when
        val psiFile = PsiDocumentManager.getInstance(myFixture.project).getPsiFile(myFixture.editor.document)!!
        val element = psiFile.children.first().firstChildOfType(LatexCommands::class)!!
        val optionalParameters = element.getOptionalParameterMap().toStringMap()

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
        val optionalParameters = element.getOptionalParameterMap().toStringMap().keys.toList()

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
        val optionalParameters = element.getOptionalParameterMap().toStringMap()

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

    @Test
    fun testBeginCommandOptionalParameterSplitting() {
        myFixture.configureByText(LatexFileType, """\begin{lstlisting}[language=Python, label={lst:listing}]\end{lstlisting}""")

        val psiFile = PsiDocumentManager.getInstance(myFixture.project).getPsiFile(myFixture.editor.document)!!
        val element = psiFile.children.first().firstChildOfType(LatexBeginCommand::class)!!
        val optionalParameters = element.getOptionalParameterMap().toStringMap()

        assertEquals("Python", optionalParameters["language"])
        assertEquals("lst:listing", optionalParameters["label"])
    }

    @Test
    fun testLabelFirstArgumentEnvironment() {
        myFixture.configureByText(LatexFileType, """\begin{lstlisting}[label={lst:listing}, language=Python]\end{lstlisting}""")

        val psiFile = PsiDocumentManager.getInstance(myFixture.project).getPsiFile(myFixture.editor.document)!!
        val element = psiFile.children.first().firstChildOfType(LatexEnvironment::class)!!

        assertEquals("lst:listing", element.getLabel())
    }

    @Test
    fun testLabelNotFirstParameterEnvironment() {
        myFixture.configureByText(LatexFileType, """\begin{lstlisting}[language=Python, label={lst:listing}]\end{lstlisting}""")

        val psiFile = PsiDocumentManager.getInstance(myFixture.project).getPsiFile(myFixture.editor.document)!!
        val element = psiFile.children.first().firstChildOfType(LatexEnvironment::class)!!

        assertEquals("lst:listing", element.getLabel())
    }

    @Test
    fun testDefaultOptionalParameter() {
        myFixture.configureByText(
            LatexFileType,
            """
            % Second optional parameter of \newcommand makes the first parameter of \lvec optional with as default value 'n' (see LaTeX Companion page 845)
            \newcommand{\lvec}[2][n]{\ensuremath{#2_1+\cdots + #2_{#1}}}
            For the series \lvec{x} we have \[ \lvec{x} = \sum_{k=1}^{n} G_{\lvec[k]{y}} \]
            """.trimIndent()
        )

        val psiFile = PsiDocumentManager.getInstance(myFixture.project).getPsiFile(myFixture.editor.document)!!
        val element = psiFile.firstChildOfType(LatexCommands::class)!!
        assertTrue("2" in element.getOptionalParameterMap().toStringMap())
        assertTrue("n" in element.getOptionalParameterMap().toStringMap())
    }
}