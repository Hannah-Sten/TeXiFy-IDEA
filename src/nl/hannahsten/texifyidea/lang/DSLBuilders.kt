package nl.hannahsten.texifyidea.lang

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

open class DSLLatexBuilderScope {
    /**
     * The package name of the current scope.
     * This is used to determine the namespace of commands and environments.
     */
    var namespace: LatexLib = LatexLib.BASE

    /**
     * The context set that is the commands are applicable in,
     * or null if the command is applicable in all contexts.
     */
    var applicableContexts: LContextSet? = null

    /**
     *
     * `package` is a reserved keyword in Kotlin, so we use `packageOf` instead.
     */
    fun packageOf(name: String) {
        namespace = LatexLib.Package(name)
    }

    fun packageOf(lib: LatexLib) {
        namespace = lib
    }

    fun applicableIn(vararg context: LatexContext) {
        applicableContexts = context.toSet()
    }

    @OptIn(ExperimentalContracts::class)
    inline fun underPackage(name: String, action: () -> Unit) {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        val oldDependency = namespace
        packageOf(name)
        action()
        namespace = oldDependency
    }

    @OptIn(ExperimentalContracts::class)
    inline fun underPackage(lib: LatexLib, action: () -> Unit) {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        val oldDependency = namespace
        packageOf(lib)
        action()
        namespace = oldDependency
    }

    @OptIn(ExperimentalContracts::class)
    inline fun underBase(action: () -> Unit) {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        val oldDependency = namespace
        packageOf(LatexLib.BASE)
        action()
        namespace = oldDependency
    }

    @Suppress("unused")
    @OptIn(ExperimentalContracts::class)
    inline fun underAnyContext(action: () -> Unit) {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        val oldContext = applicableContexts
        applicableContexts = null
        action()
        applicableContexts = oldContext
    }

    @OptIn(ExperimentalContracts::class)
    inline fun underContext(context: LatexContext, action: () -> Unit) {
        contract {
            callsInPlace(action, InvocationKind.EXACTLY_ONCE)
        }
        val oldContext = applicableContexts
        applicableContexts = setOf(context)
        action()
        applicableContexts = oldContext
    }

    /**
     * Under any of the given contexts, the commands/environments will be applicable.
     */
    @OptIn(ExperimentalContracts::class)
    inline fun underContexts(vararg context: LatexContext, action: () -> Unit) {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        val oldContext = applicableContexts
        applicableContexts = context.toSet()
        action()
        applicableContexts = oldContext
    }

    val String.required: LArgument
        get() = LArgument.required(this)

    val String.optional: LArgument
        get() = LArgument.optional(this)

    fun String.required(
        ctx: LatexContext, description: String = ""
    ): LArgument = LArgument.required(this, ctx, description)

    fun String.required(
        ctx: LContextSet, description: String = ""
    ): LArgument = LArgument.required(this, LatexContextIntro.Assign(ctx), description)

    fun String.required(
        ctx: LatexContextIntro, description: String = ""
    ): LArgument = LArgument.required(this, ctx, description)

    fun String.optional(
        ctx: LatexContext, description: String = ""
    ): LArgument = LArgument.optional(this, ctx, description)

    fun String.optional(
        ctx: LatexContextIntro, description: String = ""
    ): LArgument = LArgument.optional(this, ctx, description)

    operator fun LatexContext.unaryPlus(): LatexContextIntro = LatexContextIntro.add(this)

    operator fun LatexContext.unaryMinus(): LatexContextIntro = LatexContextIntro.remove(this)

    fun command(
        name: String, arguments: List<LArgument>, description: String, display: String?
    ): LSemanticCommand {
        val command = LSemanticCommand(name, namespace, applicableContexts, arguments, description, display)
        add(command)
        return command
    }

//    fun String.cmd(
//        vararg arguments: LArgument, desc: String = "",
//    ): LSemanticCommand {
//        return command(
//            name = this, arguments = arguments.toList(), description = desc, display = null
//        )
//    }

    @OptIn(ExperimentalContracts::class)
    inline fun String.cmd(
        vararg arguments: LArgument, display: String? = null, desc: () -> String = { "" }
    ): LSemanticCommand {
        contract { callsInPlace(desc, InvocationKind.EXACTLY_ONCE) }
        return command(this, arguments.toList(), description = desc(), display = display)
    }

