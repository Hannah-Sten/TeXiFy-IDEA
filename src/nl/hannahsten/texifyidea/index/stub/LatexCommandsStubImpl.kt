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
    override val commandToken: String,
    override val requiredParams: List<String>,
    override val optionalParams: Map<String, String>
) : NamedStubBase<LatexCommands>(parent, elementType, commandToken), LatexCommandsStub {

    override fun getName() = commandToken

    override fun toString() = "LatexCommandsStubImpl{commandToken='$commandToken', requiredParams=$requiredParams, optionalParams=$optionalParams}"
}
