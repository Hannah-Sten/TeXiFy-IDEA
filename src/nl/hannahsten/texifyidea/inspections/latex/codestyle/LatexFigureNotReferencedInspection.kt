package nl.hannahsten.texifyidea.inspections.latex.codestyle

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.createSmartPointer
import com.intellij.psi.search.searches.ReferencesSearch
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.inspections.AbstractTexifyContextAwareInspection
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.SafeDeleteFix
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import nl.hannahsten.texifyidea.util.parser.findFirstChildOfType
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped
import java.util.*

open class LatexFigureNotReferencedInspection : AbstractTexifyContextAwareInspection(
    inspectionId = "FigureNotReferenced",
    inspectionGroup = InsightGroup.LATEX,
    applicableContexts = null,
    excludedContexts = setOf(),
    skipChildrenInContext = setOf(LatexContexts.Comment, LatexContexts.InsideDefinition),
) {

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName(): String = "Figure not referenced"

    override fun inspectElement(element: PsiElement, contexts: LContextSet, bundle: DefinitionBundle, file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>) {
        if (element !is LatexEnvironment) return
        if (element.getEnvironmentName() !in EnvironmentMagic.figures) return

        // Does not yet support other labels like defined in parameters
        val label = element.getLabelCommand() ?: return
        val labelText = label.findFirstChildTyped<LatexParameterText>() ?: return
        if (ReferencesSearch.search(labelText).none()) {
            descriptors.add(createDescriptor(manager, label, isOnTheFly) ?: return)
        }
    }

    private fun createDescriptor(manager: InspectionManager, label: LatexCommands, isOntheFly: Boolean): ProblemDescriptor? =
        label.findFirstChildOfType(LatexParameterText::class)?.let {
            manager.createProblemDescriptor(
                it,
                "Figure is not referenced",
                RemoveFigureFix(it.createSmartPointer()),
                ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                isOntheFly
            )
        }

    class RemoveFigureFix(label: SmartPsiElementPointer<LatexParameterText>) : SafeDeleteFix(label.element as PsiElement) {

        override fun getText(): String = "Safe delete figure environment"
    }
}

private val LatexCommands.labelName: String?
    get() = requiredParameterText(0)

private val LatexCommands.referencedLabelNames: List<String>
    get() = requiredParameterText(0)?.split(",") ?: emptyList()