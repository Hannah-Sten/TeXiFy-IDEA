package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latex.StepSchemaReadStatus
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import javax.swing.BoxLayout

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

    fun testUsesVerticalBoxLayout() {
        val disposable = Disposer.newDisposable()
        try {
            val component = LatexCompileSequenceComponent(disposable)
            val layout = component.layout as? BoxLayout

            assertNotNull(layout)
            assertEquals(BoxLayout.Y_AXIS, layout!!.axis)
        }
        finally {
            Disposer.dispose(disposable)
        }
    }
}
