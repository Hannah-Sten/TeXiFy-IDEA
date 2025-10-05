package nl.hannahsten.texifyidea.psi

import nl.hannahsten.texifyidea.util.parser.forEachDirectChild
import nl.hannahsten.texifyidea.util.parser.forEachDirectChildTyped
import nl.hannahsten.texifyidea.util.parser.toStringMap

/**
 * Defines a command possibly with parameters in LaTeX, such as `\alpha`, `\sqrt{x}` or `\frac{1}{2}`.
 *
 * This class allows the LatexCommandsImplMixin class to 'inject' methods into LatexCommands(Impl).
 *
 * For more LaTex structure related operations, please see the extension methods in LatexCommandsUtil.
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
     * Whether this command has a star, such as in `\section*{Title}`.
     */
    fun hasStar(): Boolean

    fun firstParameter(): LatexParameter? {
        forEachDirectChild {
            if (it is LatexParameter) return it
        }
        return null
    }

    fun firstRequiredParameter(): LatexRequiredParam? {
        forEachDirectChild { c ->
            if (c is LatexParameter) {
                c.requiredParam?.let { return it }
            }
        }
        return null
    }

    /**
     * Whether this command has any parameters, either required or optional.
     */
    fun hasParameter(): Boolean {
        return firstParameter() != null
    }

    fun hasRequiredParameter(): Boolean {
        return firstRequiredParameter() != null
    }

    /**
     * Looks up all the required parameters of this command.
     *
     * @return A list of all required parameters.
     */
    fun requiredParameters(): List<LatexRequiredParam> {
        return parameterList.mapNotNull { it.requiredParam }
    }

    /**
     * Generates a list of all names of all required parameters in the command.
     */
    fun requiredParametersText(): List<String> {
        return parameterList.mapNotNull {
            if (it.requiredParam != null) it.contentText() else null
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

    fun optionalParameterText(idx: Int): String? {
        var pos = 0
        forEachDirectChildTyped<LatexParameter> { param ->
            if (param.optionalParam != null) {
                if (pos == idx) return param.contentText()
                pos++
            }
        }
        return null
    }

    fun getOptionalParameterMap(): Map<LatexOptionalKeyValKey, LatexKeyValValue?>
}

val LatexCommandWithParams.nameWithoutSlash: String?
    get() = getName()?.removePrefix("\\")

val LatexCommandWithParams.nameWithSlash: String?
    get() = getName()