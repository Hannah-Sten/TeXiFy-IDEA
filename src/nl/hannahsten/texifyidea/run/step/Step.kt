package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.actionSystem.DataContext
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.ui.LatexCompileSequenceComponent
import nl.hannahsten.texifyidea.run.ui.console.LatexExecutionConsole
import javax.swing.Icon

/**
 * A step in the run configuration flow of compiling a LaTeX document.
 *
 * This can be anything that executes something, e.g., a latex or bibtex compiler or opening a pdf file in a pdf viewer.
 */
interface Step : Cloneable {

    val provider: StepProvider

    var configuration: LatexRunConfiguration

    val name: String

    fun configure(context: DataContext, button: LatexCompileSequenceComponent.StepButton)

    fun onConfigured(button: LatexCompileSequenceComponent.StepButton) = button.updateButton()

    fun execute(id: String, console: LatexExecutionConsole): ProcessHandler

    fun isValid() = true

    fun getDescription(): String = provider.name

    fun getIcon(): Icon = provider.icon

    public override fun clone(): Step
}