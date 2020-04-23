package nl.hannahsten.texifyidea.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

abstract class TexifyInspectionTriggerTestBase(val inspection: TexifyInspectionBase) : BasePlatformTestCase() {

    /**
     * Strings that do trigger [inspection].
     */
    abstract val triggers: List<String>

    /**
     * Strings that do not trigger [inspection].
     */
    abstract val noTriggers: List<String>

    fun testTriggers() {
        triggers.forEach { text ->
            myFixture.configureByText(LatexFileType, text)
            myFixture.enableInspections(inspection)
            val highlighters = myFixture.doHighlighting()
            assertTrue(highlighters.any { it.inspectionToolId == inspection.inspectionGroup.prefix + inspection.inspectionId })
        }
    }

    fun testNoTriggers() {
        noTriggers.forEach { text ->
            myFixture.configureByText(LatexFileType, text)
            myFixture.enableInspections(inspection)
            val highlighters = myFixture.doHighlighting()
            assertTrue(highlighters.none { it.inspectionToolId == inspection.inspectionGroup.prefix + inspection.inspectionId })
        }
    }
}