package nl.hannahsten.texifyidea.run.latex.ui

import com.intellij.execution.ui.*
import com.intellij.util.ui.JBUI
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationExtensionsManager
import java.awt.Font
import javax.swing.JLabel

class NewLatexSettingsEditor(settings: LatexRunConfiguration) : RunConfigurationFragmentedEditor<LatexRunConfiguration>(settings, LatexRunConfigurationExtensionsManager.instance) {


    override fun createRunFragments(): MutableList<SettingsEditorFragment<LatexRunConfiguration, *>> {
        val fragments = mutableListOf<SettingsEditorFragment<LatexRunConfiguration, *>>()

        val beforeRunComponent = BeforeRunComponent(this)
        fragments.add(BeforeRunFragment.createBeforeRun(beforeRunComponent, null))
        fragments.addAll(BeforeRunFragment.createGroup())

        fragments.add(CommonTags.parallelRun())

        // Working directory and environment variables
        val commonParameterFragments = CommonParameterFragments<LatexRunConfiguration>(mySettings.project) { false }
        fragments.addAll(commonParameterFragments.fragments)

        // Compile sequence
        val compileSequenceComponent = LatexCompileSequenceComponent(this)
        val compileSequenceFragment = LatexCompileSequenceFragment(compileSequenceComponent)
        fragments.add(compileSequenceFragment)

        // Label compile LaTex
        val compileLabel = JLabel("Compile LaTeX").apply {
            font = JBUI.Fonts.label().deriveFont(Font.BOLD)
        }
        val compileLabelFragment = SettingsEditorFragment<LatexRunConfiguration, JLabel>("compileLabel", null, null, compileLabel, 0, { _, _ -> }, { _, _ -> }) { true }
        fragments.add(compileLabelFragment)

        // LaTeX compiler
        fragments.add(CommonLatexFragments.latexCompiler(100) { s -> s::compiler })

        // LaTeX compiler arguments
        val compilerArguments = CommonLatexFragments.programArguments<LatexRunConfiguration>(
            "compilerArguments", "Compiler arguments", 200, { s -> s::compilerArguments },
            name = "Compiler a&rguments"
        )
        compilerArguments.setHint("CLI arguments for the LaTeX compiler")
        fragments.add(compilerArguments)

        // Main file
        val mainFile = CommonLatexFragments.file<LatexRunConfiguration>(
            "mainFile", "Main file", 300, mySettings.project, { s -> s::mainFile },
            name = "Main &file"
        )
        mainFile.setHint("Root file of the document to compile")
        fragments.add(mainFile)

        return fragments
    }
}