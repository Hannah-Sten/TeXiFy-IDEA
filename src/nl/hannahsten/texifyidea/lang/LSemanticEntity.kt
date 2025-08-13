package nl.hannahsten.texifyidea.lang

import nl.hannahsten.texifyidea.lang.LatexContextIntro.Inherit

/**
 * A semantic entity in LaTeX, such as a command or environment.
 *
 * @author Ezrnest
 */
abstract class LSemanticEntity(
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
    val description: String = ""
) {
    val displayName: String
        get() = if (dependency.isDefault) name else "$name($dependency)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LSemanticEntity) return false

        if (name != other.name) return false
        if (dependency != other.dependency) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + dependency.hashCode()
        return result
    }

    fun isApplicableIn(context: LContextSet): Boolean {
        return applicableContext == null || applicableContext.any { it in context }
    }

    fun applicableContextDisplay(): String {
        return if (applicableContext == null) "*"
        else "<${applicableContext.joinToString("|") { it.display }}>" // they are union
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
    val nameWithSlash: String = "\\$name",
) : LSemanticEntity(name, namespace, applicableCtx, description) {

    override fun toString(): String {
        return "Cmd('$displayName', ctx=${applicableContextDisplay()}, arg=${arguments.joinToString("")}, description='$description')"
    }
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
    override fun toString(): String {
        return "Env($displayName, ctx=${applicableContextDisplay()}, arg=${arguments.joinToString("")}, scope=$contextSignature, description='$description')"
    }
}