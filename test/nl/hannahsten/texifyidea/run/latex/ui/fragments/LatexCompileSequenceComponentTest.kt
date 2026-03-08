package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ui.WrapLayout
import nl.hannahsten.texifyidea.run.latex.*
import javax.swing.JComponent
import javax.swing.JLabel

class LatexCompileSequenceComponentTest : BasePlatformTestCase() {

    fun testAddStepUpdatesSharedShadowSteps() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.configOptions.steps = mutableListOf(LatexCompileStepOptions(), PdfViewerStepOptions())
        val shadowSteps = runConfig.copyStepsForUi()

        val disposable = Disposer.newDisposable()
        try {
            val component = LatexCompileSequenceComponent(disposable, shadowSteps, project)
            component.resetEditorFrom()
            component.addStepForTest(LatexStepType.BIBTEX)
        }
        finally {
            Disposer.dispose(disposable)
        }

        assertEquals(listOf("latex-compile", "pdf-viewer", "bibtex"), shadowSteps.map { it.type })
    }

    fun testUsesHorizontalWrapLayout() {
        val disposable = Disposer.newDisposable()
        try {
            val component = LatexCompileSequenceComponent(disposable, mutableListOf(), project)
            val layout = component.layout as? WrapLayout

            assertNotNull(layout)
            assertEquals(java.awt.FlowLayout.LEFT, layout!!.alignment)
        }
        finally {
            Disposer.dispose(disposable)
        }
    }

    fun testResetClearsSelectionAndNotifiesNoSelection() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.configOptions.steps = mutableListOf(LatexCompileStepOptions(), PdfViewerStepOptions())

        val disposable = Disposer.newDisposable()
        try {
            val shadowSteps = runConfig.copyStepsForUi()
            val component = LatexCompileSequenceComponent(disposable, shadowSteps, project)
            var callbackIndex = -2
            var callbackType: String? = "init"
            component.onSelectionChanged = { index, _, type ->
                callbackIndex = index
                callbackType = type
            }

            component.resetEditorFrom()

            assertEquals(-1, component.selectedStepIndex())
            assertNull(component.selectedStepType())
            assertEquals(-1, callbackIndex)
            assertNull(callbackType)
        }
        finally {
            Disposer.dispose(disposable)
        }
    }

    fun testSelectStepChangesSelectionAndNotifies() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.configOptions.steps = mutableListOf(LatexCompileStepOptions(), PdfViewerStepOptions())

        val disposable = Disposer.newDisposable()
        try {
            val shadowSteps = runConfig.copyStepsForUi()
            val component = LatexCompileSequenceComponent(disposable, shadowSteps, project)
            var callbackIndex = -1
            var callbackType: String? = null
            component.onSelectionChanged = { index, _, type ->
                callbackIndex = index
                callbackType = type
            }

            component.resetEditorFrom()
            component.selectStep(1)

            assertEquals(1, component.selectedStepIndex())
            assertEquals("pdf-viewer", component.selectedStepType())
            assertEquals(1, callbackIndex)
            assertEquals("pdf-viewer", callbackType)
        }
        finally {
            Disposer.dispose(disposable)
        }
    }

    fun testMoveStepKeepsSelectionOnMovedStep() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.configOptions.steps = mutableListOf(LatexCompileStepOptions(), BibtexStepOptions(), PdfViewerStepOptions())

        val disposable = Disposer.newDisposable()
        try {
            val shadowSteps = runConfig.copyStepsForUi()
            val component = LatexCompileSequenceComponent(disposable, shadowSteps, project)
            component.resetEditorFrom()
            component.selectStep(2)

            component.moveStep(from = 2, to = 0)

            assertEquals(0, component.selectedStepIndex())
            assertEquals("pdf-viewer", component.selectedStepType())
            assertEquals(listOf("pdf-viewer", "latex-compile", "bibtex"), shadowSteps.map { it.type })
        }
        finally {
            Disposer.dispose(disposable)
        }
    }

    fun testAutoConfigureReplacesSequenceAndNotifiesChanges() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.configOptions.steps = mutableListOf(LatexCompileStepOptions(), PdfViewerStepOptions())

        val disposable = Disposer.newDisposable()
        try {
            val shadowSteps = runConfig.copyStepsForUi()
            val component = LatexCompileSequenceComponent(disposable, shadowSteps, project)
            var onStepsChangedCount = 0
            component.onStepsChanged = { onStepsChangedCount++ }
            component.onAutoConfigureRequested = {
                listOf(
                    LatexCompileStepOptions(),
                    BibtexStepOptions(),
                    LatexCompileStepOptions(),
                    PdfViewerStepOptions()
                )
            }

            component.resetEditorFrom()
            component.triggerAutoConfigureForTest()

            assertEquals(listOf("latex-compile", "bibtex", "latex-compile", "pdf-viewer"), component.currentStepTypesForTest())
            assertEquals(listOf("latex-compile", "bibtex", "latex-compile", "pdf-viewer"), shadowSteps.map { it.type })
            assertTrue(onStepsChangedCount >= 2)
        }
        finally {
            Disposer.dispose(disposable)
        }
    }

    fun testWrappedComponentShowsAutoConfigureWithoutSelection() {
        val disposable = Disposer.newDisposable()
        try {
            val component = LatexCompileSequenceComponent(disposable, mutableListOf(), project)
            val wrapped = LatexCompileSequenceFragment.createWrappedComponent(component)

            assertTrue(containsButtonText(wrapped, "Auto configure"))
        }
        finally {
            Disposer.dispose(disposable)
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
}
