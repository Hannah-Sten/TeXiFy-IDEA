package nl.hannahsten.texifyidea.inspections.latex.codematurity

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.siblings
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexGroup
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import org.jetbrains.annotations.Nls

/**
 * @author Hannah Schellekens
 */
class LatexPrimitiveStyleInspection : TexifyInspectionBase() {

    override val inspectionGroup: InsightGroup
        get() = InsightGroup.LATEX

    @Nls
    override fun getDisplayName(): String {
        return "Discouraged use of TeX styling primitives"
    }

    override val inspectionId: String
        get() = "PrimitiveStyle"

    override fun inspectFile(
        file: PsiFile,
        manager: InspectionManager,
        isOntheFly: Boolean
    ): List<ProblemDescriptor> {
        val descriptors = mutableListOf<ProblemDescriptor>()
        val commands = LatexCommandsIndex.getItems(file)
        for (command in commands) {
            val index = CommandMagic.stylePrimitives.indexOf(command.name)
            if (index < 0) {
                continue
            }
            descriptors.add(
                manager.createProblemDescriptor(
                    command,
                    "Use of TeX primitive " + CommandMagic.stylePrimitives[index] + " is discouraged",
                    InspectionFix(SmartPointerManager.createPointer(command)),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly
                )
            )
        }
        return descriptors
    }

    private inner class InspectionFix(val oldCommand: SmartPsiElementPointer<LatexCommands>) : LocalQuickFixAndIntentionActionOnPsiElement(oldCommand.element) {

        @Nls
        override fun getFamilyName(): String {
            return "Convert to LaTeX alternative"
        }

        override fun getText(): String {
            return oldCommand.element
                ?.let { "Convert ${it.name} to ${CommandMagic.stylePrimitiveReplacements[it.name]}" }
                ?: familyName
        }

        override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
            (startElement as? LatexCommands)?.let { command ->
                command.setName(CommandMagic.stylePrimitiveReplacements[command.name] ?: return)

                // Convert {\bf this is content} to \textbf{this is content}
                if (command.requiredParameters.isEmpty()) {
                    val commandInFile = file.findElementAt(oldCommand.range?.startOffset ?: return) ?: return

                    val previousTrace = mutableListOf(commandInFile)
                    while (previousTrace.last().parent != null) {
                        val parent = previousTrace.last().parent
                        previousTrace.add(parent)
                        if (parent is LatexGroup) break
                    }

                    val commandInContent = previousTrace.lastOrNull { it.text == commandInFile.text }
                    val prefixContent = commandInContent?.siblings(forward = false, withSelf = false) ?: emptySequence()
                    val requiredParamContent = commandInContent?.siblings(forward = true, withSelf = false) ?: emptySequence()
                    val parentGroup = previousTrace.last()

                    val helper = LatexPsiHelper(project)
                    if (prefixContent.any()) {
                        val prefixPsi = helper.createFromText(prefixContent.toList().reversed().joinToString("") { it.text })
                        commandInFile.parent.addBefore(prefixPsi, commandInFile)
                    }
                    val requiredParam = helper.createRequiredParameter(requiredParamContent.joinToString("") { it.text }.trim())
                    commandInFile.parent.add(requiredParam)

                    val newPsi = commandInFile.parent.copy()
                    parentGroup.replace(newPsi)
                }
            }
        }
    }
}