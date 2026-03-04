package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ui.WrapLayout
import nl.hannahsten.texifyidea.run.latex.*

class LatexCompileSequenceComponentTest : BasePlatformTestCase() {

    fun testApplyWritesStepListToRunConfiguration() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.configOptions.steps = mutableListOf(LatexCompileStepOptions(), PdfViewerStepOptions())

        val disposable = Disposer.newDisposable()
        try {
            val component = LatexCompileSequenceComponent(disposable)
            component.resetEditorFrom(runConfig)
            component.applyEditorTo(runConfig)
        }
        finally {
            Disposer.dispose(disposable)
        }

        assertEquals(listOf("latex-compile", "pdf-viewer"), runConfig.configOptions.steps.map { it.type })
    }

    fun testUsesHorizontalWrapLayout() {
        val disposable = Disposer.newDisposable()
        try {
            val component = LatexCompileSequenceComponent(disposable)
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
            val component = LatexCompileSequenceComponent(disposable)
            var callbackIndex = -2
            var callbackType: String? = "init"
            component.onSelectionChanged = { index, _, type ->
                callbackIndex = index
                callbackType = type
            }

            component.resetEditorFrom(runConfig)

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
            val component = LatexCompileSequenceComponent(disposable)
            var callbackIndex = -1
            var callbackType: String? = null
            component.onSelectionChanged = { index, _, type ->
                callbackIndex = index
                callbackType = type
            }

            component.resetEditorFrom(runConfig)
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
            val component = LatexCompileSequenceComponent(disposable)
            component.resetEditorFrom(runConfig)
            component.selectStep(2)

            component.moveStep(from = 2, to = 0)

            assertEquals(0, component.selectedStepIndex())
            assertEquals("pdf-viewer", component.selectedStepType())
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
            val component = LatexCompileSequenceComponent(disposable)
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

            component.resetEditorFrom(runConfig)
            component.triggerAutoConfigureForTest()

            assertEquals(listOf("latex-compile", "bibtex", "latex-compile", "pdf-viewer"), component.currentStepTypesForTest())
            assertTrue(onStepsChangedCount >= 2)
        }
        finally {
            Disposer.dispose(disposable)
        }
    }
}
