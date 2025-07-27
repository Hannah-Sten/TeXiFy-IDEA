package nl.hannahsten.texifyidea.lang

interface LatexBuilderDSLScope

class LatexCommandBuilderScope : LatexBuilderDSLScope {
    var pkg: String = ""

    private val commands = mutableListOf<NewLatexCommand>()

    var requiredContext: LContextSet = emptySet()

    /**
     *
     * `package` is a reserved keyword in Kotlin, so we use `packageOf` instead.
     */
    fun packageOf(name: String) {
        pkg = name
    }

    inline fun inPackage(name : String, action: () -> Unit) {
        val oldDependency = pkg
        pkg = oldDependency
        action()
        pkg = oldDependency
    }


    fun setCommandContext(vararg context: LatexContext) {
        requiredContext = context.toSet()
    }

    inline fun underCmdContext(
        vararg context: LatexContext,
        action: () -> Unit
    ) {
        val oldContext = requiredContext
        requiredContext = context.toSet()
        action()
        requiredContext = oldContext
    }

    operator fun String.invoke(
        vararg arguments: LArgument,
        description: String = "",
    ): NewLatexCommand {
        return cmd(*arguments) { description }
    }

    fun String.cmd(
        vararg arguments: LArgument,
        desc: String = "",
    ): NewLatexCommand {
        val name = this
        val command = NewLatexCommand(
            name = name,
            namespace = pkg,
            arguments = arguments.toList(),
            description = desc,
            display = null,
            requiredContext = requiredContext,
        )
        commands.add(command)
        return command
    }


    inline fun String.cmd(
        vararg arguments: LArgument,
        desc: () -> String,
    ): NewLatexCommand {
        return cmd(*arguments, desc = desc())
    }

    operator fun String.unaryPlus(): NewLatexCommand {
        return this.cmd()
    }

    fun symbol(name: String, display: String? = null, description: String? = null): NewLatexCommand {
        val command = NewLatexCommand(
            name = name,
            namespace = pkg,
            arguments = emptyList(),
            description = description ?: display ?: name,
            display = display,
            requiredContext = requiredContext,
        )
        commands.add(command)
        return command
    }

    fun required(
        name: String, ctx: LContextIntro = LContextInherit, description: String = ""
    ): LArgument {
        return LArgument(name, type = LArgumentType.REQUIRED, ctx, description)
    }

    fun required(
        name: String, ctx: LatexContext, description: String = ""
    ): LArgument {
        return LArgument(name, type = LArgumentType.REQUIRED, LAssignContext(ctx), description)
    }

    fun optional(
        name: String, ctx: LContextIntro = LContextInherit, description: String = ""
    ): LArgument {
        return LArgument(name, type = LArgumentType.OPTIONAL, ctx, description)
    }

    fun optional(
        name: String, ctx: LatexContext, description: String = ""
    ): LArgument {
        return LArgument(name, type = LArgumentType.OPTIONAL, LAssignContext(ctx), description)
    }

    val String.required: LArgument
        get() = required(this)

    val String.optional: LArgument
        get() = optional(this)


    companion object {

        fun buildCommands(action: LatexCommandBuilderScope.() -> Unit): List<NewLatexCommand> {
            val scope = LatexCommandBuilderScope()
            scope.action()
            return scope.commands
        }

        fun mathCommands(action: LatexCommandBuilderScope.() -> Unit): List<NewLatexCommand> {
            val scope = LatexCommandBuilderScope()
            scope.setCommandContext(LMathContext)
            scope.action()
            return scope.commands
        }
    }
}

abstract class LatexCommandSet {
    private val myAllCommands = mutableListOf<NewLatexCommand>()

    protected fun buildCommands(action: LatexCommandBuilderScope.() -> Unit): List<NewLatexCommand> {
        val built = LatexCommandBuilderScope.buildCommands(action)
        myAllCommands.addAll(built)
        return built
    }

    protected fun mathCommands(action: LatexCommandBuilderScope.() -> Unit): List<NewLatexCommand> {
        return buildCommands {
            setCommandContext(LMathContext)
            action()
        }
    }

    protected fun textCommands(action: LatexCommandBuilderScope.() -> Unit): List<NewLatexCommand> {
        return buildCommands {
            setCommandContext(LTextContext)
            action()
        }
    }
}

fun main() {
    LatexCommandBuilderScope.buildCommands {
        packageOf("my.package")
        setCommandContext(LMathContext)

        symbol("alpha", "α", "Greek letter alpha")
    }
}
