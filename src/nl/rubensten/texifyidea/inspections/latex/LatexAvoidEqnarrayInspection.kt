package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.lang.Package.Companion.AMSMATH
import nl.rubensten.texifyidea.lang.magic.MagicCommentScope
import nl.rubensten.texifyidea.psi.LatexEnvironment
import nl.rubensten.texifyidea.util.*
import java.util.*

/**
 * @author Ruben Schellekens
 */
open class LatexAvoidEqnarrayInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "AvoidEqnarray"

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "Avoid eqnarray"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val environments = file.childrenOfType(LatexEnvironment::class)
        for (env in environments) {
            val name = env.name()?.text ?: continue
            if (name != "eqnarray" && name != "eqnarray*") {
                continue
            }

            val star = name.substring("eqnarray".length)
            descriptors.add(manager.createProblemDescriptor(
                    env,
                    TextRange(7, 7 + name.length),
                    "Avoid using the 'eqnarray$star' environment",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly,
                    ChangeEnvironmentFix(star)
            ))
        }

        return descriptors
    }

    /**
     * @author Ruben Schellekens
     */
    private open class ChangeEnvironmentFix(val star: String) : LocalQuickFix {

        override fun getFamilyName() = "Convert to 'align$star' (amsmath)"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val environment = descriptor.psiElement as LatexEnvironment
            val file = environment.containingFile
            val document = file.document() ?: return
            val begin = environment.beginCommand
            val end = environment.endCommand

            document.replaceString(end.textOffset, end.endOffset(), "\\end{align$star}")
            document.replaceString(begin.textOffset, begin.endOffset(), "\\begin{align$star}")

            file.insertUsepackage(AMSMATH)
        }
    }
}