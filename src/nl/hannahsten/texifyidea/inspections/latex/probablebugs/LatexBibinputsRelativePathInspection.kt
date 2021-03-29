package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase

/**
 * BIBINPUTS cannot handle paths which contain /../ which can happen if you have a BIBINPUTS variable in the run config
 * but you have \bibliography{../mybib}.
 * Solution: set the BIBINPUTS path to the parent and use \bibliography{mybib} instead.
 * See https://tex.stackexchange.com/questions/406024/relative-paths-with-bibinputs
 */
class LatexBibinputsRelativePathInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "BibinputsRelativePath"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        TODO("Not yet implemented")
    }
}