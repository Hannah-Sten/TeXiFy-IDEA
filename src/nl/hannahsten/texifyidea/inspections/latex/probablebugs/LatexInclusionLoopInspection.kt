package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexIncludesIndex
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.util.files.findIncludedFile

/**
 * This inspection only detects inclusion loops involving two files.
 *
 * If ever it is extended to more files, great care should be taken for a good performance.
 */
open class LatexInclusionLoopInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "InclusionLoop"

    override fun getDisplayName() = "Inclusion loops"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        val allIncludeCommands = LatexIncludesIndex.Util.getItems(file.project, useCache = false)

        // Maps every file to all the files it includes.
        val inclusions: MutableMap<PsiFile, MutableSet<PsiFile>> = HashMap()

        // Find all related files.
        for (command in allIncludeCommands) {
            // Find included files
            val declaredIn = command.containingFile

            // Do not use ImportPackage#searchFileByImportPaths, because if we would do that for every command, that would be way too expensive.
            for (referenced in declaredIn.findIncludedFile(command)) {
                inclusions.getOrPut(declaredIn) { mutableSetOf() }.add(referenced)

                if (declaredIn == file && inclusions.getOrDefault(referenced, mutableSetOf()).contains(declaredIn)) {
                    descriptors.add(
                        manager.createProblemDescriptor(
                            command,
                            TextRange(0, command.textLength - 1),
                            "File inclusion loop found for files ${referenced.name} and ${declaredIn.name}.",
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            isOntheFly
                        )
                    )
                }
            }
        }

        return descriptors
    }
}
