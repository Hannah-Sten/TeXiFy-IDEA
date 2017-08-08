package nl.rubensten.texifyidea.inspections;

import com.google.common.collect.Sets;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import kotlin.reflect.jvm.internal.impl.utils.SmartList;
import nl.rubensten.texifyidea.lang.Diacritic;
import nl.rubensten.texifyidea.lang.LatexCommand;
import nl.rubensten.texifyidea.lang.LatexMathCommand;
import nl.rubensten.texifyidea.lang.LatexNoMathCommand;
import nl.rubensten.texifyidea.lang.Package;
import nl.rubensten.texifyidea.psi.LatexMathEnvironment;
import nl.rubensten.texifyidea.psi.LatexNormalText;
import nl.rubensten.texifyidea.util.PackageUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.Normalizer;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Sten Wessel
 */
public class UnicodeInspection extends TexifyInspectionBase {

    private static final Pattern NONASCII_PATTERN = Pattern.compile("\\P{ASCII}");
    private static final Pattern BASE_PATTERN = Pattern.compile("^\\p{ASCII}*");
    private static final Set<Package> UNICODE_PACKAGES = Sets.newHashSet(Package.INPUTENC.with("utf8"), Package.FONTENC.with("T1"));

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Unsupported non-ASCII character";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "Unicode";
    }

    @NotNull
    @Override
    List<ProblemDescriptor> inspectFile(@NotNull PsiFile file, @NotNull InspectionManager
            manager, boolean isOntheFly) {

        boolean hasUnicode = unicodeEnabled(file);

        List<ProblemDescriptor> descriptors = new SmartList<>();

        Collection<LatexNormalText> texts = PsiTreeUtil.findChildrenOfType(file, LatexNormalText.class);
        for (LatexNormalText text : texts) {
            Matcher matcher = NONASCII_PATTERN.matcher(text.getText());
            while (matcher.find()) {
                boolean inMathMode = PsiTreeUtil.getParentOfType(text, LatexMathEnvironment.class) != null;

                if (!inMathMode && hasUnicode) {
                    continue;
                }

                descriptors.add(manager.createProblemDescriptor(
                        text,
                        new TextRange(matcher.start(), matcher.end()),
                        "Unsupported non-ASCII character",
                        ProblemHighlightType.ERROR,
                        isOntheFly,
                        new EscapeUnicodeFix(inMathMode),
                        inMathMode ? null : new InsertUnicodePackageFix()
                ));
            }
        }

        return descriptors;
    }

    private static boolean unicodeEnabled(@NotNull PsiFile file) {
        Collection<String> included = PackageUtils.getIncludedPackages(file);
        return UNICODE_PACKAGES.stream().allMatch(p -> included.contains(p.getName()));
    }

    private static class InsertUnicodePackageFix implements LocalQuickFix {
        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return "Include Unicode support packages";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
            PsiFile file = element.getContainingFile();
            Document document = PsiDocumentManager.getInstance(project).getDocument(file);
            Collection<String> included = PackageUtils.getIncludedPackages(file);

            if (document != null) {
                UNICODE_PACKAGES.forEach(p -> {
                    if (!included.contains(p.getName())) {
                         PackageUtils.insertUsepackage(
                                 document,
                                 file,
                                 p.getName(),
                                 String.join(",", p.getParameters())
                         );
                    }
                });
            }

        }
    }

    private static class EscapeUnicodeFix implements LocalQuickFix {

        private boolean inMathMode;

        EscapeUnicodeFix(boolean inMathMode) {
            this.inMathMode = inMathMode;
        }

        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return "Escape Unicode character";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();

            String c = descriptor.getTextRangeInElement().substring(element.getText());

            // Try to find in lookup for special command
            String replacement = null;
            LatexCommand command = inMathMode ? LatexMathCommand.findByDisplay(c) :
                    LatexNoMathCommand.findByDisplay(c);

            // Replace with found command or with standard substitution
            if (command != null) {
                replacement = "\\" + command.getCommand();
            } else {
                replacement = findReplacement(c);
            }

            // When no replacement is found, show error message
            Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
            if (replacement == null) {
                Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                if (editor != null) {
                    HintManager.getInstance().showErrorHint(editor, "Character could not be converted");
                }
                return;
            }

            // Fill in replacement
            TextRange range = descriptor.getTextRangeInElement().shiftRight(element.getTextOffset());
            if (document != null) {
                document.replaceString(range.getStartOffset(), range.getEndOffset(), replacement);
            }

        }

        @Nullable
        private String findReplacement(@NotNull String c) {
            String n = Normalizer.normalize(c, Normalizer.Form.NFD);

            // Extract base characters
            Matcher matcher = BASE_PATTERN.matcher(n);
            matcher.find();
            String base = matcher.group();

            // Extract modifiers
            String[] mods = n.substring(matcher.end()).split("");

            // Diacritics
            BiFunction<String, Diacritic, String> reducer = (s, d) -> d.buildCommand(s);
            List<Diacritic> diacritics = IntStream.range(0, mods.length)
                    // Modifiers in reversed order
                    .mapToObj(i -> mods[mods.length - i - 1])
                    .map(m -> (Diacritic)(inMathMode ? Diacritic.Math.Companion.fromUnicode(m)
                            : Diacritic.Normal.Companion.fromUnicode(m)))
                    .collect(Collectors.toList());

            return Diacritic.Companion.buildChain(base, diacritics);
        }
    }
}
