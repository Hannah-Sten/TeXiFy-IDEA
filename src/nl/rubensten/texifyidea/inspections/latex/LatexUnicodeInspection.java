package nl.rubensten.texifyidea.inspections.latex;

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
import nl.rubensten.texifyidea.insight.InsightGroup;
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase;
import nl.rubensten.texifyidea.lang.Diacritic;
import nl.rubensten.texifyidea.lang.LatexCommand;
import nl.rubensten.texifyidea.lang.LatexMathCommand;
import nl.rubensten.texifyidea.lang.LatexRegularCommand;
import nl.rubensten.texifyidea.psi.LatexMathEnvironment;
import nl.rubensten.texifyidea.psi.LatexNormalText;
import nl.rubensten.texifyidea.util.Magic;
import nl.rubensten.texifyidea.util.PackageUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.Normalizer;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Checks whether Unicode is enabled in the file and flags illegal characters when they are not
 * supported.
 * <p>
 * Flags non-ASCII characters outside of math mode only when Unicode support packages are not
 * loaded. Unicode support is assumed when the packages {@code inputenc} and {@code fontenc} are
 * loaded. The inspection always flags non-ASCII characters in math mode, because Unicode math has
 * no support package in pdfLaTeX.
 * <p>
 * Quick fixes: <ul> <li>Escape the character: see {@link EscapeUnicodeFix}</li> <li>(When outside
 * math mode) Insert Unicode support packages: see {@link InsertUnicodePackageFix}</li> </ul>
 *
 * @author Sten Wessel
 */
public class LatexUnicodeInspection extends TexifyInspectionBase {

    private static final Pattern BASE_PATTERN = Pattern.compile("^\\p{ASCII}*");

    @NotNull
    @Override
    public InsightGroup getInspectionGroup() {
        return InsightGroup.LATEX;
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Unsupported non-ASCII character";
    }

    @NotNull
    @Override
    public String getInspectionId() {
        return "Unicode";
    }

    @NotNull
    @Override
    public List<ProblemDescriptor> inspectFile(@NotNull PsiFile file, @NotNull InspectionManager
            manager, boolean isOntheFly) {

        boolean hasUnicode = unicodeEnabled(file);

        List<ProblemDescriptor> descriptors = descriptorList();

        Collection<LatexNormalText> texts = PsiTreeUtil.findChildrenOfType(file, LatexNormalText.class);
        for (LatexNormalText text : texts) {
            Matcher matcher = Magic.Pattern.nonAscii.matcher(text.getText());
            while (matcher.find()) {
                boolean inMathMode = PsiTreeUtil.getParentOfType(text, LatexMathEnvironment.class) != null;

                if (!inMathMode && hasUnicode) {
                    // Unicode is supported, characters are legal
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

    /**
     * Checks whether Unicode support is enabled for the file.
     * <p>
     * Support is assumed when the packages {@code inputenc} and {@code fontenc} are loaded. The
     * loaded options are not checked.
     *
     * @param file
     *         The file to check support for.
     * @return Whether Unicode support is enabled.
     */
    static boolean unicodeEnabled(@NotNull PsiFile file) {
        // TODO: check if options are correct as well
        Collection<String> included = PackageUtils.getIncludedPackages(file);
        return Magic.Package.unicode.stream().allMatch(p -> included.contains(p.getName()));
    }

    /**
     * Inserts required packages for Unicode support.
     * <p>
     * This is only available for Unicode support outside math mode, since Unicode in math is not
     * available in pdfLaTeX.
     * <p>
     * This fix loads the packages {@code inputenc} with option {@code utf8} and {@code fontenc}
     * with option {@code T1} when needed to enable unicode support.
     */
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

            if (document == null) {
                return;
            }

            Magic.Package.unicode.forEach(p -> {
                if (included.contains(p.getName())) {
                    return;
                }

                PackageUtils.insertUsepackage(
                        document, file, p.getName(),
                        String.join(",", p.getParameters())
                );
            });
        }
    }

    /**
     * Attempts to escape the non-ASCII character to avoid encoding issues.
     * <p>
     * The following attempts are made, in order, to determine a suitable replacement: <ol> <li> The
     * character is matched against the <em>display</em> attribute of either {@link
     * LatexRegularCommand} or {@link LatexMathCommand} (where appropiate). When there is a match,
     * the corresponding command is used as replacement. </li> <li> The character is decomposed to
     * separate combining marks (see also <a href="http://unicode.org/reports/tr15/">Unicode</a>).
     * An attempt is made to match the combining sequence against LaTeX character diacritical
     * commands. See {@link Diacritic} for a list of supported diacritics for both non-math and math
     * mode. When there is a match for all combining marks, the sequence of LaTeX commands is used
     * as replacement. Also, when the letters <em>i</em> or <em>j</em> are used in combination with
     * a diacritic their dotless versions are substituted. </li> </ol>
     * <p>
     * When neither of these steps is successful, the character is too exotic to replace and an
     * appropriate fail message is shown.
     */
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
            String replacement;
            LatexCommand command = inMathMode ? LatexMathCommand.findByDisplay(c) :
                    LatexRegularCommand.findByDisplay(c);

            // Replace with found command or with standard substitution
            if (command != null) {
                replacement = "\\" + command.getCommand();
            }
            else {
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
