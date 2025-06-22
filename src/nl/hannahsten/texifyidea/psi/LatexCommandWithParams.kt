package nl.hannahsten.texifyidea.psi

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
    fun getName() : String?

    val parameterList: List<LatexParameter>

    /**
     * Generates a list of all names of all required parameters in the command.
     */
    fun getRequiredParameters(): List<String>

    fun getOptionalParameterMap(): Map<LatexOptionalKeyValKey, LatexKeyValValue?>
}