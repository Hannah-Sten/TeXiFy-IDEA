package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.index.NewBibtexEntryIndex
import nl.hannahsten.texifyidea.inspections.AbstractTexifyContextAwareInspection
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.psi.contentText
import nl.hannahsten.texifyidea.reference.LatexLabelParameterReference
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import java.lang.Integer.max
import java.util.*

/**
 * @author Hannah Schellekens
 */
class LatexUnresolvedReferenceInspection : AbstractTexifyContextAwareInspection(
    inspectionId = "UnresolvedReference",
    inspectionGroup = InsightGroup.LATEX,
    applicableContexts = setOf(LatexContexts.LabelReference, LatexContexts.CitationReference),
    excludedContexts = setOf(LatexContexts.InsideDefinition, LatexContexts.Preamble)
) {

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.COMMAND, MagicCommentScope.GROUP)!!

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // If the project filesets are not available, we do not inspect the file.
        return LatexProjectStructure.isProjectFilesetsAvailable(file.project)
    }

    override fun shouldInspectChildrenOf(element: PsiElement, state: LContextSet, lookup: LatexSemanticsLookup): Boolean {
        // inspect only the commands that can be references
        return LatexContexts.LabelReference !in state
    }

    private fun isInsideDefinition(element: PsiElement, lookup: DefinitionBundle): Boolean {
        // unfortunately, the context does not contain enough information to determine whether we are inside a command definition as they can be overridden
        // For example, in `\newcommand{\myref}[1]{$ \text{\ref{#1}} $}`,  `#1` only has the context, while `\ref` has context `<text>`.
        // To improve it, we have to introduce penetrating context or conflicting context, making our context system more complex.
        // When we find more use cases requiring advanced context system later, we can upgrade it to fit the needs
        val introList = LatexPsiUtil.resolveContextIntroUpward(element, lookup, shortCircuit = false)
        return introList.any { it.introduces(LatexContexts.InsideDefinition) }
    }

    override fun inspectElement(element: PsiElement, contexts: LContextSet, lookup: DefinitionBundle, file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>) {
        if(element !is LatexParameter) return
        if(!isApplicableInContexts(contexts)) return
        val parts = element.contentText().split(",")
        var offset = 1 // account for {[(
        var checkedInsideDefinition = false
        for (part in parts) {
            val label = part.trim()
            run {
                if (label.isEmpty() || label == "*") {
                    return@run
                }
                if (LatexLabelParameterReference.isLabelDefined(label, file)) {
                    return@run
                }
                if (NewBibtexEntryIndex.existsByNameInFileSet(part, file)) {
                    return@run
                }
                if (!checkedInsideDefinition) {
                    if (isInsideDefinition(element, lookup)) {
                        return // skip all if inside definition
                    }
                    checkedInsideDefinition = true
                }
                descriptors.add(
                    manager.createProblemDescriptor(
                        element,
                        TextRange.from(max(offset, 0), label.length),
                        "Unresolved reference '$label'",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOnTheFly
                    )
                )
            }
            offset += part.length + 1
        }
    }
}