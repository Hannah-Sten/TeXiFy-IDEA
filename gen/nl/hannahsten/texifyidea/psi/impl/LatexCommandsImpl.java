package nl.hannahsten.texifyidea.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        final LatexRequiredParam firstParam = readFirstParam();

        if (REFERENCE_COMMANDS.contains(getCommandToken().getText()) && firstParam != null) {
            return extractReferences(firstParam).toArray(new PsiReference[0]);
        }

        return new PsiReference[0];
    }

    @NotNull
    private List<PsiReference> extractReferences(LatexRequiredParam firstParam) {
        List<TextRange> subParamRanges = extractSubParameterRanges(firstParam);

        List<PsiReference> references = new ArrayList<>();
        for (TextRange range : subParamRanges) {
            references.add(new LatexLabelReference(
                    this, range.shiftRight(firstParam.getTextOffset() - getTextOffset())
            ));
        }
        return references;
    }

    private LatexRequiredParam readFirstParam() {
        return ApplicationManager.getApplication().runReadAction((Computable<LatexRequiredParam>)() -> {
                List<LatexRequiredParam> params = PsiCommandsKt.requiredParameters(this);
                return params.isEmpty() ? null : params.get(0);
        });
    }

    @NotNull
    private static List<TextRange> extractSubParameterRanges(LatexRequiredParam param) {
        return splitToRanges(stripGroup(param.getText()), Magic.Pattern.parameterSplit).stream()
                .map(r -> r.shiftRight(1)).collect(Collectors.toList());
    }

    @NotNull
    private static List<TextRange> splitToRanges(String text, Pattern pattern) {
        String[] parts = pattern.split(text);

        List<TextRange> ranges = new ArrayList<>();

        int currentOffset = 0;
        for (String part : parts) {
            final int partStartOffset = text.indexOf(part, currentOffset);
            ranges.add(TextRange.from(partStartOffset, part.length()));
            currentOffset = partStartOffset + part.length();
        }

        return ranges;
    }

    private static String stripGroup(String text) {
        return text.substring(1, text.length() - 1);
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