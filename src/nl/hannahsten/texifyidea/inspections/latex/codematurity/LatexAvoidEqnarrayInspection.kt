package nl.hannahsten.texifyidea.inspections.latex.codematurity

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.AMSMATH
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import nl.hannahsten.texifyidea.util.parser.endOffset
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.insertUsepackage
import nl.hannahsten.texifyidea.util.parser.forEachChildTyped
import java.util.*

/**
 * @author Hannah Schellekens
 */
open class LatexAvoidEqnarrayInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "AvoidEqnarray"

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "Avoid eqnarray"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        file.forEachChildTyped<LatexEnvironment> { env ->
            val name = env.getEnvironmentName()
            if(name == "eqnarray" || name == "eqnarray*") {
                val star = name.substring("eqnarray".length)
                descriptors.add(
                    manager.createProblemDescriptor(
                        env,
                        TextRange.from(7, name.length),
                        "Avoid using the 'eqnarray$star' environment",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly,
                        ChangeEnvironmentFix(star)
                    )
                )
            }
        }
        return descriptors
    }

    /**
     * @author Hannah Schellekens
     */
    private open class ChangeEnvironmentFix(val star: String) : LocalQuickFix {

        override fun getFamilyName() = "Convert to 'align$star' (amsmath)"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val environment = descriptor.psiElement as LatexEnvironment
            val file = environment.containingFile
            val document = file.document() ?: return
            val begin = environment.beginCommand
            val end = environment.endCommand ?: return

            document.replaceString(end.textOffset, end.endOffset(), "\\end{align$star}")
            document.replaceString(begin.textOffset, begin.endOffset(), "\\begin{align$star}")

            file.insertUsepackage(AMSMATH)
        }
    }
}