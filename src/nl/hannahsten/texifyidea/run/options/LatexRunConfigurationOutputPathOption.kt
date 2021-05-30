package nl.hannahsten.texifyidea.run.options

/**
 * See [LatexRunConfigurationAbstractOutputPathOption].
 */
data class LatexRunConfigurationOutputPathOption(override val resolvedPath: String? = null, override val pathWithMacro: String? = resolvedPath) : LatexRunConfigurationAbstractOutputPathOption(pathWithMacro, resolvedPath)
