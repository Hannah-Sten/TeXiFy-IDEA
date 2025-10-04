package nl.hannahsten.texifyidea.inspections.latex.codestyle

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.predefined.CommandNames
import nl.hannahsten.texifyidea.psi.forEachCommand
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.forcedFirstRequiredParameterAsCommand
import org.jetbrains.annotations.Nls

/**
 * @author Sten Wessel
 */
class LatexMightBreakTexifyInspection : TexifyInspectionBase() {

    override val inspectionGroup: InsightGroup
        get() = InsightGroup.LATEX

    @Nls
    override fun getDisplayName(): String {
        return "Might break TeXiFy functionality"
    }

    override val inspectionId: String
        get() = "MightBreakTexify"

    override fun inspectFile(
        file: PsiFile,
        manager: InspectionManager,
        isOntheFly: Boolean
    ): List<ProblemDescriptor> {
        val descriptors = descriptorList()
        file.forEachCommand { command ->
            // Error when \newcommand is used on existing command
            if (CommandMagic.commandRedefinitions.contains(command.name)) {
                val newCommand = command.forcedFirstRequiredParameterAsCommand()
                if (CommandMagic.fragile.contains(newCommand?.name) || command.name == CommandNames.CAT_CODE) {
                    descriptors.add(
                        manager.createProblemDescriptor(
                            command,
                            "Redefining ${newCommand?.name ?: "this command"} might break TeXiFy functionality",
                            null as LocalQuickFix?,
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