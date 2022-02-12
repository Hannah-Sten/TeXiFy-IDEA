package nl.hannahsten.texifyidea.intentions

import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.settings.conventions.LabelConventionType
import nl.hannahsten.texifyidea.testutils.updateConvention
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexAddLabelIntentionTest : BasePlatformTestCase() {

    fun testMissingChapterLabel() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \chapter{Chapter<caret> without label}
            \end{document}
            """.trimIndent()
        )
        val intentions = myFixture.availableIntentions
        writeCommand(myFixture.project) {
            intentions.first { i -> i.text == "Add label" }.invoke(myFixture.project, myFixture.editor, myFixture.file)
        }
        myFixture.checkResult(
            """
            \begin{document}
                \chapter{Chapter without label}\label{ch:chapter-without-label}<caret>
            \end{document}
            """.trimIndent()
        )
    }

    fun testMissingChapterLabelAtEnd() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \chapter{Chapter without label}<caret>
            \end{document}
            """.trimIndent()
        )
        val intentions = myFixture.availableIntentions
        writeCommand(myFixture.project) {
            intentions.first { i -> i.text == "Add label" }.invoke(myFixture.project, myFixture.editor, myFixture.file)
        }
        myFixture.checkResult(
            """
            \begin{document}
                \chapter{Chapter without label}\label{ch:chapter-without-label}<caret>
            \end{document}
            """.trimIndent()
        )
    }

    fun testMissingSectionLabelWithComma() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \section{Section about A, B and C}<caret>
            \end{document}
            """.trimIndent()
        )
        val intentions = myFixture.availableIntentions
        writeCommand(myFixture.project) {
            intentions.first { i -> i.text == "Add label" }.invoke(myFixture.project, myFixture.editor, myFixture.file)
        }
        myFixture.checkResult(
            """
            \begin{document}
                \section{Section about A, B and C}\label{sec:section-about-a-b-and-c}<caret>
            \end{document}
            """.trimIndent()
        )
    }

    fun testLabelForItem() {
        myFixture.updateConvention { s ->
            s.getLabelConvention("\\item", LabelConventionType.COMMAND)!!.enabled = true
        }
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \begin{enumerate}
                    \item<caret> Some item
                \end{enumerate}
            \end{document}
            """.trimIndent()
        )
        TemplateManagerImpl.setTemplateTesting(myFixture.projectDisposable)
        val intentions = myFixture.availableIntentions
        writeCommand(myFixture.project) {
            intentions.first { i -> i.text == "Add label" }.invoke(myFixture.project, myFixture.editor, myFixture.file)
        }
        myFixture.checkResult(
            """
            \begin{document}
                \begin{enumerate}
                    \item\label{itm:<caret>} Some item
                \end{enumerate}
            \end{document}
            """.trimIndent()
        )
    }

    fun testAddLabelForEnvironment() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \begin{lstlisting}<caret>
                \end{lstlisting}
            \end{document}
            """.trimIndent()
        )
        val intentions = myFixture.availableIntentions
        writeCommand(myFixture.project) {
            intentions.first { i -> i.text == "Add label" }.invoke(myFixture.project, myFixture.editor, myFixture.file)
        }
        myFixture.checkResult(
            """
            \begin{document}
                \begin{lstlisting}[label={lst:lstlisting}]
                \end{lstlisting}
            \end{document}
            """.trimIndent()
        )
    }
}