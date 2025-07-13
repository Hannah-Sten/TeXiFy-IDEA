package nl.hannahsten.texifyidea.psi

import nl.hannahsten.texifyidea.util.parser.toStringMap

/**
 * Defines a command possibly with parameters in LaTeX, such as `\alpha`, `\sqrt{x}` or `\frac{1}{2}`.
 *
 * This class allows the LatexCommandsImplMixin class to 'inject' methods into LatexCommands(Impl).
 * In general, it is more straightforward to provide extension methods in LatexCommandsUtil.
 */
interface LatexCommandWithParams : LatexComposite {

    /**
     * Get the name of the command, for example `\newcommand`, from the stub if available, otherwise default to getting the text from psi.
     *
     * **Note that the backslash `\` is included in the name.**
     */
    fun getName(): String?

    val parameterList: List<LatexParameter>

    /**
     * Generates a list of all names of all required parameters in the command.
     */
    fun requiredParametersText(): List<String> {
        return parameterList.mapNotNull {
            val param = it.requiredParam ?: return@mapNotNull null
            val text = param.text
            text.trim { c ->
                c == '{' || c == '}' || c.isWhitespace()
            }
        }
    }

    fun optionalParameterTextMap(): Map<String, String> {
        return getOptionalParameterMap().toStringMap()
    }

    /**
     * Gets the required parameters of this command at the specified index, or null if the index is out of bounds.
     */
    fun requiredParameterText(idx: Int): String? {
        return requiredParametersText().getOrNull(idx)
    }

    fun getOptionalParameterMap(): Map<LatexOptionalKeyValKey, LatexKeyValValue?>
}