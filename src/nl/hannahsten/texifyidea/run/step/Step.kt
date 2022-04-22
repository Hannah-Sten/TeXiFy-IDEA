package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.actionSystem.DataContext
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.ui.console.LatexExecutionConsole
import javax.swing.JButton

/**
 * A step in the run configuration flow of compiling a LaTeX document.
 *
 * This can be anything that executes something, e.g., a latex or bibtex compiler or opening a pdf file in a pdf viewer.
 */
interface Step : Cloneable {

    val provider: StepProvider

    var configuration: LatexRunConfiguration

    val name: String

    fun configure(context: DataContext, button: JButton)

    fun execute(id: String, console: LatexExecutionConsole): ProcessHandler

    fun isValid() = true

    fun getDescription(): String = provider.name

    public override fun clone(): Step
}