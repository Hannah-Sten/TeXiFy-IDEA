package nl.hannahsten.texifyidea.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub;
import nl.hannahsten.texifyidea.psi.LatexCommands;
import nl.hannahsten.texifyidea.psi.LatexParameter;
import nl.hannahsten.texifyidea.psi.LatexRequiredParam;
import nl.hannahsten.texifyidea.psi.LatexVisitor;
import nl.hannahsten.texifyidea.reference.LatexLabelReference;
import nl.hannahsten.texifyidea.util.Magic;
import nl.hannahsten.texifyidea.util.PsiCommandsKt;
import nl.hannahsten.texifyidea.util.files.ReferencedFileSetService;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

import static nl.hannahsten.texifyidea.psi.LatexTypes.COMMAND_TOKEN;

public class LatexCommandsImpl extends StubBasedPsiElementBase<LatexCommandsStub>
        implements LatexCommands {

    private static final Set<String> REFERENCE_COMMANDS = Magic.Command.reference;

    private String name;

    public LatexCommandsImpl(ASTNode node) {
        super(node);
    }

    public LatexCommandsImpl(LatexCommandsStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public void accept(@NotNull LatexVisitor visitor) {
        visitor.visitCommands(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LatexVisitor) {
            accept((LatexVisitor)visitor);
        }
        else {
            super.accept(visitor);
        }
    }

    @Override
    public PsiReference getReference() {
        LatexRequiredParam firstParam = ApplicationManager.getApplication().runReadAction((Computable<LatexRequiredParam>)() -> {
            List<LatexRequiredParam> params = PsiCommandsKt.requiredParameters(this);
            return params.isEmpty() ? null : params.get(0);
        });

        if (REFERENCE_COMMANDS.contains(getCommandToken().getText()) && firstParam != null) {
            return new LatexLabelReference(this, firstParam);
        }

        return null;
    }

    @Override
    @NotNull
    public List<LatexParameter> getParameterList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, LatexParameter.class);
    }

    @Override
    @NotNull
    public PsiElement getCommandToken() {
        return findNotNullChildByType(COMMAND_TOKEN);
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        this.name = name;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "LatexCommandsImpl(COMMANDS)[STUB]{" + getName() + "}";
    }

    @Override
    public void subtreeChanged() {
        ReferencedFileSetService setService = ReferencedFileSetService.getInstance(getProject());
        setService.dropCaches(getContainingFile());
        super.subtreeChanged();
    }
}