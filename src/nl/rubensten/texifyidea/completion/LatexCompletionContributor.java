package nl.rubensten.texifyidea.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import nl.rubensten.texifyidea.LatexLanguage;
import nl.rubensten.texifyidea.lang.LatexMode;
import nl.rubensten.texifyidea.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sten Wessel, Ruben Schellekens
 */
public class LatexCompletionContributor extends CompletionContributor {

    public LatexCompletionContributor() {
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(LatexTypes.COMMAND_TOKEN)
                        .inside(LatexMathEnvironment.class)
                        .withLanguage(LatexLanguage.INSTANCE),
                new LatexCommandProvider(LatexMode.MATH)
        );

        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(LatexTypes.COMMAND_TOKEN)
                        .andNot(PlatformPatterns.psiElement()
                                .inside(LatexMathEnvironment.class))
                        .withLanguage(LatexLanguage.INSTANCE),
                new LatexCommandProvider(LatexMode.NORMAL)
        );

        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(LatexTypes.NORMAL_TEXT)
                        .inside(LatexRequiredParam.class)
                        .inside(LatexBeginCommand.class)
                        .withLanguage(LatexLanguage.INSTANCE),
                new LatexCommandProvider(LatexMode.ENVIRONMENT_NAME)
        );

        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(LatexTypes.NORMAL_TEXT)
                        .inside(LatexRequiredParam.class)
                        .with(new PatternCondition<PsiElement>(null) {
                            @Override
                            public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext processingContext) {
                                LatexCommands command = LatexPsiUtil.getParentOfType(
                                        psiElement, LatexCommands.class
                                );
                                return command.getCommandToken().getText().equals("\\ref");
                            }
                        })
                        .withLanguage(LatexLanguage.INSTANCE),
                new LatexReferenceProvider()
        );
    }
}
