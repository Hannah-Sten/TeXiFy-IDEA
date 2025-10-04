package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.Diacritic
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.commands.LatexMathCommand
import nl.hannahsten.texifyidea.lang.commands.LatexRegularCommand
import nl.hannahsten.texifyidea.psi.LatexContent
import nl.hannahsten.texifyidea.psi.LatexMathEnvironment
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.settings.sdk.MiktexWindowsSdk
import nl.hannahsten.texifyidea.settings.sdk.TexliveSdk
import nl.hannahsten.texifyidea.util.includedPackages
import nl.hannahsten.texifyidea.util.insertUsepackage
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.magic.PackageMagic
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.parser.findDependencies
import nl.hannahsten.texifyidea.util.parser.firstChildOfType
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.jetbrains.annotations.Nls
import java.text.Normalizer
import java.util.regex.Pattern

/**
 * Checks whether Unicode is enabled in the file and flags illegal characters when they are not
 * supported.
 *
 *
 * Flags non-ASCII characters outside of math mode only when Unicode support packages are not
 * loaded. Unicode support is assumed when the packages `inputenc` and `fontenc` are
 * loaded. The inspection always flags non-ASCII characters in math mode, because Unicode math has
 * no support package in pdfLaTeX.
 *
 * Quick fixes:
 * * Escape the character: see [EscapeUnicodeFix]
 * * (When outside math mode) Insert Unicode support packages: see [InsertUnicodePackageFix]
 *
 * @author Sten Wessel
 */
class LatexUnicodeInspection : TexifyInspectionBase() {

    object Util {
        internal val BASE_PATTERN = Pattern.compile("^\\p{ASCII}*")

        /**
         * Checks whether Unicode support is enabled for the file.
         *
         * Support is assumed when the packages `inputenc` and `fontenc` are
         * loaded, or when a compiler is used which does not need these packages. The
         * loaded options are not checked.
         *
         * @param file The file to check support for.
         * @return Whether Unicode support is enabled.
         */
        internal fun unicodeEnabled(file: PsiFile): Boolean {
            // TeX Live 2018 is UTF-8 by default and loads inputenc automatically
            val compilerCompat = file.project.selectedRunConfig()?.let { it.options.compiler }
            if (compilerCompat?.supportsUnicode == true ||
                TexliveSdk.Cache.version >= 2018  ||
                MiktexWindowsSdk().getVersion(null) >= DefaultArtifactVersion("2.9.7350")) {
                return true
            }

            // If we can't figure it out by compiler, check included packages
            val included = file.includedPackages()
            return PackageMagic.unicode.stream().allMatch { p -> included.contains(p) }
        }
    }

    override val inspectionGroup = InsightGroup.LATEX

    @Nls
    override fun getDisplayName(): String {
        return "Unsupported non-ASCII character"
    }

    override val inspectionId = "Unicode"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val hasUnicode = Util.unicodeEnabled(file)

        val descriptors = descriptorList()

        val texts = PsiTreeUtil.findChildrenOfType(file, LatexNormalText::class.java)
        for (text in texts) {
            val matcher = PatternMagic.nonAscii.matcher(text.text)
            while (matcher.find()) {
                val inMathMode = PsiTreeUtil.getParentOfType(text, LatexMathEnvironment::class.java) != null

                if (!inMathMode && hasUnicode) {
                    // Unicode is supported, characters are legal
                    continue
                }

                val fix = if (inMathMode) {
                    null
                }
                else {
                    InsertUnicodePackageFix()
                }
                val fixes = listOfNotNull(fix).toTypedArray()

                descriptors.add(
                    manager.createProblemDescriptor(
                        text,
                        TextRange(matcher.start(), matcher.end()),
                        "Unsupported non-ASCII character",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly,
                        EscapeUnicodeFix(inMathMode),
                        *fixes
                    )
                )
            }
        }

