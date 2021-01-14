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
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.document
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
            if (cmd.name !in Magic.Command.bibliographyReference) {
                continue
            }

            val bundle = cmd.findCiteBundle().filter { it.optionalParameterMap.isEmpty() }
            if (bundle.size <= 1 || !bundle.contains(cmd)) {
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
        val nextCommandIsACitation = name in Magic.Command.bibliographyReference
        val previousCommandIsOfTheSameType = this.name == name
        val equalStars = hasStar() == cite.hasStar()
        return if (nextCommandIsACitation && previousCommandIsOfTheSameType && equalStars) cite else null
    }

    private fun LatexContent.isCorrect(): Boolean {
        val normalText = firstChildOfType(LatexParameterText::class) ?: return false
        return normalText.text.length == 1
    }

    /**
     * @param citeBundle a bundle of cite commands that have to be merged into one command. All commands
     * in this bundle have only required parameters, as cites with optional parameters should not
     * be collapsed.
     *
     * @author Hannah Schellekens
     */
    private inner class InspectionFix(val citeBundle: List<LatexCommands>) : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Collapse citations"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val file = descriptor.psiElement.containingFile ?: return
            val document = file.document() ?: return
            val sortedBundle = citeBundle.sortedBy { it.textOffset }
            // The bundle can contain a gap when the cite commands in it surround a cite command
            // that is not in the bundle, e.g., a cite command that has an optional parameter.
            val bundleContainsGap = sortedBundle
                .zipWithNext { a, b -> a.nextSibling != b }
                .any()

            // The text range of the bundle of cites, gaps included.
            val offsetRange = sortedBundle.first().textOffset until sortedBundle.last().endOffset()
            // Create the content of the required parameter of the new cite command.
            val bundle = sortedBundle
                .flatMap { it.requiredParameters }
                .joinToString(",")

            // When the bundle does contain gaps, we place the replacement at the location of the cite at which the
            // quick fix was invoked. The required parameter of this cite gets replaced by the new bundle, and all
            // other cite commands that are in the citeBundle are removed.
            val replacementText = if (bundleContainsGap) {
                val editor = FileEditorManager.getInstance(project).selectedTextEditor
                val citeAtCaret = editor?.caretOffset()?.let {
                    sortedBundle.first { cite -> cite.endOffset >= it }
                }
                    ?: sortedBundle.lastOrNull()
                    ?: return

                val star = if (citeAtCaret.hasStar()) "*" else ""
                val replacement = "${citeAtCaret.name}$star{$bundle}"
                val oldText = document.getText(offsetRange.toTextRange())

                // Replace the cite at the caret with the new bundle and remove all other cites in the citeBundle.
                sortedBundle.fold(oldText) { text, cite ->
                    text.replace(cite.text, if (cite == citeAtCaret) replacement else "")
                }
            }
            // When the bundle does not contain gaps the replacement text is simply a cite with the new bundle as
            // required argument.
            else {
                val first = citeBundle.first()
                val star = if (first.hasStar()) "*" else ""
                "${first.name}$star{$bundle}"
            }

            document.replaceString(offsetRange.toTextRange(), replacementText)
        }
    }
}
