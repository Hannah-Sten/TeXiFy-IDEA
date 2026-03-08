package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ui.WrapLayout
import nl.hannahsten.texifyidea.run.latex.*
import nl.hannahsten.texifyidea.run.latex.ui.LatexSettingsEditor
import javax.swing.JComponent
import javax.swing.JLabel

class LatexCompileSequenceComponentTest : BasePlatformTestCase() {

    fun testAddStepUpdatesSharedShadowSteps() {
        withEditor(configWithSteps(LatexCompileStepOptions(), PdfViewerStepOptions())) { editor, component ->
            component.resetEditorFrom()
            component.addStepForTest(LatexStepType.BIBTEX)

            assertEquals(listOf("latex-compile", "pdf-viewer", "bibtex"), editor.shadowSteps.map { it.type })
        }
    }

    fun testUsesHorizontalWrapLayout() {
        withEditor(configWithSteps()) { _, component ->
            val layout = component.layout as? WrapLayout

            assertNotNull(layout)
            assertEquals(java.awt.FlowLayout.LEFT, layout!!.alignment)
        }
    }

    fun testResetClearsSelection() {
        withEditor(configWithSteps(LatexCompileStepOptions(), PdfViewerStepOptions())) { _, component ->
            component.resetEditorFrom()

            assertEquals(-1, component.selectedStepIndex())
            assertNull(component.selectedStepType())
        }
    }

    fun testSelectStepChangesSelection() {
        withEditor(configWithSteps(LatexCompileStepOptions(), PdfViewerStepOptions())) { _, component ->
            component.resetEditorFrom()
            component.selectStep(1)

            assertEquals(1, component.selectedStepIndex())
            assertEquals("pdf-viewer", component.selectedStepType())
        }
    }

    fun testMoveStepKeepsSelectionOnMovedStep() {
        withEditor(configWithSteps(LatexCompileStepOptions(), BibtexStepOptions(), PdfViewerStepOptions())) { editor, component ->
            component.resetEditorFrom()
            component.selectStep(2)

            component.moveStep(from = 2, to = 0)

            assertEquals(0, component.selectedStepIndex())
            assertEquals("pdf-viewer", component.selectedStepType())
            assertEquals(listOf("pdf-viewer", "latex-compile", "bibtex"), editor.shadowSteps.map { it.type })
        }
    }

    fun testAutoConfigureReplacesSequenceUsingEditorFlow() {
        withEditor(configWithSteps(BibtexStepOptions(), PdfViewerStepOptions())) { editor, component ->
            component.resetEditorFrom()
            component.triggerAutoConfigureForTest()

            assertEquals(listOf("bibtex", "latex-compile", "latex-compile", "pdf-viewer"), component.currentStepTypesForTest())
            assertEquals(listOf("bibtex", "latex-compile", "latex-compile", "pdf-viewer"), editor.shadowSteps.map { it.type })
        }
    }

    fun testWrappedComponentShowsAutoConfigureWithoutSelection() {
        withEditor(configWithSteps()) { _, component ->
            val wrapped = LatexCompileSequenceFragment.createWrappedComponent(component)

            assertTrue(containsButtonText(wrapped, "Auto configure"))
        }
    }

    private fun containsButtonText(component: JComponent, text: String): Boolean {
        if (component is JLabel && component.text == text) {
            return true
        }
        return component.components.any { child ->
            child is JComponent && containsButtonText(child, text)
        }
    }

    private fun configWithSteps(vararg steps: LatexStepRunConfigurationOptions): LatexRunConfiguration = LatexRunConfiguration(
        project,
        LatexRunConfigurationProducer().configurationFactory,
        "Test run config"
    ).apply {
        configOptions.steps = steps.map { it.deepCopy() }.toMutableList()
    }

    private fun withEditor(
        runConfig: LatexRunConfiguration,
        action: (LatexSettingsEditor, LatexCompileSequenceComponent) -> Unit,
    ) {
        val editor = LatexSettingsEditor(runConfig)
        editor.shadowSteps.clear()
        editor.shadowSteps.addAll(runConfig.copyStepsForUi())
        action(editor, editor.compileSequenceComponent)
    }
}
