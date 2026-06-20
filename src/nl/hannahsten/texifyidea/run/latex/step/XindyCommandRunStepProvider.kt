package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.util.appendExtension
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.LatexStepType
import nl.hannahsten.texifyidea.run.latex.XindyStepOptions

internal object XindyCommandRunStepProvider : LatexRunStepProvider {

    override val type: String = LatexStepType.XINDY

    override val aliases: Set<String> = setOf(
        type,
        "xindy",
        "texindy",
    )

    override fun create(stepConfig: LatexStepRunConfigurationOptions): LatexRunStep = CommandLineRunStep(
        configId = stepConfig.id,
        id = type,
        commandLineSupplier = { context ->
            val options = stepConfig as? XindyStepOptions
                ?: error("Expected XindyStepOptions for $type, but got ${stepConfig::class.simpleName}")
            val executable = options.executable?.trim().takeUnless { it.isNullOrBlank() } ?: "xindy"
            val arguments = options.arguments?.trim().takeUnless { it.isNullOrBlank() }
                ?: context.session.mainFile.nameWithoutExtension.appendExtension("idx")
            "$executable $arguments"
        },
        workingDirectorySupplier = { context ->
            val options = stepConfig as? XindyStepOptions
                ?: error("Expected XindyStepOptions for $type, but got ${stepConfig::class.simpleName}")
            CommandLineRunStep.resolveWorkingDirectory(context, options.workingDirectoryPath)
        },
    )
}
