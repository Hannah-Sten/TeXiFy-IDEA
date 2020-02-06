package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class FigureNotReferencedInspectionTest : TexifyInspectionTestBase(LatexFigureNotReferencedInspection()) {

    fun testFigureNotReferencedWarning() {
        myFixture.configureByText(LatexFileType, """
            \usepackage{listings}
            \begin{document}
                \begin{figure}
                    <weak_warning descr="Figure is not referenced">\label{fig:some-figure}</weak_warning>
                \end{figure}
            \end{document}
        """.trimIndent())
        myFixture.checkHighlighting(false, false, true, false)
    }
}