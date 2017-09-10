package nl.rubensten.texifyidea.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.util.commandsInFileSet
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * @author Ruben Schellekens
 */
open class DuplicateLabelInspection : TexifyInspectionBase() {

    companion object {
        val NO_FIX: LocalQuickFix? = null
    }

    override fun getDisplayName(): String {
        return "Duplicate labels"
    }

    override fun getShortName(): String {
        return "DuplicateLabel"
    }

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        val labels: MutableSet<String> = HashSet()
        val commands = file.commandsInFileSet()
        for (cmd in commands) {
            if (cmd.name != "\\label") {
                continue
            }

            val required = cmd.requiredParameters
            if (required.isEmpty()) {
                continue
            }

            if (labels.contains(required[0])) {
                descriptors.add(manager.createProblemDescriptor(
                        cmd,
                        "Duplicate label",
                        NO_FIX,
                        ProblemHighlightType.ERROR,
                        isOntheFly
                ))
            }
            else {
                labels.add(required[0])
            }
        }

        return descriptors
    }
}