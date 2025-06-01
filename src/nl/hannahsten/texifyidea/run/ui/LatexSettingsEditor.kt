package nl.hannahsten.texifyidea.run.ui

import com.intellij.execution.ui.*
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.options.LatexRunConfigurationAbstractOutputPathOption

/**
 * UI for the [LatexRunConfiguration].
 */
class LatexSettingsEditor(settings: LatexRunConfiguration) : RunConfigurationFragmentedEditor<LatexRunConfiguration>(settings, LatexRunConfigurationExtensionsManager.instance) {

    /** Name of the group in 'Modify options' menu which contains some LaTeX settings. */
    val latexGroupName = "Compile LaTeX"

    // The fragments that are returned here are checked for modifications (i.e. compared with xml after creating a snapshot)
    override fun createRunFragments(): MutableList<SettingsEditorFragment<LatexRunConfiguration, *>> {
        val fragments = mutableListOf<SettingsEditorFragment<LatexRunConfiguration, *>>()

        // Before run optional pills
        fragments.addAll(BeforeRunFragment.createGroup())

        // Compile sequence
        val compileSequenceComponent = LatexCompileSequenceComponent(this)
        val compileSequenceFragment = LatexCompileSequenceFragment(compileSequenceComponent, -2)
        fragments.add(compileSequenceFragment)

        fragments.add(CommonParameterFragments.createHeader(latexGroupName))

        // LaTeX compiler arguments. Define them before the compiler (which comes first in the UI) because changing the compiler should change the default arguments.
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

        // Elements that do not (literally) form the command line get index 0, which sets their [com.intellij.execution.ui.SettingsEditorFragmentType]
        // to [EDITOR] and avoids strange tiling behaviour of the COMMAND_LINE type.
        // Environment variables
        fragments.add(CommonLatexFragments.createEnvParametersFragment(latexGroupName, 0))

        // Working directory
        fragments.add(CommonLatexFragments.createWorkingDirectoryFragment(latexGroupName, 0, project))

        // Output path
        fragments.add(CommonLatexFragments.createOutputPathFragment(
            latexGroupName,
            0,
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
            0,
            project,
            "auxiliary",
            reset = { s -> s.options.auxilPath.pathWithMacro ?: LatexRunConfigurationAbstractOutputPathOption.getDefault("auxil", project).pathWithMacro!! },
            apply = { s, option -> s.options.auxilPath = option },
            isDefault = { s -> s?.options?.auxilPath?.isDefault("auxil") },
            mySettings
        ))

        // Output format
        fragments.add(CommonLatexFragments.createOutputFormatFragment(latexGroupName, 0, mySettings))

        // LaTeX distribution
        fragments.add(CommonLatexFragments.createLatexDistributionFragment(latexGroupName, 0, mySettings))

        // Allow parallel run
        fragments.add(CommonTags.parallelRun())

        return fragments
    }
}