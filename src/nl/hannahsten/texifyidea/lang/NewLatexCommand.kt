package nl.hannahsten.texifyidea.lang


enum class ArgumentType {
    REQUIRED, // Required argument
    OPTIONAL, // Optional argument
    STAR, // Star argument (e.g., `\newcommand{\foo}[1]{#1}` where `#1` is a star argument)
    VARIADIC, // Variadic argument (e.g., `\newcommand{\foo}[1][]{#1}` where `[]` indicates optional arguments)
}

data class ContextChange(
    val context: LatexContext,
    val enabled: Boolean
)

class LArgument(
    val name : String,
    val type: ArgumentType = ArgumentType.REQUIRED,
    val contexts : List<ContextChange> = emptyList(),

    val description : String = "",
)


data class NewLatexCommand(
    /**
     * The name of the command without the leading backslash.
     */
    val name : String,
    val dependency : LatexPackage,

    val arguments : List<LArgument>,
    /**
     * The description of the command, used for documentation.
     */
    val description : String = "",

    val display : String? = null,


    val nameWithSlash : String = "\\$name",
) {

}