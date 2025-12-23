package nl.hannahsten.texifyidea.index.stub

import com.intellij.psi.stubs.NamedStub
import com.intellij.psi.stubs.StubElement
import nl.hannahsten.texifyidea.psi.LatexCommands

@JvmRecord
data class LatexParameterStub(
    val type: Int,
    val content: String,
) {
    companion object {
        const val REQUIRED = 0
        const val OPTIONAL = 1
    }
}

/**
 * @author Hannah Schellekens
 */
interface LatexCommandsStub : StubElement<LatexCommands>, NamedStub<LatexCommands> {

    val commandToken: String

    /**
     * The name of the command without the leading backslash.
     */
    val commandName: String
        get() = commandToken.removePrefix("\\")

    /**
     * All the parameters of the command in order
     */
    val parameters: List<LatexParameterStub>

    val optionalParamsMap: Map<String, String>
}

val LatexCommandsStub.requiredParams: List<String>
    get() = parameters.mapNotNull { if (it.type == LatexParameterStub.REQUIRED) it.content else null }

val LatexCommandsStub.optionalParams: List<String>
    get() = parameters.mapNotNull { if (it.type == LatexParameterStub.OPTIONAL) it.content else null }

fun LatexCommandsStub.parameterOfTypeAt(index: Int, type: Int): String? {
    var pos = 0
    for (param in parameters) {
        if (param.type == type) {
            if (pos == index) return param.content
            pos++
        }
    }
    return null
}

fun LatexCommandsStub.requiredParamAt(index: Int): String? = parameterOfTypeAt(index, LatexParameterStub.REQUIRED)

fun LatexCommandsStub.optionalParamAt(index: Int): String? = parameterOfTypeAt(index, LatexParameterStub.OPTIONAL)
