package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexIncludesIndex
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.files.findFile
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.replaceString
import nl.hannahsten.texifyidea.util.parser.requiredParameter
import java.util.*

/**
 * @author Sten Wessel
 */
open class LatexNestedIncludesInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "NestedIncludes"

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "Nested includes"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()
        val root = file.findRootFile()

        val isInclude = LatexIncludesIndex.getItemsInFileSet(file).any {
            it.name == "\\include" && it.requiredParameter(0)?.let { f -> root.findFile(f) } == file
        }

        if (!isInclude) {
            return descriptors
        }

        file.commandsInFile().asSequence()
            .filter { it.name == "\\include" }
            .forEach {
                descriptors.add(
                    manager.createProblemDescriptor(
                        it,
                        TextRange.allOf(it.text),
                        "Includes cannot be nested",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly,
                        ConvertToInputFix
                    )
                )
            }

        return descriptors
    }

    /**
     * @author Sten Wessel
     */
    object ConvertToInputFix : LocalQuickFix {

        override fun getFamilyName() = "Convert to \\input"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val document = command.containingFile.document() ?: return

            val fileName = command.requiredParameter(0) ?: return

            document.replaceString(command.textRange, "\\input{$fileName}")
        }
    }
}