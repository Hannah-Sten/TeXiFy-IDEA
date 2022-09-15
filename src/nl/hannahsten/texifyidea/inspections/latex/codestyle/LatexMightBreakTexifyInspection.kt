package nl.hannahsten.texifyidea.inspections.latex.codestyle

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.commands.LatexNewDefinitionCommand
import nl.hannahsten.texifyidea.util.forcedFirstRequiredParameterAsCommand
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.cmd
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
        val commands = LatexCommandsIndex.getItems(file)
        for (command in commands) {
            // Error when \newcommand is used on existing command
            if (CommandMagic.commandRedefinitions.contains(command.name)) {
                val newCommand = command.forcedFirstRequiredParameterAsCommand()
                if (CommandMagic.fragile.contains(newCommand?.name) || command.name == LatexNewDefinitionCommand.CATCODE.cmd) {
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