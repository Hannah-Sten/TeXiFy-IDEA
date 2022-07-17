package nl.hannahsten.texifyidea.run.ui

import com.intellij.execution.ui.*
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.options.LatexRunConfigurationAbstractOutputPathOption
import java.awt.Font
import javax.swing.JLabel

/**
 * UI for the [LatexRunConfiguration].
 */
class LatexSettingsEditor(settings: LatexRunConfiguration) : RunConfigurationFragmentedEditor<LatexRunConfiguration>(settings, LatexRunConfigurationExtensionsManager.instance) {

    /** Name of the group in 'Modify options' menu which contains some LaTeX settings. */
    val latexGroupName = "Compile LaTeX"

    // The fragments that are returned here are checked for modifications (i.e. compared with xml after creating a snapshot)
    override fun createRunFragments(): MutableList<SettingsEditorFragment<LatexRunConfiguration, *>> {
        val fragments = mutableListOf<SettingsEditorFragment<LatexRunConfiguration, *>>()

        // Before run
        val beforeRunComponent = BeforeRunComponent(this)
        fragments.add(BeforeRunFragment.createBeforeRun(beforeRunComponent, null))
        fragments.addAll(BeforeRunFragment.createGroup())

        // Compile sequence
        val compileSequenceComponent = LatexCompileSequenceComponent(this)
        // todo Avoid next fragments being placed next to this one? (it reflows, but we want a hardcoded linebreak here)
        compileSequenceComponent.minimumSize = JBDimension(300, 30)
        val compileSequenceFragment = LatexCompileSequenceFragment(compileSequenceComponent, 1)
        fragments.add(compileSequenceFragment)

        // Label
        val compileLabel = JLabel("Compile LaTeX").apply {
            font = JBUI.Fonts.label().deriveFont(Font.BOLD)
        }
        val compileLabelFragment = SettingsEditorFragment<LatexRunConfiguration, JLabel>("compileLabel", null, null, compileLabel, 2, { _, _ -> }, { _, _ -> }) { true }
        fragments.add(compileLabelFragment)

        // LaTeX compiler arguments
        val compilerArguments = CommonLatexFragments.createProgramArgumentsFragment(
            "compilerArguments", "Compiler arguments", 4,
            { s -> (s.configuration as LatexRunConfiguration).options::compilerArguments },
            { s -> (s.configuration as? LatexRunConfiguration)?.options?.compilerArguments?.isNotEmpty() == true },
            name = "Add compiler arguments", latexGroupName
        )

        // LaTeX compiler
        fragments.add(CommonLatexFragments.createLatexCompilerFragment(3, compilerArguments) { s -> s.options::compiler })

        // Compiler arguments, part 2
        compilerArguments.setHint("CLI arguments for the LaTeX compiler")
        fragments.add(compilerArguments)

        // Main file
        val mainFile = CommonLatexFragments.createMainFileFragment(5, project)
        fragments.add(mainFile)

        // Environment variables
        fragments.add(CommonLatexFragments.createEnvParametersFragment(latexGroupName, 6))

        // Working directory
        fragments.add(CommonLatexFragments.createWorkingDirectoryFragment(latexGroupName, 7, project))

        // Output path
        fragments.add(CommonLatexFragments.createOutputPathFragment(
            latexGroupName,
            8,
            project,
            "output",
            reset = { s -> s.options.outputPath.pathWithMacro ?: "" }, // todo LatexRunConfigurationAbstractOutputPathOption.getDefault("out", project).pathWithMacro!! },
            apply = { s, option -> s.options.outputPath = option },
            isDefault = { s -> s?.options?.outputPath?.isDefault("out") },
            mySettings
        ))

        // Path for auxiliary output files
        fragments.add(CommonLatexFragments.createOutputPathFragment(
            latexGroupName,
            8,
            project,
            "auxiliary",
            reset = { s -> s.options.auxilPath.pathWithMacro ?: LatexRunConfigurationAbstractOutputPathOption.getDefault("auxil", project).pathWithMacro!! },
            apply = { s, option -> s.options.auxilPath = option },
            isDefault = { s -> s?.options?.auxilPath?.isDefault("auxil") },
            mySettings
        ))

        // Output format
        fragments.add(CommonLatexFragments.createOutputFormatFragment(latexGroupName, 10, mySettings))

        // LaTeX distribution
        fragments.add(CommonLatexFragments.createLatexDistributionFragment(latexGroupName, 11, mySettings))

        // Allow parallel run
        fragments.add(CommonTags.parallelRun())

        return fragments
    }
}