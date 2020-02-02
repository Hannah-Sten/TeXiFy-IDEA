package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class FigureNotReferencedInspectionTest : BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(LatexFigureNotReferencedInspection())
    }

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