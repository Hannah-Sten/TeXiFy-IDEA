package nl.rubensten.texifyidea.index.stub;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import nl.rubensten.texifyidea.psi.LatexCommands;

/**
 * @author Ruben Schellekens
 */
public class LatexCommandsStubImpl extends StubBase<LatexCommands> implements LatexCommandsStub {

    protected LatexCommandsStubImpl(StubElement parent, IStubElementType elementType) {
        super(parent, elementType);
    }
}
