package nl.hannahsten.texifyidea.inspections.latex.typesetting

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.updateFilesets

class LatexMissingGlossaryReferenceInspectionTest : TexifyInspectionTestBase(LatexMissingGlossaryReferenceInspection()) {

    fun testMissingReference() {
        myFixture.configureByText(LatexFileType, """\newglossaryentry{sample}{name={sample},description={an example}} \gls{sample} \Glslink{sample} <warning descr="Missing glossary reference">sample</warning>""")
        myFixture.checkHighlighting()
    }

    fun testAddGls() {
        testQuickFix(
            """
                \newglossaryentry{sample}{name={sample},description={an example}} \gls{sample} sample text
            """.trimIndent(),
            """
                \newglossaryentry{sample}{name={sample},description={an example}} \gls{sample} \gls{sample} text
            """.trimIndent()
        )
    }

    fun testNewCommand() {
        myFixture.configureByText(LatexFileType, """\newcommand{\mygls}{\newabbreviation{name}{\ensuremath{#1}}{long}} Some text""")
        myFixture.checkHighlighting()
    }

    fun testNoWarningForUnfinishedGlossaryEntry() {
        myFixture.configureByText(
            LatexFileType,
            """
                \newglossaryentry{}{}{}
                \newglossaryentry
                some text
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNoWarning() {
        myFixture.configureByText(
            "mypackage.sty",
            """
                \protected@edef\@do@newglossaryentry{%
                  \noexpand\newglossaryentry{\the\glslabeltok}%
                  {%
                    type=\acronymtype,%
                    name={\expandonce{\acronymentry{##2}}},%
                    sort={\acronymsort{\the\glsshorttok}{\the\glslongtok}},%
                    text={\the\glsshorttok},%
                    short={\the\glsshorttok},%
                    shortplural={\the\glsshorttok\noexpand\acrpluralsuffix},%
                    long={\the\glslongtok},%
                    longplural={\the\glslongtok\noexpand\glspluralsuffix},%
                    \GenericAcronymFields,%
                    \the\glskeylisttok
                  }%
                }%
            """.trimIndent(),
        ) // a fake package that makes definition (copied fragment from glossaries.sty), but has no actual glossary entries
        myFixture.configureByText(
            "main.tex",
            """
            \documentclass{article}
            \usepackage{glossaries}
            \usepackage{mypackage}
            \makenoidxglossaries 
            \newglossaryentry{sample}{name={sample},description={an example}}

            \begin{document}
                A \gls{sample}.
                Text in a sentence. % false positive 'missing glossary reference'
                \printnoidxglossaries 
            \end{document}
            """.trimIndent()
        )
        myFixture.updateFilesets()
        myFixture.checkHighlighting()
    }

    fun testEntryInSeparateFile() {
        myFixture.configureByText(
            "glossary.tex",
            """
            \newglossaryentry{sample}{name={sample},description={an example}}
            """.trimIndent()
        )
        myFixture.configureByText(
            "main.tex",
            """
            \documentclass{article}
            \usepackage{glossaries}
            \input{glossary.tex}
            
            \begin{document}
                A \gls{sample}.
                <warning descr="Missing glossary reference">sample</warning>
            \end{document}
            """.trimIndent()
        )
        myFixture.updateFilesets()
        myFixture.checkHighlighting()
    }

    fun testAcronym() {
        testQuickFix(
            """
            \documentclass{article}
            \usepackage{acronym}
            \begin{document}
            	\begin{acronym}
            		\acro{CD}{Compact Disc}
            	\end{acronym}
            	Play CD.
            \end{document}
            """.trimIndent(),
            """
            \documentclass{article}
            \usepackage{acronym}
            \begin{document}
            	\begin{acronym}
            		\acro{CD}{Compact Disc}
            	\end{acronym}
                Play \ac{CD}.
            \end{document}
            """.trimIndent(),
            updateCommand = true,
        )
    }
}