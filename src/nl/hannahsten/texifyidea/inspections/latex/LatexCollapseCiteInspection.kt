package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexContent
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.document
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Hannah Schellekens
 */
open class LatexCollapseCiteInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "CollapseCite"

    override val ignoredSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.COMMAND)!!

    override fun getDisplayName() = "Collapce cite commands"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        val commands = LatexCommandsIndex.getItems(file)
        for (cmd in commands) {
            if (cmd.name !in Magic.Command.bibliographyReference) {
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
        val nextCommandIsACitation = name in Magic.Command.bibliographyReference
        val previousCommandIsOfTheSameType = this.name == name
        val equalStars = hasStar() == cite.hasStar()
        return if (nextCommandIsACitation && previousCommandIsOfTheSameType && equalStars) cite else null
    }

    private fun LatexContent.isCorrect(): Boolean {
        val normalText = firstChildOfType(LatexNormalText::class) ?: return false
        return normalText.text.length == 1
    }

    /**
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
            val offsetRange = sortedBundle.first().textOffset until sortedBundle.last().endOffset()

            val bundle = sortedBundle
                    .flatMap { it.requiredParameters }
                    .joinToString(",")

            val first = citeBundle.first()
            val star = if (first.hasStar()) "*" else ""
            document.replaceString(offsetRange.toTextRange(), "${first.name}$star{$bundle}")
        }
    }
}