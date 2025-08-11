package nl.hannahsten.texifyidea.lang

interface DSLLatexBuilderScope

abstract class AbstractDSLLatexBuilderScope : DSLLatexBuilderScope {
    /**
     * The package name of the current scope.
     * This is used to determine the namespace of commands and environments.
     */
    var namespace: String = ""

    /**
     * The context set that is required for the commands in this scope.
     */
    var requiredContext: LContextSet = emptySet()

    /**
     *
     * `package` is a reserved keyword in Kotlin, so we use `packageOf` instead.
     */
    fun packageOf(name: String) {
        namespace = toPackageName(name)
    }

    private fun appendSuffixIfNeeded(name: String, suffix: String): String {
        return if (name.endsWith(suffix)) name else "$name$suffix"
    }

    fun toPackageName(name: String): String {
        if (name.isEmpty()) return ""
        return appendSuffixIfNeeded(name, ".sty")
    }

    inline fun underPackage(name: String, action: () -> Unit) {
        val oldDependency = namespace
        namespace = toPackageName(name)
        action()
        namespace = oldDependency
    }

    fun setRequiredContext(vararg context: LatexContext) {
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
        ctx: LatexContextIntro, description: String = ""
    ): LArgument {
        return LArgument.required(this, ctx, description)
    }

    fun String.optional(
        ctx: LatexContext, description: String = ""
    ): LArgument {
        return LArgument.optional(this, ctx, description)
    }

    fun String.optional(
        ctx: LatexContextIntro, description: String = ""
    ): LArgument {
        return LArgument.optional(this, ctx, description)
    }

    operator fun LatexContext.unaryPlus(): LatexContextIntro {
        return LatexContextIntro.add(this)
    }

    operator fun LatexContext.unaryMinus(): LatexContextIntro {
        return LatexContextIntro.remove(this)
    }
}

class DSLLatexCommandBuilderScope : AbstractDSLLatexBuilderScope() {

    private val commands = mutableListOf<LSemanticCommand>()

    fun build(): List<LSemanticCommand> {
        return commands
    }

    fun String.cmd(
        vararg arguments: LArgument,
        desc: String = "",
    ): LSemanticCommand {
        val name = this
        val command = LSemanticCommand(
            name = name,
            namespace = namespace,
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
            namespace = namespace,
            arguments = emptyList(),
            description = description ?: display ?: name,
            display = display,
            requiredContext = requiredContext,
        )
        commands.add(command)
        return command
    }

    companion object {

        inline fun buildCommands(action: DSLLatexCommandBuilderScope.() -> Unit): List<LSemanticCommand> {
            val scope = DSLLatexCommandBuilderScope()
            scope.action()
            return scope.build()
        }
    }
}

class DSLLatexEnvironmentBuilderScope : AbstractDSLLatexBuilderScope() {

    private val environments = mutableListOf<LSemanticEnv>()

    fun build(): List<LSemanticEnv> {
        return environments
    }

    fun environment(
        name: String, context: LatexContextIntro,
        arguments: List<LArgument>, description: String
    ): LSemanticEnv {
        val environment = LSemanticEnv(
            name = name,
            namespace = namespace,
            contextSignature = context,
            arguments = arguments,
            description = description,
            requiredContext = requiredContext,
        )
        environments.add(environment)
        return environment
    }

    inline fun String.env(
        context: LatexContextIntro,
        vararg arguments: LArgument,
        desc: () -> String = { "" }
    ): LSemanticEnv {
        return environment(this, context, arguments.toList(), description = desc())
    }

    inline fun String.env(
        context: LatexContext,
        vararg arguments: LArgument,
        desc: () -> String = { "" }
    ): LSemanticEnv {
        return env(LAssignContext(context), *arguments, desc = desc)
    }

    inline fun String.env(
        context: LContextSet,
        vararg arguments: LArgument,
        desc: () -> String = { "" }
    ): LSemanticEnv {
        return env(LAssignContext(context), *arguments, desc = desc)
    }

    operator fun String.unaryPlus(): LSemanticEnv {
        return this.env(LatexContextIntro.inherit())
    }

    companion object {

        inline fun buildEnvironments(action: DSLLatexEnvironmentBuilderScope.() -> Unit): List<LSemanticEnv> {
            val scope = DSLLatexEnvironmentBuilderScope()
            scope.action()
            return scope.build()
        }
    }
}

abstract class PredefinedCommandSet {
    private val myAllCommands = mutableSetOf<LSemanticCommand>()

    val allCommands: Set<LSemanticCommand>
        get() = myAllCommands

    protected fun buildCommands(action: DSLLatexCommandBuilderScope.() -> Unit): List<LSemanticCommand> {
        val built = DSLLatexCommandBuilderScope.buildCommands(action)
        myAllCommands.addAll(built)
        return built
    }

    protected fun mathCommands(action: DSLLatexCommandBuilderScope.() -> Unit): List<LSemanticCommand> {
        return buildCommands {
            setRequiredContext(LatexContexts.Math)
            action()
        }
    }

    protected fun textCommands(action: DSLLatexCommandBuilderScope.() -> Unit): List<LSemanticCommand> {
        return buildCommands {
            setRequiredContext(LatexContexts.Text)
            action()
        }
    }

    protected fun preambleCommands(action: DSLLatexCommandBuilderScope.() -> Unit): List<LSemanticCommand> {
        return buildCommands {
            setRequiredContext(LatexContexts.Preamble)
            action()
        }
    }

//    protected fun Collection<LSemanticCommand>.toSingleLookupMap(): Map<String, LSemanticCommand> {
//        return this.associateBy { it.name }
//    }
//
//    protected fun Collection<LSemanticCommand>.toLookupMap(): Map<String, List<LSemanticCommand>> {
//        return this.groupBy { it.name }
//    }
}

abstract class PredefinedEnvironmentSet {
    private val myAllEnvironments = mutableSetOf<LSemanticEnv>()

    val allEnvironments: Set<LSemanticEnv>
        get() = myAllEnvironments

    protected fun buildEnvironments(action: DSLLatexEnvironmentBuilderScope.() -> Unit): List<LSemanticEnv> {
        val built = DSLLatexEnvironmentBuilderScope.buildEnvironments(action)
        myAllEnvironments.addAll(built)
        return built
    }

    protected fun mathEnvironments(action: DSLLatexEnvironmentBuilderScope.() -> Unit): List<LSemanticEnv> {
        return buildEnvironments {
            setRequiredContext(LatexContexts.Math)
            action()
        }
    }

//    protected fun Collection<LSemanticEnv>.toSingleLookupMap(): Map<String, LSemanticEnv> {
//        return this.associateBy { it.name }
//    }
//
//    protected fun Collection<LSemanticEnv>.toLookupMap(): Map<String, List<LSemanticEnv>> {
//        return this.groupBy { it.name }
//    }
}

fun main() {
    DSLLatexCommandBuilderScope.buildCommands {
        packageOf("my.package")

        symbol("alpha", "α", "Greek letter alpha")
    }
}
