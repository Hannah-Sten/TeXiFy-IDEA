package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.bibtexIdsInFileSet
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import java.util.*

/**
 * @author Hannah Schellekens, Sten Wessel
 */
open class LatexDuplicateLabelInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId: String = "DuplicateLabel"

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "Duplicate labels"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        // Fill up a set of labels.
        val labels = mutableMapOf<String, MutableSet<String>>()
        val firstPass = mutableMapOf<String, MutableSet<String>>()
        for (command in file.commandsInFileSet()) {
            val labelName = command.requiredParameter(0) ?: continue

            if (command.name != "\\label" && command.name != "\\bibitem") {
                continue
            }

            if (firstPass[command.name!!]?.let { labelName in it } == true) {
                labels.getOrPut(command.name!!) { mutableSetOf() }.add(labelName)
                continue
            }
            firstPass.getOrPut(command.name!!) { mutableSetOf() }.add(labelName)
        }

        for (id in file.bibtexIdsInFileSet()) {
            val labelName = id.idName()

            if (firstPass["\\bibitem"]?.let { labelName in it } == true) {
                labels.getOrPut("\\bibitem") { mutableSetOf() }.add(labelName)
                continue
            }
            firstPass.getOrPut("\\bibitem") { mutableSetOf() }.add(labelName)
        }

        // Check labels in file.
        for (cmd in file.commandsInFile()) {
            if (cmd.name != "\\label" && cmd.name != "\\bibitem") {
                continue
            }

            val labelName = cmd.requiredParameter(0) ?: continue
            if (labelName in labels[cmd.name!!] ?: continue) {
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
