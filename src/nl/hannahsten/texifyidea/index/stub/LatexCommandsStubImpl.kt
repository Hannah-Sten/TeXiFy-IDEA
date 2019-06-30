package nl.hannahsten.texifyidea.index.stub

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.NamedStubBase
import com.intellij.psi.stubs.StubElement
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * @author Hannah Schellekens
 */
class LatexCommandsStubImpl(
        parent: StubElement<*>,
        elementType: IStubElementType<*, *>,
        private val commandToken: String,
        private val requiredParams: List<String>,
        private val optionalParams: List<String>
) : NamedStubBase<LatexCommands>(parent, elementType, commandToken), LatexCommandsStub {

    override fun getName() = getCommandToken()

    override fun getCommandToken() = commandToken

    override fun getRequiredParams() = requiredParams

    override fun getOptionalParams() = optionalParams

    override fun toString() = "LatexCommandsStubImpl{commandToken='$commandToken', requiredParams=$requiredParams, optionalParams=$optionalParams}"

}
