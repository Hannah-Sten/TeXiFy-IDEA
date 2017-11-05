package nl.rubensten.texifyidea.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import nl.rubensten.texifyidea.run.compiler.BibliographyCompiler
import org.jdom.Element

/**
 *
 * @author Sten Wessel
 */
class BibtexRunConfiguration(project: Project, factory: ConfigurationFactory, name: String) : RunConfigurationBase(project, factory, name), LocatableConfiguration {
    companion object {
        private val PARENT_ELEMENT = "texify-bibtex"
        private val COMPILER = "compiler"
    }

    var compiler: BibliographyCompiler? = null

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = BibtexSettingsEditor(project)

    override fun checkConfiguration() {
        if (compiler == null) {
            throw RuntimeConfigurationError("No compiler specified.")
        }
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return BibtexCommandLineState(environment, this)
    }

    override fun readExternal(element: Element) {
        super<RunConfigurationBase>.readExternal(element)

        val parent = element.getChild(PARENT_ELEMENT)

        compiler = try {
            BibliographyCompiler.valueOf(parent.getChildText(COMPILER))
        }
        catch (e: IllegalArgumentException) {
            null
        }
    }

    override fun writeExternal(element: Element) {
        super<RunConfigurationBase>.writeExternal(element)

        val parent = element.getChild(PARENT_ELEMENT) ?: Element(PARENT_ELEMENT).apply { element.addContent(this) }
        parent.removeContent()

        parent.addContent(Element(COMPILER).apply { text = compiler?.name ?: "" })
    }

    override fun isGeneratedName() = false
}
