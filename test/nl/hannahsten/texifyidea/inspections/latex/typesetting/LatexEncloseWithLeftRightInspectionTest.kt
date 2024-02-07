package nl.hannahsten.texifyidea.inspections.latex.typesetting

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexEncloseWithLeftRightInspectionTest : TexifyInspectionTestBase(LatexEncloseWithLeftRightInspection()) {

    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            ${'$'} <weak_warning descr="Parentheses pair could be replaced by \left(..\right)">(</weak_warning>\frac 1 2<weak_warning descr="Parentheses pair could be replaced by \left(..\right)">)</weak_warning>${'$'}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testQuickfix() {
        myFixture.configureByText(
            LatexFileType,
            """
            ${'$'} (\frac 1 2)${'$'}
            """.trimIndent()
        )

        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(2, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult(
            """
            ${'$'} \left(\frac 1 2\right)${'$'}
            """.trimIndent()
        )
    }

    fun testAllQuickFixes() {
        myFixture.configureByText(
            LatexFileType,
            """
            ${'$'} ((\frac 1 2))${'$'}
            """.trimIndent()
        )

        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(4, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.forEach { it.invoke(myFixture.project, myFixture.editor, myFixture.file) }
        }

        myFixture.checkResult(
            """
            ${'$'} \left(\left(\frac 1 2\right)\right)${'$'}
            """.trimIndent()
        )
    }

    fun testAllQuickFixesWithPictureArgument() {
        myFixture.configureByText(
            LatexFileType,
            """
            ${'$'} (\frac {\cos(\frac{1}{2} \pi)}{2})${'$'}
            """.trimIndent()
        )

        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(4, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.forEach { it.invoke(myFixture.project, myFixture.editor, myFixture.file) }
        }

        myFixture.checkResult(
            """
            ${'$'} \left(\frac {\cos\left(\frac{1}{2} \pi\right)}{2}\right)${'$'}
            """.trimIndent()
        )
    }
}