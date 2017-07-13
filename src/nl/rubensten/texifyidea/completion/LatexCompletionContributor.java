package nl.rubensten.texifyidea.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import nl.rubensten.texifyidea.LatexLanguage;
import nl.rubensten.texifyidea.lang.LatexMode;
import nl.rubensten.texifyidea.lang.LatexNoMathCommand;
import nl.rubensten.texifyidea.lang.RequiredFileArgument;
import nl.rubensten.texifyidea.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

        // References.
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
                                return command != null &&
                                        command.getCommandToken().getText().equals("\\ref");
                            }
                        })
                        .withLanguage(LatexLanguage.INSTANCE),
                new LatexReferenceProvider()
        );

        // File names
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

                                if (command == null) {
                                    return false;
                                }

                                String name = command.getCommandToken().getText();
                                LatexNoMathCommand cmd = LatexNoMathCommand.get(name.substring(1)).orElse(null);
                                if (cmd == null) {
                                    return false;
                                }

                                List<RequiredFileArgument> args = cmd.getArgumentsOf(RequiredFileArgument.class);
                                return !args.isEmpty();
                            }
                        })
                        .withLanguage(LatexLanguage.INSTANCE),
                new LatexFileProvider()
        );
    }
}