        return descriptors
    }

    /**
     * Inserts required packages for Unicode support.
     *
     *
     * This is only available for Unicode support outside math mode, since Unicode in math is not
     * available in pdfLaTeX.
     *
     *
     * This fix loads the packages `inputenc` with option `utf8` and `fontenc`
     * with option `T1` when needed to enable unicode support.
     */
    private class InsertUnicodePackageFix : LocalQuickFix {

        @Nls
        override fun getFamilyName(): String {
            return "Include Unicode support packages"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val file = descriptor.psiElement.containingFile

            PackageMagic.unicode.forEach { p ->
                file.insertUsepackage(p)
            }
        }
    }

    /**
     * Attempts to escape the non-ASCII character to avoid encoding issues.
     *
     * The following attempts are made, in order, to determine a suitable replacement:   1.  The
     * character is matched against the *display* attribute of either [ ] or [LatexCommand] (where appropriate).
     * When there is a match, the corresponding command is used as replacement. 1.  The character is decomposed to
     * separate combining marks (see also [Unicode](http://unicode.org/reports/tr15/)).
     * An attempt is made to match the combining sequence against LaTeX character diacritical
     * commands. See [Diacritic] for a list of supported diacritics for both non-math and math
     * mode. When there is a match for all combining marks, the sequence of LaTeX commands is used
     * as replacement. Also, when the letters *i* or *j* are used in combination with
     * a diacritic their dotless versions are substituted.
     *
     * When neither of these steps is successful, the character is too exotic to replace and an
     * appropriate fail message is shown.
     */
    private class EscapeUnicodeFix(private val inMathMode: Boolean) : LocalQuickFix {

        @Nls
        override fun getFamilyName(): String {
            return "Escape Unicode character"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            val replacement = getReplacementFromProblemDescriptor(descriptor)

            // When no replacement is found, show error message
            val document = PsiDocumentManager.getInstance(project).getDocument(element.containingFile)
            if (replacement == null) {
                if (editor != null) {
                    runInEdt {
                        HintManager.getInstance().showErrorHint(editor, "Character could not be converted")
                    }
                }
                return
            }

            runWriteCommandAction(project) {
                replacement.findDependencies().forEach { pkg ->
                    document?.psiFile(project)?.insertUsepackage(pkg)
                }

                val command = replacement.firstChildOfType(LatexContent::class) ?: return@runWriteCommandAction
                element.parent?.node?.replaceChild(element.node, command.node)
            }
        }

        /**
         * Extend the heuristics implemented in [LocalQuickFix.generatePreview] that predict that the fix can not be applied.
         * We cannot use the automatically generated preview because in the applyFix we sometimes show a UI element, which breaks the preview
         */
        override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo {
            val replacement = getReplacementFromProblemDescriptor(previewDescriptor) ?: return IntentionPreviewInfo.EMPTY
            val element = previewDescriptor.psiElement

            return IntentionPreviewInfo.CustomDiff(LatexFileType, element.containingFile.name, element.text, replacement.text)
        }

        private fun getReplacementFromProblemDescriptor(descriptor: ProblemDescriptor): PsiElement? {
            val element = descriptor.psiElement

            val c = try {
                descriptor.textRangeInElement.substring(element.text)
            }
            catch (e: IndexOutOfBoundsException) {
                return null
            }

            // Try to find in lookup for special command
            val replacementText: String?
            val command: LatexCommand? = if (inMathMode) {
                LatexMathCommand.findByDisplay(c)?.firstOrNull() ?: LatexRegularCommand.findByDisplay(c)?.firstOrNull()
            }
            else {
                LatexRegularCommand.findByDisplay(c)?.firstOrNull()
            }

            // Replace with found command or with standard substitution
            replacementText = command?.let { "\\" + it.command }
                ?: findReplacement(c)

            return replacementText?.let {
                LatexPsiHelper(element.project).createFromText(element.text.replaceRange(descriptor.textRangeInElement.toIntRange(), it))
            }
        }

        private fun findReplacement(c: String): String? {
            val n = Normalizer.normalize(c, Normalizer.Form.NFD)

            // Extract base characters
            val matcher = Util.BASE_PATTERN.matcher(n)
            matcher.find()
            val base = matcher.group()

            // Extract modifiers
            val mods = n.substring(matcher.end()).split("".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            val diacritics = (mods.indices)
                // Modifiers in reversed order
                .map { mods[mods.size - 1 - it] }
                .mapNotNull {
                    @Suppress("USELESS_CAST")
                    if (inMathMode)
                        Diacritic.Math.fromUnicode(it) as? Diacritic
                    else
                        Diacritic.Normal.fromUnicode(it) as? Diacritic
                }

            return Diacritic.buildChain(base, diacritics)
        }
    }
}
