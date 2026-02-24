package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latex.StepSchemaReadStatus
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import com.intellij.util.ui.WrapLayout

class LatexCompileSequenceComponentTest : BasePlatformTestCase() {

    fun testApplyWritesInferredStepSchemaToRunConfiguration() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.compiler = LatexCompiler.PDFLATEX
        runConfig.compileTwice = true
        runConfig.pdfViewer = PdfViewer.firstAvailableViewer
        runConfig.stepSchemaStatus = StepSchemaReadStatus.MISSING
        runConfig.stepSchemaTypes = emptyList()

        val disposable = Disposer.newDisposable()
        try {
            val component = LatexCompileSequenceComponent(disposable)
            component.resetEditorFrom(runConfig)
            component.applyEditorTo(runConfig)
        }
        finally {
            Disposer.dispose(disposable)
        }

        assertEquals(StepSchemaReadStatus.PARSED, runConfig.stepSchemaStatus)
        assertEquals(listOf("latex-compile", "latex-compile", "pdf-viewer"), runConfig.stepSchemaTypes)
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

    fun testResetSelectsFirstStepAndNotifiesSelection() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.stepSchemaTypes = listOf("latex-compile", "pdf-viewer")
        runConfig.stepSchemaStatus = StepSchemaReadStatus.PARSED

        val disposable = Disposer.newDisposable()
        try {
            val component = LatexCompileSequenceComponent(disposable)
            var callbackIndex = -2
            var callbackType: String? = "init"
            component.onSelectionChanged = { index, type ->
                callbackIndex = index
                callbackType = type
            }

            component.resetEditorFrom(runConfig)

            assertEquals(0, component.selectedStepIndex())
            assertEquals("latex-compile", component.selectedStepType())
            assertEquals(0, callbackIndex)
            assertEquals("latex-compile", callbackType)
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
        runConfig.stepSchemaTypes = listOf("latex-compile", "pdf-viewer")
        runConfig.stepSchemaStatus = StepSchemaReadStatus.PARSED

        val disposable = Disposer.newDisposable()
        try {
            val component = LatexCompileSequenceComponent(disposable)
            var callbackIndex = -1
            var callbackType: String? = null
            component.onSelectionChanged = { index, type ->
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

    fun testRemoveSelectedStepFallsBackToFirstVisibleStep() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.stepSchemaTypes = listOf("latex-compile", "pdf-viewer")
        runConfig.stepSchemaStatus = StepSchemaReadStatus.PARSED

        val disposable = Disposer.newDisposable()
        try {
            val component = LatexCompileSequenceComponent(disposable)
            component.resetEditorFrom(runConfig)
            component.selectStep(1)

            component.removeStepAt(1)

            assertEquals(0, component.selectedStepIndex())
            assertEquals("latex-compile", component.selectedStepType())
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
        runConfig.stepSchemaTypes = listOf("latex-compile", "legacy-bibtex", "pdf-viewer")
        runConfig.stepSchemaStatus = StepSchemaReadStatus.PARSED

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
}
