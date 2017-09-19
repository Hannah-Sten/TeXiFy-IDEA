package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.commandsInFile
import java.util.*
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * @author Ruben Schellekens
 */
open class LatexNonMatchingIfInspection : TexifyInspectionBase() {

    companion object {

        val NO_FIX: LocalQuickFix? = null

        val IFS = setOf(
                "\\if", "\\ifcat", "\\ifnum", "\\ifdim", "\\ifodd", "\\ifvmode", "\\ifhmode", "\\ifmmode",
                "\\ifinner", "\\ifvoid", "\\ifhbox", "\\ifvbox", "\\ifx", "\\ifeof", "\\iftrue", "\\iffalse",
                "\\ifcase", "\\ifdefined", "\\ifcsname", "\\iffontchar", "\\ifincsname", "\\ifpdfprimitive",
                "\\ifpdfabsnum", "\\ifpdfabsdim", "\\ifpdfprimitive", "\\ifprimitive", "\\ifabsum", "\\ifabsdim"
        )
        val END_IF = "\\fi"
    }

    override fun getInspectionGroup() = InsightGroup.LATEX

    override fun getDisplayName() = "Open if-then-else control sequence"

    override fun getInspectionId() = "NonMatchingIf"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        // Find matches.
        val stack = ArrayDeque<LatexCommands>()
        val commands = file.commandsInFile()
        for (cmd in commands) {
            val name = cmd.name
            if (IFS.contains(name)) {
                stack.push(cmd)
            }
            else if (END_IF == cmd.name) {
                // Non-opened fi.
                if (stack.isEmpty()) {
                    descriptors.add(manager.createProblemDescriptor(
                            cmd,
                            "No matching \\if-command found",
                            NO_FIX,
                            ProblemHighlightType.GENERIC_ERROR,
                            isOntheFly
                    ))
                    continue
                }

                stack.pop()
            }
        }

        // Mark unclosed ifs.
        for (cmd in stack) {
            descriptors.add(manager.createProblemDescriptor(
                    cmd,
                    "If statement is not closed",
                    NO_FIX,
                    ProblemHighlightType.GENERIC_ERROR,
                    isOntheFly
            ))
        }

        return descriptors
    }
}