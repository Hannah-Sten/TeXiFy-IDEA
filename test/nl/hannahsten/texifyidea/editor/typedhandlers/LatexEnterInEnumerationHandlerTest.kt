package nl.hannahsten.texifyidea.editor.typedhandlers

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class LatexEnterInEnumerationHandlerTest : BasePlatformTestCase() {

    fun testItemize() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{itemize}
                \item <caret>
            \end{itemize}
            """.trimIndent()
        )
        myFixture.type("\n")
        myFixture.checkResult(
            """
            \begin{itemize}
                \item 
                \item <caret>
            \end{itemize}
            """.trimIndent()
        )
    }

    fun testItemizeSplitLine() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{itemize}
                \item This sentence is <caret>broken
            \end{itemize}
            """.trimIndent()
        )
        myFixture.testAction(ActionManager.getInstance().getAction("EditorSplitLine"))
        myFixture.checkResult(
            """
            \begin{itemize}
                \item This sentence is <caret>
                broken
            \end{itemize}
            """.trimIndent()
        )
    }

    fun testItemizeBreakLine() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{itemize}
                \item This sentence is <caret>broken
            \end{itemize}
            """.trimIndent()
        )
        myFixture.type("\n")
        myFixture.checkResult(
            """
            \begin{itemize}
                \item This sentence is 
                \item <caret>broken
            \end{itemize}
            """.trimIndent()
        )
    }

    fun testItemizeBreakLineNoItem() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{itemize}
                \item {This sentence is <caret>broken}
                \item Second item
            \end{itemize}
            """.trimIndent()
        )
        myFixture.type("\n")
        myFixture.checkResult(
            """
            \begin{itemize}
                \item {This sentence is 
                <caret>broken}
                \item Second item
            \end{itemize}
            """.trimIndent()
        )
    }

    fun testItemizeBreakLineInCommand() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{itemize}
                \item \textit{This sentence is <caret>broken}
                \item Second item
            \end{itemize}
            """.trimIndent()
        )
        myFixture.type("\n")
        myFixture.checkResult(
            """
            \begin{itemize}
                \item \textit{This sentence is 
                <caret>broken}
                \item Second item
            \end{itemize}
            """.trimIndent()
        )
    }

    fun testItemizeBreakLineInCommandExtraBrackets() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{itemize}
                \item {\textit{This sentence is <caret>broken}}
                \item Second item
            \end{itemize}
            """.trimIndent()
        )
        myFixture.type("\n")
        myFixture.checkResult(
            """
            \begin{itemize}
                \item {\textit{This sentence is 
                <caret>broken}}
                \item Second item
            \end{itemize}
            """.trimIndent()
        )
    }

    fun `test nested enumeration with prefix`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{enumerate}
                \item foo
                \item[whoo] bar
                \begin{labeling}{foobar:}
                    \item[foobar:]
                    \item[barfoo:]
                \end{labeling} <caret>
            \end{enumerate}
            """.trimIndent()
        )
        myFixture.type("\n")
        myFixture.checkResult(
            """
            \begin{enumerate}
                \item foo
                \item[whoo] bar
                \begin{labeling}{foobar:}
                    \item[foobar:]
                    \item[barfoo:]
                \end{labeling} 
                \item[whoo] 
            \end{enumerate}
            """.trimIndent()
        )
    }
}