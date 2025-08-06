package nl.hannahsten.texifyidea.lang

interface LatexBuilderDSLScope

class LatexCommandBuilderScope : LatexBuilderDSLScope {
    var pkg: String = ""

    private val commands = mutableListOf<LSemanticCommand>()

    fun build(): List<LSemanticCommand> {
        return commands
    }

    var requiredContext: LContextSet = emptySet()

    /**
     *
     * `package` is a reserved keyword in Kotlin, so we use `packageOf` instead.
     */
    fun packageOf(name: String) {
        pkg = toPackageName(name)
    }

    private fun appendSuffixIfNeeded(name: String, suffix: String) : String{
        return if (name.endsWith(suffix)) name else "$name$suffix"
    }

    fun toPackageName(name: String): String {
        if(name.isEmpty()) return ""
        return appendSuffixIfNeeded(name, ".sty")
    }

    inline fun underPackage(name: String, action: () -> Unit) {
        val oldDependency = pkg
        pkg = toPackageName(name)
        action()
        pkg = oldDependency
    }

    fun setCommandContext(vararg context: LatexContext) {
        requiredContext = context.toSet()
    }

    inline fun underContext(
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
    ): LSemanticCommand {
        return cmd(*arguments) { description }
    }

    fun String.cmd(
        vararg arguments: LArgument,
        desc: String = "",
    ): LSemanticCommand {
        val name = this
        val command = LSemanticCommand(
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
    ): LSemanticCommand {
        return cmd(*arguments, desc = desc())
    }

    operator fun String.unaryPlus(): LSemanticCommand {
        return this.cmd()
    }

    fun symbol(name: String, display: String? = null, description: String? = null): LSemanticCommand {
        val command = LSemanticCommand(
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

    val String.required: LArgument
        get() = LArgument.required(this)

    val String.optional: LArgument
        get() = LArgument.optional(this)

    fun String.required(
        ctx: LatexContext, description: String = ""
    ): LArgument {
        return LArgument.required(this, ctx, description)
    }

    fun String.required(
        ctx: LContextIntro, description: String = ""
    ): LArgument {
        return LArgument.required(this, ctx, description)
    }

    fun String.optional(
        ctx: LatexContext, description: String = ""
    ): LArgument {
        return LArgument.optional(this, ctx, description)
    }

    fun String.optional(
        ctx: LContextIntro, description: String = ""
    ): LArgument {
        return LArgument.optional(this, ctx, description)
    }

    operator fun LatexContext.unaryPlus(): LContextIntro {
        return LContextIntro.add(this)
    }

    operator fun LatexContext.unaryMinus(): LContextIntro {
        return LContextIntro.remove(this)
    }

    companion object {

        inline fun buildCommands(action: LatexCommandBuilderScope.() -> Unit): List<LSemanticCommand> {
            val scope = LatexCommandBuilderScope()
            scope.action()
            return scope.build()
        }
    }
}

abstract class PredefinedCommandSet {
    private val myAllCommands = mutableSetOf<LSemanticCommand>()

    val allCommands : Set<LSemanticCommand>
        get() = myAllCommands



    protected fun buildCommands(action: LatexCommandBuilderScope.() -> Unit): List<LSemanticCommand> {
        val built = LatexCommandBuilderScope.buildCommands(action)
        myAllCommands.addAll(built)
        return built
    }

    protected fun mathCommands(action: LatexCommandBuilderScope.() -> Unit): List<LSemanticCommand> {
        return buildCommands {
            setCommandContext(LatexContexts.Math)
            action()
        }
    }

    protected fun textCommands(action: LatexCommandBuilderScope.() -> Unit): List<LSemanticCommand> {
        return buildCommands {
            setCommandContext(LatexContexts.Text)
            action()
        }
    }

    protected fun Collection<LSemanticCommand>.toSingleLookupMap(): Map<String, LSemanticCommand> {
        return this.associateBy { it.name }
    }

    protected fun Collection<LSemanticCommand>.toLookupMap(): Map<String, List<LSemanticCommand>> {
        return this.groupBy { it.name }
    }
}

fun main() {
    LatexCommandBuilderScope.buildCommands {
        packageOf("my.package")

        symbol("alpha", "α", "Greek letter alpha")
    }
}
