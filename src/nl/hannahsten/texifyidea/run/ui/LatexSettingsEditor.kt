package nl.hannahsten.texifyidea.run.ui

import com.intellij.execution.ui.*
import com.intellij.util.ui.JBUI
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.LatexRunConfigurationProducer
import java.awt.Font
import javax.swing.JLabel

/**
 * UI for the [LatexRunConfiguration].
 */
class LatexSettingsEditor(settings: LatexRunConfiguration) : RunConfigurationFragmentedEditor<LatexRunConfiguration>(settings, LatexRunConfigurationExtensionsManager.instance) {

    // The fragments that are returned here are checked for modifications (i.e. compared with xml after creating a snapshot)
    override fun createRunFragments(): MutableList<SettingsEditorFragment<LatexRunConfiguration, *>> {
        val fragments = mutableListOf<SettingsEditorFragment<LatexRunConfiguration, *>>()

        // Compile sequence
        val compileSequenceComponent = LatexCompileSequenceComponent(this)
        val compileSequenceFragment = LatexCompileSequenceFragment(compileSequenceComponent)
        fragments.add(compileSequenceFragment)

        val beforeRunComponent = BeforeRunComponent(this)
        fragments.add(BeforeRunFragment.createBeforeRun(beforeRunComponent, null))
        fragments.addAll(BeforeRunFragment.createGroup())

        fragments.add(CommonTags.parallelRun())

        // todo move to inside step
//        // Working directory and environment variables
//        val commonParameterFragments = CommonParameterFragments<LatexRunConfiguration>(mySettings.project) { false }
//        fragments.addAll(commonParameterFragments.fragments)

        // Label compile LaTeX
        val compileLabel = JLabel("Compile LaTeX").apply {
            font = JBUI.Fonts.label().deriveFont(Font.BOLD)
        }
        val compileLabelFragment = SettingsEditorFragment<LatexRunConfiguration, JLabel>("compileLabel", null, null, compileLabel, -1, { _, _ -> }, { _, _ -> }) { true }
        fragments.add(compileLabelFragment)

//        // LaTeX compiler
//        fragments.add(CommonLatexFragments.latexCompiler(100) { s -> s::compiler })

        // LaTeX compiler arguments
        val compilerArguments = CommonLatexFragments.programArguments(
            "compilerArguments", "Compiler arguments", 200, { s -> (s.configuration as LatexRunConfiguration)::compilerArguments },
            name = "Compiler arguments"
        )
        compilerArguments.setHint("CLI arguments for the LaTeX compiler")
        fragments.add(compilerArguments)

//        // Main file
//        val mainFile = CommonLatexFragments.file<LatexRunConfiguration>(
//            "mainFile", "Main file", 300, mySettings.project, { s -> s::mainFile },
//            name = "Main file"
//        )
//        mainFile.setHint("Root file of the document to compile")
//        fragments.add(mainFile)

        return fragments
    }
}