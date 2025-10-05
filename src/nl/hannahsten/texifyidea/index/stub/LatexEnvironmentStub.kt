package nl.hannahsten.texifyidea.index.stub

import com.intellij.psi.stubs.StubElement
import nl.hannahsten.texifyidea.psi.LatexEnvironment

interface LatexEnvironmentStub : StubElement<LatexEnvironment> {

    val environmentName: String
    val label: String?
}