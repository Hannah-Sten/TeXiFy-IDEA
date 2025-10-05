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
import java.util.*

/**
 * @author Hannah Schellekens
 */
class LatexUnresolvedReferenceInspection : AbstractTexifyContextAwareInspection(
    inspectionId = "UnresolvedReference",
    inspectionGroup = InsightGroup.LATEX,
    applicableContexts = setOf(LatexContexts.LabelReference, LatexContexts.CitationReference),
    excludedContexts = setOf(LatexContexts.InsideDefinition, LatexContexts.Preamble),
    skipChildrenInContext = setOf(LatexContexts.Comment, LatexContexts.InsideDefinition)
) {

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.COMMAND, MagicCommentScope.GROUP)!!

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // If the project filesets are not available, we do not inspect the file.
        return LatexProjectStructure.isProjectFilesetsAvailable(file.project) && super.isAvailableForFile(file)
    }

    override fun shouldInspectChildrenOf(element: PsiElement, state: LContextSet, lookup: LatexSemanticsLookup): Boolean {
        // inspect only the commands that can be references
        return LatexContexts.LabelReference !in state
    }

    override fun inspectElement(element: PsiElement, contexts: LContextSet, bundle: DefinitionBundle, file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>) {
        if (element !is LatexParameter) return
        if (!isApplicableInContexts(contexts)) return
        val parts = element.contentText().split(",")
        var offset = 1 // account for {[(
        for (part in parts) {
            val label = part.trim()
            run {
                if (label.isEmpty() || label == "*") {
                    return@run
                }
                if (LatexLabelParameterReference.isLabelDefined(label, file)) {
                    return@run
                }
                if (NewBibtexEntryIndex.existsByNameInFileSet(label, file)) {
                    return@run
                }
                val labelOffset = part.indexOfFirst { !it.isWhitespace() }
                descriptors.add(
                    manager.createProblemDescriptor(
                        element,
                        TextRange.from(offset + labelOffset, label.length),
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