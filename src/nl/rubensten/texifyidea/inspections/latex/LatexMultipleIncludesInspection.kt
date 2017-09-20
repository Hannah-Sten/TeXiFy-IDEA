package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.util.PackageUtils
import nl.rubensten.texifyidea.util.commandsInFile
import nl.rubensten.texifyidea.util.requiredParameter
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * @author Ruben Schellekens
 */
open class LatexMultipleIncludesInspection : TexifyInspectionBase() {

    override fun getInspectionGroup() = InsightGroup.LATEX

    override fun getInspectionId() = "MultipleIncludes"

    override fun getDisplayName() = "Package has been imported multiple times"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        // Find all duplicates.
        val packages = PackageUtils.getIncludedPackagesList(file)
        val covered = HashSet<String>()
        val duplicates = HashSet<String>()
        packages.filterNotTo(duplicates) {
            covered.add(it)
        }

        // Duplicates!
        file.commandsInFile()
                .filter { it.name == "\\usepackage" && it.requiredParameter(0) in duplicates }
                .forEach {
                    val nameLength = it.requiredParameter(0)?.length ?: 1
                    descriptors.add(manager.createProblemDescriptor(
                            it,
                            TextRange.from(it.commandToken.textLength + 1, nameLength),
                            "Package has already been included",
                            ProblemHighlightType.GENERIC_ERROR,
                            isOntheFly
                    ))
                }

        return descriptors
    }
}