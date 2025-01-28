package nl.hannahsten.texifyidea.structure.latex

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class LatexStructureViewElementTest : BasePlatformTestCase() {
    fun `test that item is added to correct level when it is more than one level higher than the previous element`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \section{s}

            \subsection{ss1}

            \subsubsection{sss}

            \paragraph{p}

            \subsection{ss2}
            """.trimIndent()
        )

        myFixture.testStructureView { component ->
            val documentChildren = (component.treeModel as LatexStructureViewModel).root.children
            // \section{s1} is the only child element of the document class element.
            assertEquals(1, documentChildren.size)
            // \subsection{ss1}
            //   ...
            // \subsection{ss2}
            assertEquals(
                listOf("\\subsection{ss1}", "\\subsection{ss2}"),
                documentChildren[0].children.map { (it as LatexStructureViewCommandElement).value.text }
            )
        }
    }

    fun `test that item is added to tree when all items before where of lower level`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \subsubsection{sss}

            \paragraph{p}

            \subsection{ss}
            """.trimIndent()
        )

        myFixture.testStructureView { component ->
            val documentChildren = (component.treeModel as LatexStructureViewModel).root.children
            // \subsubsection{sss}
            //    \paragraph{p}
            // \subsection{ss}
            assertEquals(
                listOf("\\subsubsection{sss}", "\\subsection{ss}"),
                documentChildren.map { (it as LatexStructureViewCommandElement).value.text }
            )
        }
    }

    fun `test labels are added in the correct place`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \section{s}
            \subsection{ss1}

            \label{l}

            \subsection{ss2}
            """.trimIndent()
        )

        myFixture.testStructureView { component ->
            val documentChildren = (component.treeModel as LatexStructureViewModel).root.children
            assertEquals(
                listOf("\\section{s}"),
                documentChildren.map { (it as LatexStructureViewCommandElement).value.text }
            )
            assertEquals(
                listOf("\\label{l}"),
                documentChildren.filterIsInstance<LatexStructureViewCommandElement>().first().children.first().children.map { (it as LatexStructureViewCommandElement).value.text }
            )
        }
    }
}