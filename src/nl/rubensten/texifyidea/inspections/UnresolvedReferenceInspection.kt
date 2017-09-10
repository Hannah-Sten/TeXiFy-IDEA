package nl.rubensten.texifyidea.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.util.TexifyUtil
import nl.rubensten.texifyidea.util.commandsInFileSet
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * @author Ruben Schellekens
 */
open class UnresolvedReferenceInspection : TexifyInspectionBase() {

    companion object {
        val NO_FIX: LocalQuickFix? = null
    }

    override fun getDisplayName(): String {
        return "Unresolved reference"
    }

    override fun getShortName(): String {
        return "UnresolvedReference"
    }

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        val labels = TexifyUtil.findLabelsInFileSet(file)
        val commands = file.commandsInFileSet()
        for (cmd in commands) {
            if (!NonBreakingSpaceInspection.REFERENCE_COMMANDS.contains(cmd.name)) {
                continue
            }

            val required = cmd.requiredParameters
            if (required.isEmpty()) {
                continue
            }

            if (!labels.contains(required[0])) {
                descriptors.add(manager.createProblemDescriptor(
                        cmd,
                        "Unresolved reference",
                        NO_FIX,
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly
                ))
            }
        }

        return descriptors
    }
}