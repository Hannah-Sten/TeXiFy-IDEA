package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.LatexStepType
import nl.hannahsten.texifyidea.run.latex.MakeglossariesStepOptions

internal object MakeglossariesCommandRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.MAKEGLOSSARIES

    override val aliases: Set<String> = setOf(
        type,
        "makeglossaries"
    )

    override fun create(stepConfig: LatexStepRunConfigurationOptions): LatexRunStep = CommandLineRunStep(
        configId = stepConfig.id,
        id = type,
        commandLineSupplier = { context ->
            val options = stepConfig as? MakeglossariesStepOptions
                ?: error("Expected MakeglossariesStepOptions for $type, but got ${stepConfig::class.simpleName}")
            val executable = options.executable?.trim().takeUnless { it.isNullOrBlank() } ?: "makeglossaries"
            val arguments = options.arguments?.trim().takeUnless { it.isNullOrBlank() } ?: context.mainFile.nameWithoutExtension
            "$executable $arguments"
        },
        workingDirectorySupplier = { context ->
            val options = stepConfig as? MakeglossariesStepOptions
                ?: error("Expected MakeglossariesStepOptions for $type, but got ${stepConfig::class.simpleName}")
            CommandLineRunStep.resolveWorkingDirectory(context, options.workingDirectoryPath)
        },
    )
}
