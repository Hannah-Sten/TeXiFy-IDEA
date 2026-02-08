package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.includedPackagesInFileset
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.GeneralMagic
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.matches
import nl.hannahsten.texifyidea.util.parser.collectSubtreeTyped
import nl.hannahsten.texifyidea.util.parser.previousCommand
import java.util.*

/**
 * @author Hannah Schellekens
 */
open class LatexNonMatchingIfInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override fun getDisplayName() = "Open if-then-else control sequence"

    override val inspectionId = "NonMatchingIf"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        // etoolbox has many \if... commands without a \fi
        if (file.includedPackagesInFileset().contains(LatexPackage.ETOOLBOX)) {
            return emptyList()
        }

        // Find matches.
        val stack = ArrayDeque<PsiElement>()
        val commands = file.commandsInFile()
        val ifs = file.collectSubtreeTyped<LeafPsiElement> { it.elementType == LatexTypes.END_IF || it.elementType == LatexTypes.START_IF }
        val all = (commands + ifs).sortedBy { it.textOffset }
        for (command in all) {
            val name = if (command is LatexCommands) command.name else command.text
            if (command is LeafPsiElement && command.elementType == LatexTypes.END_IF) {
                // Non-opened fi.
                if (stack.isEmpty()) {
                    descriptors.add(
                        manager.createProblemDescriptor(
                            command,
                            "No matching \\if-command found",
                            GeneralMagic.noQuickFix,
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            isOntheFly
                        )
                    )
                    continue
                }

                stack.pop()
            }
            else if (PatternMagic.ifCommand.matches(name) && name !in CommandMagic.ignoredIfs && command.previousCommand()?.name != "\\newif") {
                stack.push(command)
            }
        }

        // Mark unclosed ifs.
        for (cmd in stack) {
            descriptors.add(
                manager.createProblemDescriptor(
                    cmd,
                    "If statement should probably be closed with \\fi",
                    GeneralMagic.noQuickFix,
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly
                )
            )
        }

        return descriptors
    }
}