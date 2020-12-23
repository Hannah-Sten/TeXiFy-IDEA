package nl.hannahsten.texifyidea.reference

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class LabelDefinitionReferenceTest : BasePlatformTestCase() {

    fun `test rename of label command`() {
        myFixture.configureByText(LatexFileType, """\label{test} \ref{test<caret>}""")
        myFixture.renameElementAtCaret("renamed")
        myFixture.checkResult("""\label{renamed} \ref{renamed<caret>}""")
    }

    fun `test rename of label in environment`() {
        myFixture.configureByText(LatexFileType,
            """
            \begin{listing}[label=test]
            \end{listing}
            \ref{test<caret>}
            """
            .trimMargin())
        myFixture.renameElementAtCaret("renamed")
        myFixture.checkResult(
            """
            \begin{listing}[label=renamed]
            \end{listing}
            \ref{renamed<caret>}
            """
        )
    }

    fun `test rename of label in command`() {
        myFixture.configureByText(LatexFileType,
            """
            \lstinputlisting[label=test]{inputfile}
            \ref{test<caret>}
            """
                .trimMargin())
        myFixture.renameElementAtCaret("renamed")
        myFixture.checkResult(
            """
            \lstinputlisting[label=renamed]{inputfile} 
            \ref{renamed<caret>}
            """
        )
    }

    fun `test rename of label in group`() {
        myFixture.configureByText(LatexFileType,
            """
            \lstinputlisting[label={test}]{inputfile}
            \ref{test<caret>}
            """
                .trimMargin())
        myFixture.renameElementAtCaret("renamed")
        myFixture.checkResult(
            """
            \lstinputlisting[label={renamed}]{inputfile} 
            \ref{renamed<caret>}
            """
        )
    }
}