    fun symbol(name: String, display: String? = null, description: String? = null): LSemanticCommand = command(name, emptyList(), description ?: display ?: name, display)

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
            requiredContext = applicableContexts,
        )
        add(environment)
        return environment
    }

    @OptIn(ExperimentalContracts::class)
    inline fun String.env(
        context: LatexContextIntro,
        vararg arguments: LArgument,
        desc: () -> String = { "" }
    ): LSemanticEnv {
        contract { callsInPlace(desc, InvocationKind.EXACTLY_ONCE) }
        return environment(this, context, arguments.toList(), description = desc())
    }

    @OptIn(ExperimentalContracts::class)
    inline fun String.env(
        context: LatexContext,
        vararg arguments: LArgument,
        desc: () -> String = { "" }
    ): LSemanticEnv {
        contract { callsInPlace(desc, InvocationKind.EXACTLY_ONCE) }
        return env(LatexContextIntro.Assign(context), *arguments, desc = desc)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun String.env(
        context: LContextSet,
        vararg arguments: LArgument,
        desc: () -> String = { "" }
    ): LSemanticEnv {
        contract { callsInPlace(desc, InvocationKind.EXACTLY_ONCE) }
        return env(LatexContextIntro.Assign(context), *arguments, desc = desc)
    }

    private val myEntities = mutableListOf<LSemanticEntity>()

    fun add(entity: LSemanticEntity) {
        myEntities.add(entity)
    }

    fun build(): List<LSemanticEntity> = myEntities.toList()
}

class DSLLatexCommandBuilderScope : DSLLatexBuilderScope() {
    fun buildCommands(): List<LSemanticCommand> = build().filterIsInstance<LSemanticCommand>()

    operator fun String.unaryPlus(): LSemanticCommand = this.cmd()
}

class DSLLatexEnvironmentBuilderScope : DSLLatexBuilderScope() {

    fun buildEnvironments(): List<LSemanticEnv> = build().filterIsInstance<LSemanticEnv>()

    operator fun String.unaryPlus(): LSemanticEnv = this.env(LatexContextIntro.inherit())
}

abstract class PredefinedEntitySet {
    private val myAllEntities = mutableSetOf<LSemanticEntity>()

    val allEntities: Set<LSemanticEntity>
        get() = myAllEntities

    fun addAll(entities: Collection<LSemanticEntity>) {
        myAllEntities.addAll(entities)
    }

    protected inline fun definitions(action: DSLLatexBuilderScope.() -> Unit): List<LSemanticEntity> {
        val scope = DSLLatexBuilderScope()
        scope.action()
        return scope.build().also {
            addAll(it)
        }
    }

    protected inline fun definedUnder(
        packageName: String, action: DSLLatexBuilderScope.() -> Unit
    ): List<LSemanticEntity> = definitions {
        packageOf(packageName)
        action()
    }
}

abstract class PredefinedCommandSet : PredefinedEntitySet() {

    val allCommands: List<LSemanticCommand>
        get() = allEntities.filterIsInstance<LSemanticCommand>()

    @OptIn(ExperimentalContracts::class)
    protected inline fun buildCommands(action: DSLLatexCommandBuilderScope.() -> Unit): List<LSemanticCommand> {
        contract {
            callsInPlace(action, InvocationKind.EXACTLY_ONCE)
        }

        val scope = DSLLatexCommandBuilderScope()
        scope.action()
        val built = scope.buildCommands()
        addAll(built)
        return built
    }

    protected inline fun mathCommands(action: DSLLatexCommandBuilderScope.() -> Unit): List<LSemanticCommand> = buildCommands {
        applicableIn(LatexContexts.Math)
        action()
    }

    protected inline fun textCommands(action: DSLLatexCommandBuilderScope.() -> Unit): List<LSemanticCommand> = buildCommands {
        applicableIn(LatexContexts.Text)
        action()
    }

    protected inline fun preambleCommands(action: DSLLatexCommandBuilderScope.() -> Unit): List<LSemanticCommand> = buildCommands {
        applicableIn(LatexContexts.Preamble)
        action()
    }
}

abstract class PredefinedEnvironmentSet : PredefinedEntitySet() {

    val allEnvironments: List<LSemanticEnv>
        get() = allEntities.filterIsInstance<LSemanticEnv>()

    protected fun buildEnvironments(action: DSLLatexEnvironmentBuilderScope.() -> Unit): List<LSemanticEnv> {
        val scope = DSLLatexEnvironmentBuilderScope()
        scope.action()
        val built = scope.buildEnvironments()
        addAll(built)
        return built
    }

    protected fun mathEnvironments(action: DSLLatexEnvironmentBuilderScope.() -> Unit): List<LSemanticEnv> = buildEnvironments {
        applicableIn(LatexContexts.Math)
        action()
    }
}