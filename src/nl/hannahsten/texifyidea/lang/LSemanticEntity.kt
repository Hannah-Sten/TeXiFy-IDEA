@file:Suppress("unused")

package nl.hannahsten.texifyidea.lang

import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.util.Key

import nl.hannahsten.texifyidea.lang.LatexContextIntro.Inherit

/**
 * A semantic entity in LaTeX, such as a command or environment.
 *
 * @author Ezrnest
 */
sealed class LSemanticEntity(
    /**
     * The name of the entity.
     *
     * This is the name without the leading backslash for commands.
     */
    val name: String,
    /**
     * The namespace of the entity, i.e., the package or class it belongs to, including the suffix `.sty` or `.cls`.
     */
    val dependency: LatexLib = LatexLib.CUSTOM,
    /**
     * This entity is applicable in any of these contexts, or anywhere if null.
     */
    val applicableContext: LContextSet? = null,
    var description: String = ""
) {

    val displayName: String
        get() = if (dependency.isCustom) "'$name'" else if (dependency.isCustom) "'$name'(base)" else "'$name'($dependency)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LSemanticEntity) return false

        if (this::class != other::class) return false

        if (name != other.name) return false
        if (dependency != other.dependency) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + dependency.hashCode()
        return result
    }

    fun isApplicableIn(context: LContextSet): Boolean = applicableContext == null || applicableContext.any { it in context }

    fun isApplicableIn(context: LatexContext): Boolean = applicableContext == null || context in applicableContext

    fun applicableContextDisplay(): String {
        return if (applicableContext == null) "*"
        else "<${applicableContext.joinToString("|") { it.display }}>" // they are union
    }

    // Meta info is stored via UserDataHolderBase; not part of equals/hashCode.
    private var metaHolder: UserDataHolderBase? = null

    fun <T : Any?> getMeta(key: Key<T>): T? = metaHolder?.getUserData(key)

    fun <T : Any?> putMeta(key: Key<T>, value: T?) {
        val holder = metaHolder ?: UserDataHolderBase().also { metaHolder = it }
        holder.putUserData(key, value)
    }
}

class LSemanticCommand(
    /**
     * The name of the command without the leading backslash.
     */
    name: String,
    namespace: LatexLib = LatexLib.CUSTOM,
    applicableCtx: LContextSet? = null,
    /**
     * The list of arguments in order of appearance, including optional arguments.
     */
    val arguments: List<LArgument> = emptyList(),
    /**
     * The description of the command, used for documentation.
     */
    description: String = "",

    val display: String? = null,
    val commandWithSlash: String = "\\$name",
) : LSemanticEntity(name, namespace, applicableCtx, description) {

    override fun toString(): String = "Cmd($displayName, ctx=${applicableContextDisplay()}, arg=${arguments.joinToString("")}, description='$description')"
}

class LSemanticEnv(
    name: String,
    namespace: LatexLib,
    requiredContext: LContextSet? = null,
    /**
     * The list of arguments in order of appearance, including optional arguments.
     */
    val arguments: List<LArgument> = emptyList(),
    /**
     * The context signature that this environment introduces.
     */
    val contextSignature: LatexContextIntro = Inherit,
    /**
     * The description of the environment, used for documentation.
     */
    description: String = "",
) : LSemanticEntity(name, namespace, requiredContext, description) {
    override fun toString(): String = "Env($displayName, ctx=${applicableContextDisplay()}, arg=${arguments.joinToString("")}, scope=$contextSignature, description='$description')"
}