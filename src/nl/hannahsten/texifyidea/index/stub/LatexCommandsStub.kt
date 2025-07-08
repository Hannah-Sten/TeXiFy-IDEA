package nl.hannahsten.texifyidea.index.stub

import com.intellij.psi.stubs.NamedStub
import com.intellij.psi.stubs.StubElement
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * @author Hannah Schellekens
 */
interface LatexCommandsStub : StubElement<LatexCommands>, NamedStub<LatexCommands> {

    val commandToken: String
    val requiredParams: List<String>
    val optionalParams: Map<String, String>
}