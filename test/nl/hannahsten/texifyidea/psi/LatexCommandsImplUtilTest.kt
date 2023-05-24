package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.util.psi.firstChildOfType
import nl.hannahsten.texifyidea.util.psi.getOptionalParameterMapFromParameters
import nl.hannahsten.texifyidea.util.psi.toStringMap
import org.junit.Test

class LatexCommandsImplUtilTest : BasePlatformTestCase() {

    @Test
    fun `test simple optional parameters map`() {
        // given
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \lstinputlisting[param1=value1,param2=value2,param3]{some/file}
            \end{document}
            """.trimIndent()
        )

        // when
        val parameters = PsiDocumentManager.getInstance(myFixture.project)
            .getPsiFile(myFixture.editor.document)!!
            .children
            .first()
            .firstChildOfType(LatexCommands::class)!!
            .parameterList

        val map = getOptionalParameterMapFromParameters(parameters).toStringMap()
        assertSize(3, map.keys)
        assertEquals("value1", map["param1"])
        assertEquals("value2", map["param2"])
        assertEquals("", map["param3"])
    }

    @Test
    fun `test simple optional parameters map with whitespaces`() {
        // given
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \lstinputlisting[param1    = value1  , param2=value2]{some/file}
            \end{document}
            """.trimIndent()
        )

        // when
        val parameters = PsiDocumentManager.getInstance(myFixture.project)
            .getPsiFile(myFixture.editor.document)!!
            .children
            .first()
            .firstChildOfType(LatexCommands::class)!!
            .parameterList

        val map = getOptionalParameterMapFromParameters(parameters).toStringMap()
        assertSize(2, map.keys)
        assertEquals("value1", map["param1"])
        assertEquals("value2", map["param2"])
    }

    @Test
    fun `test multiple optional parameters map`() {
        // given
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \lstinputlisting[param1=value1][param2=value2,param3]{some/file}
            \end{document}
            """.trimIndent()
        )

        // when
        val parameters = PsiDocumentManager.getInstance(myFixture.project)
            .getPsiFile(myFixture.editor.document)!!
            .children
            .first()
            .firstChildOfType(LatexCommands::class)!!
            .parameterList

        val map = getOptionalParameterMapFromParameters(parameters).toStringMap()
        assertSize(3, map.keys)
        assertEquals("value1", map["param1"])
        assertEquals("value2", map["param2"])
        assertEquals("", map["param3"])
    }

    @Test
    fun `test grouped optional parameters map`() {
        // given
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \lstinputlisting[param1={value11,value12},param2=value2,{param3,param4}=value3,onlykey,param5={x=y},param6={ with space }{parts}]{some/file}
            \end{document}
            """.trimIndent()
        )

        // when
        val parameters = PsiDocumentManager.getInstance(myFixture.project)
            .getPsiFile(myFixture.editor.document)!!
            .children
            .first()
            .firstChildOfType(LatexCommands::class)!!
            .parameterList

        val map = getOptionalParameterMapFromParameters(parameters).toStringMap()
        assertSize(6, map.keys)
        assertEquals("value11,value12", map["param1"])
        assertEquals("value2", map["param2"])
        assertEquals("value3", map["param3,param4"])
        assertEquals("", map["onlykey"])
        assertEquals("x=y", map["param5"])
        assertEquals(" with space parts", map["param6"])
    }

    @Test
    fun `test grouped key optional parameters map`() {
        // given
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \lstinputlisting[{param1}=value1,param2=value2]{some/file}
            \end{document}
            """.trimIndent()
        )

        // when
        val parameters = PsiDocumentManager.getInstance(myFixture.project)
            .getPsiFile(myFixture.editor.document)!!
            .children
            .first()
            .firstChildOfType(LatexCommands::class)!!
            .parameterList

        val map = getOptionalParameterMapFromParameters(parameters).toStringMap()
        assertSize(2, map.keys)
        assertEquals("value1", map["param1"])
        assertEquals("value2", map["param2"])
    }

    @Test
    fun `test command value optional parameters map`() {
        // given
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \lstinputlisting[linewidth=\textwidth]{some/file}
            \end{document}
            """.trimIndent()
        )

        // when
        val parameters = PsiDocumentManager.getInstance(myFixture.project)
            .getPsiFile(myFixture.editor.document)!!
            .children
            .first()
            .firstChildOfType(LatexCommands::class)!!
            .parameterList

        val map = getOptionalParameterMapFromParameters(parameters).toStringMap()
        assertSize(1, map.keys)
        assertEquals("\\textwidth", map["linewidth"])
    }

    @Test
    fun `test empty value parameters map`() {
        // given
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \lstinputlisting[param1=,param2,param3=value]{some/file}
            \end{document}
            """.trimIndent()
        )

        // when
        val parameters = PsiDocumentManager.getInstance(myFixture.project)
            .getPsiFile(myFixture.editor.document)!!
            .children
            .first()
            .firstChildOfType(LatexCommands::class)!!
            .parameterList

        val map = getOptionalParameterMapFromParameters(parameters).toStringMap()
        assertSize(3, map.keys)
        assertEquals("", map["param1"])
        assertEquals("", map["param2"])
        assertEquals("value", map["param3"])
    }

    @Test
    fun `test parameters ending with comma`() {
        // given
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \lstinputlisting[param1=value1,param2=value2,]{some/file}
            \end{document}
            """.trimIndent()
        )

        // when
        val parameters = PsiDocumentManager.getInstance(myFixture.project)
            .getPsiFile(myFixture.editor.document)!!
            .children
            .first()
            .firstChildOfType(LatexCommands::class)!!
            .parameterList

        val map = getOptionalParameterMapFromParameters(parameters).toStringMap()
        assertSize(2, map.keys)
        assertEquals("value1", map["param1"])
        assertEquals("value2", map["param2"])
    }
}