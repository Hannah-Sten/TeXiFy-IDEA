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
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * @author Ruben Schellekens
 */
open class LatexInclusionLoopInspection : TexifyInspectionBase() {

    override fun getInspectionGroup() = InsightGroup.LATEX

    override fun getInspectionId() = "InclusionLoop"

    override fun getDisplayName() = "Inclusion loops"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        // Run a BFS on all file inclusions to check for duplicate files.
        val root = file.findRootFile()
        val covered: MutableSet<PsiFile> = mutableSetOf(root)
        val duplicate: MutableSet<PsiFile> = HashSet()
        val bfs = BFS(root, PsiFile::findInclusions)
        bfs.setIterationAction {
            val inclusions = it.findInclusions()
            for (inclusion in inclusions) {
                if (covered.contains(inclusion)) {
                    duplicate.add(inclusion)
                }

                covered.add(inclusion)
            }
            BFS.BFSAction.CONTINUE
        }
        bfs.execute()

        // Look through all commands to see if they include duplicates.
        val commands = LatexCommandsIndex.getIndexedCommands(file)
        for (cmd in commands) {
            val name = cmd.name
            if ("\\input" != name && "\\include" != name && "\\includeonly" != name) {
                continue
            }

            val param = cmd.requiredParameter(0) ?: continue
            val targetFile = root.findRelativeFile(param)
            if (!duplicate.contains(targetFile)) {
                continue
            }

            descriptors.add(manager.createProblemDescriptor(
                    cmd,
                    TextRange(name.length + 1, cmd.textLength - 1),
                    "File inclusion loop found.",
                    ProblemHighlightType.GENERIC_ERROR,
                    isOntheFly
            ))
        }

        return descriptors
    }

}