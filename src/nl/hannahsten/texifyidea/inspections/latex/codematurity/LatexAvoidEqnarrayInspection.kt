package nl.hannahsten.texifyidea.inspections.latex.codematurity

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.inspections.AbstractTexifyEnvironmentBasedInspection
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.AMSMATH
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import java.util.*

/**
 * @author Hannah Schellekens
 */
class LatexAvoidEqnarrayInspection : AbstractTexifyEnvironmentBasedInspection(
    inspectionId = "AvoidEqnarray",
) {

    override val outerSuppressionScopes: Set<MagicCommentScope>
        get() = EnumSet.of(MagicCommentScope.GROUP)

    override fun inspectEnvironment(environment: LatexEnvironment, contexts: LContextSet, defBundle: DefinitionBundle, file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>) {
        val name = environment.getEnvironmentName()
        if(name == "eqnarray" || name == "eqnarray*") {
            val star = name.substring("eqnarray".length)
            descriptors.add(
                manager.createProblemDescriptor(
                    environment,
                    TextRange.from(7, name.length),
                    "Avoid using the 'eqnarray$star' environment",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOnTheFly,
                    ReplaceEnvironmentQuickFix(
                        fixName = "Convert to 'align$star' (amsmath)",
                        newName = "align$star",
                        requiredPkg = LatexLib.AMSMATH
                    )
                )
            )
        }
    }
}