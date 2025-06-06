package nl.hannahsten.texifyidea.inspections.latex.typesetting

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.endOffset
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexNoMathContent
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.caretOffset
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.*
import java.util.*

/**
 * @author Hannah Schellekens
 */
open class LatexCollapseCiteInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "CollapseCite"

    override val ignoredSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.COMMAND)!!

    override fun getDisplayName() = "Collapse cite commands"

    override fun inspectFile(
        file: PsiFile,
        manager: InspectionManager,
        isOntheFly: Boolean
    ): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        val commands = file.commandsInFile()
            .filter { it.name in CommandMagic.bibliographyReference }

        for (cmd in commands) {
            val bundle = cmd.findCiteBundle().filter { it.getOptionalParameterMap().isEmpty() }
            if (bundle.size < 2 || !bundle.contains(cmd)) {
                continue
            }

            descriptors.add(
                manager.createProblemDescriptor(
                    cmd,
                    "Citations can be collapsed",
                    InspectionFix(bundle.map { SmartPointerManager.createPointer(it) }),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly
                )
            )
        }

        return descriptors
    }

    private fun LatexCommands.findCiteBundle(): List<LatexCommands> {
        val bundle: MutableList<LatexCommands> = ArrayList()

        // Lookbefore
        var foundBefore: LatexCommands? = previousCite()
        while (foundBefore != null) {
            bundle.add(foundBefore)
            foundBefore = foundBefore.previousCite()
        }

        // Current
        bundle.add(this)

        // Lookahead
        var foundAfter: LatexCommands? = nextCite()
        while (foundAfter != null) {
            bundle.add(foundAfter)
            foundAfter = foundAfter.nextCite()
        }

        return bundle
    }

    private fun LatexCommands.nextCite() = searchCite { it.nextSiblingIgnoreWhitespace() as? LatexNoMathContent }

    private fun LatexCommands.previousCite() = searchCite { it.previousSiblingIgnoreWhitespace() as? LatexNoMathContent }

    /**
     * Search for a cite command in the "direction" of [nextThing]. This cite command should be similar to this with
     * respect to the following:
     *  - Same cite command (e.g., both `\cite` or both `\footcite`).
     *  - Both are or both are not the starred version.
     *
     * @param nextThing function to get the next psi element that is searched for a cite command.
     *
     * @return null if no suitable cite command is found.
     */
    private inline fun LatexCommands.searchCite(nextThing: (LatexNoMathContent) -> LatexNoMathContent?): LatexCommands? {
        // The cite commands are not direct siblings, but their grandparents are.
        val content = firstParentOfType(LatexNoMathContent::class) ?: return null
        val nextContent = nextThing(content) ?: return null

        var cite = nextContent.firstChild
        // If there is no command inside the sibling grandparent, it can still be a non-breaking space.
        if (cite !is LatexCommands) {
            // If it is a non-breaking space, we want to check if the next grandparent sibling contains a cite.
            if (nextContent.isNonBreakingSpace()) {
                val secondNextContent = nextThing(nextContent) ?: return null
                cite = secondNextContent.firstChild ?: return null
            }
            // If it is not a non-breaking space it is some other text and we won't find another cite in this direction.
            else return null
        }

        // Check if the found command is a similar cite command as the one we started at.
        if (cite !is LatexCommands) return null
        val name = cite.name ?: return null
        val nextCommandIsACitation = name in CommandMagic.bibliographyReference
        val previousCommandIsOfTheSameType = this.name == name
        val equalStars = hasStar() == cite.hasStar()
        return if (nextCommandIsACitation && previousCommandIsOfTheSameType && equalStars) cite else null
    }

    /**
     * Check if [LatexNoMathContent] is a non breaking space.
     */
    private fun LatexNoMathContent.isNonBreakingSpace(): Boolean {
        val normalText = firstChildOfType(LatexNormalText::class) ?: return false
        return normalText.text == "~"
    }

    /**
     * @property citeBundle a bundle of cite commands that have to be merged into one command. All commands
     * in this bundle have only required parameters, as cites with optional parameters should not
     * be collapsed.
     */
    private inner class InspectionFix(val citeBundle: List<SmartPsiElementPointer<LatexCommands>>) : LocalQuickFix {
        val sortedBundle = lazy {
            citeBundle.mapNotNull { it.element }.sortedBy { it.textOffset }
        }

        override fun getFamilyName(): String {
            return "Collapse citations"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val (targetCite, _, replacement) = replacement(project) ?: return

            val psiHelper = LatexPsiHelper(project)
            for (cite in sortedBundle.value) {
                // Replace the target cite with the new cite command, using the psi tree.
                if (cite == targetCite) {
                    cite.replace(psiHelper.createFromText(replacement).firstChild)
                }
                // Remove any other cite from the psi tree.
                else cite.parent?.node?.removeChild(cite.node)
            }
        }

        override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo {
            val (_, original, replacement) = replacement(project) ?: return IntentionPreviewInfo.EMPTY
            return IntentionPreviewInfo.CustomDiff(LatexFileType, original, replacement)
        }

        private fun replacement(project: Project): Triple<LatexCommands, String, String>? {
            // The bundle can contain a gap when the cite commands in it surround a cite command
            // that is not in the bundle, e.g., a cite command that has an optional parameter.
            val bundleContainsGap = sortedBundle.value
                .zipWithNext { a, b -> a.nextSibling != b }
                .any()

            val originalText = sortedBundle.value.firstOrNull()?.let { startCite ->
                val document = startCite.containingFile.document() ?: return@let ""
                document.text.substring(startCite.textOffset, sortedBundle.value.lastOrNull()?.textRange?.endOffset ?: document.text.length)
            } ?: ""

            // Create the content of the required parameter of the new cite command.
            val bundle = sortedBundle.value
                .flatMap { it.getRequiredParameters() }
                .joinToString(",")

            // Find the cite command that has to be replaced. When the bundle contains a gap, this is the command
            // underneath the caret.
            val targetCite = if (bundleContainsGap) {
                val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return null
                sortedBundle.value.firstOrNull { it.endOffset >= editor.caretOffset() }
                    // When something went wrong with finding a cite at the caret we target the last of the cites
                    // based on the assumption that cites that have an optional argument are more specific and/or
                    // important cites and "should" come first.
                    ?: sortedBundle.value.lastOrNull() ?: return null
            }
            // When the bundle does not contain a gap this is the first command, as it doesn't matter
            // whichever one we pick.
            else sortedBundle.value.firstOrNull() ?: return null

            // Construct the entire text of the new cite command.
            val star = if (targetCite.hasStar()) "*" else ""
            return Triple(targetCite, originalText, "${targetCite.name}$star{$bundle}")
        }
    }
}
