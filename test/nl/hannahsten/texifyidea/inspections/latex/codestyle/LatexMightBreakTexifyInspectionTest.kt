package nl.hannahsten.texifyidea.inspections.latex.codestyle

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexMightBreakTexifyInspectionTest : TexifyInspectionTestBase(LatexMightBreakTexifyInspection()) {

    fun testTruePositive() = testHighlighting(
        """
            <error descr="Redefining \newcommand might break TeXiFy functionality">\def</error>\newcommand\noop
            
            <error descr="Redefining \documentclass might break TeXiFy functionality">\renewcommand{\documentclass}{\noclass}</error>
        """.trimIndent()
    )

    fun testTrueNegative() = testHighlighting(
        """
            \def\venue{openair}
            \ifthenelse {\equal{\venue}{openair}}{
                \def\venueString{parking}
            }{
                \def\venueString{cultural centre}
            }
            \def\someCommand{xxx} 
        """.trimIndent()
    )
}