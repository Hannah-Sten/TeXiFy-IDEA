package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.LatexStepType
import nl.hannahsten.texifyidea.run.latex.PythontexStepOptions

internal object PythontexCommandRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.PYTHONTEX

    override val aliases: Set<String> = setOf(
        type,
        "pythontex"
    )

    override fun create(stepConfig: LatexStepRunConfigurationOptions): LatexRunStep = CommandLineRunStep(
        configId = stepConfig.id,
        id = type,
        commandLineSupplier = { context ->
            val options = stepConfig as? PythontexStepOptions
                ?: error("Expected PythontexStepOptions for $type, but got ${stepConfig::class.simpleName}")
            val executable = options.executable?.trim().takeUnless { it.isNullOrBlank() } ?: "pythontex"
            val arguments = options.arguments?.trim().takeUnless { it.isNullOrBlank() } ?: context.mainFile.nameWithoutExtension
            "$executable $arguments"
        },
        workingDirectorySupplier = { context ->
            val options = stepConfig as? PythontexStepOptions
                ?: error("Expected PythontexStepOptions for $type, but got ${stepConfig::class.simpleName}")
            CommandLineRunStep.resolveWorkingDirectory(context, options.workingDirectoryPath)
        },
    )
}
