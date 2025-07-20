package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.NewLatexCommand


class LatexCommandBuilderScope {
    private var dependency: LatexPackage = LatexPackage.DEFAULT

    var mathMode: Boolean = false

    private val commands = mutableListOf<NewLatexCommand>()

    /**
     *
     * `package` is a reserved keyword in Kotlin, so we use `packageOf` instead.
     */
    fun packageOf(name: String) {
        dependency = LatexPackage(name)
    }

    operator fun String.invoke(
        vararg arguments: Argument,
        display: String? = null,
        description: String = "",
    ): NewLatexCommand {
        val commandText = this
        val commandWithSlash = "\\$commandText"
        val command = LatexCommandImpl(
            commandWithSlash,
            command = commandText,
            dependency = dependency,
            description = description,
            isMathMode = mathMode,
            arguments = arguments.asList(),
            display = display
        )
        commands.add(command)
        return command
    }


    companion object{

        fun buildCommands(action : LatexCommandBuilderScope.() -> Unit): List<LatexCommand> {
            val scope = LatexCommandBuilderScope()
            scope.action()
            return scope.commands
        }
    }

}
