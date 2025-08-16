package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.updateCommandDef

class LatexCompletionTest : BasePlatformTestCase() {

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    fun testCompleteLatexReferences() {
        // given
        myFixture.configureByText(LatexFileType, """\ap<caret>""")

        // when
        val result = myFixture.complete(CompletionType.BASIC)

        // then
        if (result == null) {
            // single candidate autocompletion
            myFixture.checkResult("\\appendix<caret>")
        }
        else {
            assertTrue("LaTeX autocompletion should be available", result.any { it.lookupString.startsWith("\\appendix") })
        }
    }

    fun testCompleteCustomCommandReferences() {
        // given
        myFixture.configureByText(
            LatexFileType,
            """
            \newcommand{\hi}{hi}
            \h<caret>
            """.trimIndent()
        )
        myFixture.updateCommandDef()

        // when
        val result = myFixture.complete(CompletionType.BASIC)

        if (result == null) {
            // single candidate autocompletion
            myFixture.checkResult("\\hi<caret>")
        }
        else {
            // when multiple candidates are available
            assertTrue("LaTeX autocompletion of custom commands should be available", result.any { it.lookupString == "\\hi" })
        }
    }

    fun testCompleteCustomColorDefinitions() {
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{xcolor}
            \colorlet{fadedred}{red!70!}
            \color{r<caret>}
            """.trimIndent()
        )
        myFixture.updateCommandDef()

        val result = myFixture.complete(CompletionType.BASIC)

        assertTrue("fadedred should be available", result.any { it.lookupString == "fadedred" })
    }

    fun testCompletionInCustomArgument() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newcommand{\hello}[1]{hello #1}
            \hello{\te<caret>}
            """.trimIndent()
        )

        val result = myFixture.complete(CompletionType.BASIC)

        assertTrue(result.any { it.lookupString.startsWith("\\textbf") })
    }

    fun testCustomCommandAliasCompletion() {
        myFixture.configureByText(
            LatexFileType,
            """
             \begin{thebibliography}{9}
                 \bibitem{testkey}
                 Reference.
             \end{thebibliography}

             \newcommand{\mycite}[1]{\cite{#1}}

             \mycite{<caret>}
            """.trimIndent()
        )
        myFixture.updateCommandDef()

        val result = myFixture.complete(CompletionType.BASIC)
        assertTrue(result.any { it.lookupString == "testkey" })
    }

    // Test doesn't work before, now work with context-aware completion!
    fun testTwoLevelCustomCommandAliasCompletion() {
        myFixture.configureByText(
            LatexFileType,
            """
             \begin{thebibliography}{9}
                 \bibitem{testkey}
                 Reference.
             \end{thebibliography}

             \newcommand{\mycite}[1]{\cite{#1}}
             \newcommand{\myothercite}[1]{This is \mycite{#1}}
             \myothercite{<caret>}
            """.trimIndent()
        )
        myFixture.updateCommandDef()
        val result = myFixture.complete(CompletionType.BASIC)
        assertTrue(result.any { it.lookupString == "testkey" })
    }

    fun testLabelCompletion() {
        myFixture.configureByText(
            LatexFileType,
            """
            \label{label1}
            \label{label2}

            ~\ref{la<caret>}
            """.trimIndent()
        )

        val result = myFixture.complete(CompletionType.BASIC)

        assertTrue(result.any { it.lookupString == "label1" })
        assertTrue(result.any { it.lookupString == "label2" })
    }

    // TODO: The following can be implemented but not now
//    fun testCustomLabelAliasCompletion() {
//        myFixture.configureByText(
//            LatexFileType,
//            """
//            \newcommand{\mylabel}[1]{\label{#1}}
//
//            \mylabel{label1}
//            \label{label2}
//
//            ~\ref{la<caret>}
//            """.trimIndent()
//        )
//
//        val result = myFixture.complete(CompletionType.BASIC)
//
//        assertTrue(result.any { it.lookupString == "label1" })
//    }

    // Test only works when no other tests are run
    // fun testCustomLabelPositionAliasCompletion() {
    //     myFixture.configureByText(LatexFileType, """
    //         \newcommand{\mylabel}[2]{\section{#1}\label{sec:#2}}
    //
    //         \mylabel{section1}{label1}
    //         \label{label2}
    //
    //         ~\ref{<caret>}
    //     """.trimIndent())
    //
    //     CommandManager.updateAliases(Magic.Command.labelDefinition, project)
    //     val result = myFixture.complete(CompletionType.BASIC)
    //
    //     assertEquals(2, result.size)
    //     assertTrue(result.any { it.lookupString == "label1" })
    //     assertFalse(result.any { it.lookupString == "section1" })
    //     assertFalse(result.any { it.lookupString == "sec:#2" })
    // }
}