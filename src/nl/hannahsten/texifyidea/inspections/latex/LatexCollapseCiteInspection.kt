package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.refactoring.suggested.endOffset
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexContent
import nl.hannahsten.texifyidea.psi.LatexParameterText
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.magic.CommandMagic
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
        for (cmd in commands) {
            if (cmd.name !in CommandMagic.bibliographyReference) {
                continue
            }

            val bundle = cmd.findCiteBundle().filter { it.optionalParameterMap.isEmpty() }
            if (bundle.size < 2 || !bundle.contains(cmd)) {
                continue
            }

            descriptors.add(
                manager.createProblemDescriptor(
                    cmd,
                    "Citations can be collapsed",
                    InspectionFix(bundle),
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

    private fun LatexCommands.nextCite() = searchCite { it.nextSiblingIgnoreWhitespace() as? LatexContent }

    private fun LatexCommands.previousCite() = searchCite { it.previousSiblingIgnoreWhitespace() as? LatexContent }

    private inline fun LatexCommands.searchCite(nextThing: (LatexContent) -> LatexContent?): LatexCommands? {
        val content = grandparent(2) as? LatexContent ?: return null
        val nextContent = nextThing(content) ?: return null

        var cite = nextContent.firstChildOfType(LatexCommands::class)
        if (cite == null) {
            if (!nextContent.isCorrect()) {
                return null
            }

            val secondNextContent = nextThing(nextContent) ?: return null
            cite = secondNextContent.firstChildOfType(LatexCommands::class) ?: return null
        }

        val name = cite.name ?: return null
        val nextCommandIsACitation = name in CommandMagic.bibliographyReference
        val previousCommandIsOfTheSameType = this.name == name
        val equalStars = hasStar() == cite.hasStar()
        return if (nextCommandIsACitation && previousCommandIsOfTheSameType && equalStars) cite else null
    }

    private fun LatexContent.isCorrect(): Boolean {
        val normalText = firstChildOfType(LatexParameterText::class) ?: return false
        return normalText.text.length == 1
    }

    /**
     * @property citeBundle a bundle of cite commands that have to be merged into one command. All commands
     * in this bundle have only required parameters, as cites with optional parameters should not
     * be collapsed.
     */
    private inner class InspectionFix(val citeBundle: List<LatexCommands>) : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Collapse citations"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val sortedBundle = citeBundle.sortedBy { it.textOffset }
            // The bundle can contain a gap when the cite commands in it surround a cite command
            // that is not in the bundle, e.g., a cite command that has an optional parameter.
            val bundleContainsGap = sortedBundle
                .zipWithNext { a, b -> a.nextSibling != b }
                .any()

            // Create the content of the required parameter of the new cite command.
            val bundle = sortedBundle
                .flatMap { it.requiredParameters }
                .joinToString(",")

            // Find the cite command that has to be replaced. When the bundle contains a gap, this is the command
            // underneath the caret.
            val targetCite = if (bundleContainsGap) {
                val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
                sortedBundle.firstOrNull { it.endOffset >= editor.caretOffset() }
                    // When something went wrong with finding a cite at the caret we target the last of the cites
                    // based on the assumption that cites that have an optional argument are more specific and/or
                    // important cites and "should" come first.
                    ?: sortedBundle.lastOrNull() ?: return
            }
            // When the bundle does not contain a gap this is the first command, as it doesn't matter
            // whichever one we pick.
            else sortedBundle.firstOrNull() ?: return

            // Construct the entire text of the new cite command.
            val star = if (targetCite.hasStar()) "*" else ""
            val replacement = "${targetCite.name}$star{$bundle}"

            val psiHelper = LatexPsiHelper(project)
            for (cite in sortedBundle) {
                // Replace the target cite with the new cite command, using the psi tree.
                if (cite == targetCite) {
                    cite.replace(psiHelper.createFromText(replacement).firstChild)
                }
                // Remove any other cite from the psi tree.
                else cite.parent.node.removeChild(cite.node)
            }
        }
    }
}
