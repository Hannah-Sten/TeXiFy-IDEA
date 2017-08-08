package nl.rubensten.texifyidea.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * @author Ruben Schellekens
 */
class MissingDocumentclassInspection : TexifyInspectionBase() {

    override fun getDisplayName(): String {
        return "Missing documentclass"
    }

    override fun getShortName(): String {
        return "MissingDocumentclass"
    }

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        val commands = LatexCommandsIndex.getIndexCommandsInFileSet(file)
        val hasDocumentclass = commands.stream()
                .filter { cmd -> cmd.name == "\\documentclass" }
                .count() > 0

        if (!hasDocumentclass) {
            descriptors.add(
                    manager.createProblemDescriptor(
                            file,
                            "Document doesn't contain a \\documentclass command.",
                            NoQuickFix.INSTANCE,
                            ProblemHighlightType.ERROR,
                            isOntheFly
                    )
            )
        }

        return descriptors
    }
}
