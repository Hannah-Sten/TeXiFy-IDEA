package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.util.commandsInFile
import nl.rubensten.texifyidea.util.commandsInFileSet
import nl.rubensten.texifyidea.util.requiredParameter
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * @author Ruben Schellekens
 */
open class LatexDuplicateLabelInspection : TexifyInspectionBase() {

    override fun getInspectionGroup() = InsightGroup.LATEX

    override fun getDisplayName() = "Duplicate labels"

    override fun getInspectionId(): String = "DuplicateLabel"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        // Fill up a set of labels.
        val labels: MutableSet<String> = HashSet()
        val firstPass: MutableSet<String> = HashSet()
        for (cmd in file.commandsInFileSet()) {
            val labelName = cmd.requiredParameter(0) ?: continue

            if (cmd.name != "\\label" && cmd.name != "\\bibitem") {
                continue
            }

            if (labelName in firstPass) {
                labels.add(labelName)
                continue
            }

            firstPass.add(labelName)
        }

        // Check labels in file.
        for (cmd in file.commandsInFile()) {
            if (cmd.name != "\\label" && cmd.name != "\\bibitem") {
                continue
            }

            val labelName = cmd.requiredParameter(0) ?: continue
            if (labelName in labels) {
                descriptors.add(manager.createProblemDescriptor(
                        cmd,
                        TextRange.from(cmd.commandToken.textLength + 1, labelName.length),
                        "Duplicate label '$labelName'",
                        ProblemHighlightType.GENERIC_ERROR,
                        isOntheFly
                ))
            }
        }

        return descriptors
    }
}