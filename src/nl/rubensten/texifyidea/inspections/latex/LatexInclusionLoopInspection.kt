package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.algorithm.BFS
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.util.findInclusions
import nl.rubensten.texifyidea.util.findRelativeFile
import nl.rubensten.texifyidea.util.findRootFile
import nl.rubensten.texifyidea.util.requiredParameter

/**
 * @author Ruben Schellekens
 */
open class LatexInclusionLoopInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "InclusionLoop"

    override fun getDisplayName() = "Inclusion loops"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        // Run a BFS on all file inclusions to check for duplicate files.
        val root = file.findRootFile()
        val covered: MutableSet<PsiFile> = mutableSetOf(root)
        val duplicate: MutableSet<PsiFile> = HashSet()
        val bfs = BFS(root, PsiFile::findInclusions)
        bfs.setIterationAction {
            // Set that contains all the inclusions in the current file.
            // This is used to disregard files that have been included multiple times in the same file.
            val inThisFile = HashSet<PsiFile>()
            val inclusions = it.findInclusions()

            for (inclusion in inclusions) {
                if (covered.contains(inclusion) && inclusion !in inThisFile) {
                    duplicate.add(inclusion)
                }

                covered.add(inclusion)
                inThisFile.add(inclusion)
            }
            BFS.BFSAction.CONTINUE
        }
        bfs.execute()

        // Look through all commands to see if they include duplicates.
        val commands = LatexCommandsIndex.getItems(file)
        for (command in commands) {
            val name = command.name
            if ("\\input" != name && "\\include" != name && "\\includeonly" != name) {
                continue
            }

            val param = command.requiredParameter(0) ?: continue
            val targetFile = root.findRelativeFile(param)
            if (!duplicate.contains(targetFile)) {
                continue
            }

            descriptors.add(manager.createProblemDescriptor(
                    command,
                    TextRange(name.length + 1, command.textLength - 1),
                    "File inclusion loop found.",
                    ProblemHighlightType.GENERIC_ERROR,
                    isOntheFly
            ))
        }

        return descriptors
    }
}