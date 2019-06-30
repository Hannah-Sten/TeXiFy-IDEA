package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexRequiredParam
import nl.hannahsten.texifyidea.util.PackageUtils
import nl.hannahsten.texifyidea.util.commandsInFile
import nl.hannahsten.texifyidea.util.firstChildOfType
import nl.hannahsten.texifyidea.util.requiredParameter
import java.util.*
import kotlin.collections.HashSet

/**
 * @author Hannah Schellekens
 */
open class LatexMultipleIncludesInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "MultipleIncludes"

    override val ignoredSuppressionScopes = EnumSet.of(MagicCommentScope.ENVIRONMENT, MagicCommentScope.MATH_ENVIRONMENT)!!

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "Package has been imported multiple times"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        // Find all duplicates.
        val packages = PackageUtils.getIncludedPackagesList(file)
        val covered = HashSet<String>()
        val duplicates = HashSet<String>()
        packages.filterNotTo(duplicates) {
            covered.add(it)
        }

        // Duplicates!
        file.commandsInFile().asSequence()
                .filter { it.name == "\\usepackage" && it.requiredParameter(0) in duplicates }
                .forEach {
                    val parameter = it.firstChildOfType(LatexRequiredParam::class) ?: error("There must be a required parameter.")
                    descriptors.add(manager.createProblemDescriptor(
                            it,
                            TextRange.from(parameter.textOffset + 1 - it.textOffset, parameter.textLength - 2),
                            "Package has already been included",
                            ProblemHighlightType.GENERIC_ERROR,
                            isOntheFly
                    ))
                }

        return descriptors
    }
}