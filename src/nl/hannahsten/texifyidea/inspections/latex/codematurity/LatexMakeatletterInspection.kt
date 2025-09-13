package nl.hannahsten.texifyidea.inspections.latex.codematurity

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.AbstractTexifyCommandBasedInspection
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.nameWithoutSlash

/**
 * Best would be if we could detect when the usage of \makeatletter/\makeatother is unnecessary, but this is practically impossible.
 *
 * @author Hannah Schellekens
 */
class LatexMakeatletterInspection : AbstractTexifyCommandBasedInspection(
    inspectionId = "Makeatletter",
    excludedContexts = setOf(
        LatexContexts.Preamble
    )
) {

    override fun isAvailableForFile(file: PsiFile): Boolean {
        val type = file.virtualFile?.fileType ?: file.fileType
        return type == LatexFileType
    }

    override fun inspectCommand(command: LatexCommands, contexts: LContextSet, lookup: LatexSemanticsLookup, file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>) {
        val name = command.nameWithoutSlash ?: return
        if (name != "makeatletter" && name != "makeatother") return
        val descriptor = manager.createProblemDescriptor(
            command,
            "${command.name} should only be used when necessary",
            null as? com.intellij.codeInspection.LocalQuickFix?,
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            isOnTheFly
        )
        descriptors.add(descriptor)
        // we should not provide a quickfix, as the user needs to check if it is safe to remove these commands
    }
}