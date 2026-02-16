package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.projectstructure.pathOrNull
import nl.hannahsten.texifyidea.inspections.AbstractTexifyCommandBasedInspection
import nl.hannahsten.texifyidea.inspections.createDescriptor
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContextIntro
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.SimpleFileInputContext
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.psi.contentText
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import nl.hannahsten.texifyidea.util.parser.lookupCommandPsi

/**
 * @author Thomas
 */
class LatexAbsolutePathInspection : AbstractTexifyCommandBasedInspection(
    inspectionId = "AbsolutePath",
    skipChildrenInContext = setOf(LatexContexts.Comment, LatexContexts.InsideDefinition)
) {

    private fun checkAbsolutePath(
        command: LatexCommands,
        parameter: LatexParameter, fileArg: SimpleFileInputContext,
        manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>
    ) {
        if (fileArg.isAbsolutePathSupported) return
        val fileName = parameter.contentText()
        val path = pathOrNull(fileName) ?: return
        if (!path.isAbsolute) return
        val range = TextRange.from(1, fileName.length)
        descriptors.add(
            manager.createDescriptor(
                parameter,
                "No absolute path allowed here",
                isOnTheFly = isOnTheFly,
                highlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                rangeInElement = range,
            )
        )
    }

    override fun inspectCommand(command: LatexCommands, contexts: LContextSet, defBundle: DefinitionBundle, file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>) {
        val semantics = defBundle.lookupCommandPsi(command) ?: return
        LatexPsiUtil.processArgumentsWithSemantics(command, semantics) arg@{ param, arg ->
            if (arg == null) return
            val assign = arg.contextSignature as? LatexContextIntro.Assign ?: return@arg
            val fileArg = assign.contexts.firstNotNullOfOrNull { it as? SimpleFileInputContext } ?: return@arg
            checkAbsolutePath(command, param, fileArg, manager, isOnTheFly, descriptors)
        }
    }
}
