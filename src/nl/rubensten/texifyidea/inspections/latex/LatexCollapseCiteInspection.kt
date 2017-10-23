package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.LatexContent
import nl.rubensten.texifyidea.psi.LatexNormalText
import nl.rubensten.texifyidea.util.*
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * @author Ruben Schellekens
 */
open class LatexCollapseCiteInspection : TexifyInspectionBase() {

    override fun getInspectionGroup() = InsightGroup.LATEX

    override fun getInspectionId() = "CollapseCite"

    override fun getDisplayName() = "Collapce cite commands"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        val commands = LatexCommandsIndex.getItems(file).toList()
        for (cmd in commands) {
            if (cmd.name != "\\cite") {
                continue
            }

            val bundle = cmd.findCiteBundle()
            if (bundle.size <= 1) {
                continue
            }

            descriptors.add(manager.createProblemDescriptor(
                    cmd,
                    "Citations can be collapsed",
                    InspectionFix(bundle),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly
            ))
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
        return if ("\\cite" == name) cite else null
    }

    private fun LatexContent.isCorrect(): Boolean {
        val normalText = firstChildOfType(LatexNormalText::class) ?: return false
        return normalText.text.length == 1
    }

    /**
     * @author Ruben Schellekens
     */
    private inner class InspectionFix(val citeBundle: List<LatexCommands>) : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Collapse citations"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val file = descriptor.psiElement.containingFile ?: return
            val document = file.document() ?: return
            val sortedBundle = citeBundle.sortedBy { it.textOffset }
            val offsetRange = sortedBundle.first().textOffset until sortedBundle.last().endOffset()

            val bundle = sortedBundle
                    .flatMap { it.requiredParameters }
                    .joinToString(",")

            document.replaceString(offsetRange.toTextRange(), "\\cite{$bundle}")
        }
    }
}