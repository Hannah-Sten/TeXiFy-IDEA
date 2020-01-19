package nl.hannahsten.texifyidea.insight

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.parameterInfo.*
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ArrayUtil
import nl.hannahsten.texifyidea.lang.LatexRegularCommand
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * @author Sten Wessel
 */
class LatexParameterInfoHandler : ParameterInfoHandler<LatexCommands, LatexRegularCommand> {

    private fun findLatexCommand(file: PsiFile, offset: Int): LatexCommands? {
        val element = file.findElementAt(offset)
        return PsiTreeUtil.getParentOfType(element, LatexCommands::class.java)
    }

    override fun couldShowInLookup() = true

    override fun getParametersForLookup(item: LookupElement, context: ParameterInfoContext): Array<Any>? {
        return ArrayUtil.EMPTY_OBJECT_ARRAY
    }

    override fun findElementForParameterInfo(context: CreateParameterInfoContext): LatexCommands? {
        return findLatexCommand(context.file, context.offset)
    }

    override fun showParameterInfo(element: LatexCommands, context: CreateParameterInfoContext) {
        val commandHuh = LatexRegularCommand[element.commandToken.text.substring(1)] ?: return

        context.itemsToShow = arrayOf<Any>(commandHuh)
        context.showHint(element, element.textOffset, this)
    }

    override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): LatexCommands? {
        return findLatexCommand(context.file, context.offset)
    }

    override fun updateParameterInfo(element: LatexCommands, context: UpdateParameterInfoContext) {
        context.setCurrentParameter(0)
    }

    override fun updateUI(cmd: LatexRegularCommand?, context: ParameterInfoUIContext) {
        if (cmd == null) {
            context.isUIComponentEnabled = false
            return
        }

        val index = context.currentParameterIndex
        val arguments = cmd.arguments

        if (index >= arguments.size) {
            context.isUIComponentEnabled = false
            return
        }

        context.setupUIComponentPresentation(cmd.commandDisplay + cmd.getArgumentsDisplay(), 0, 0, false, false, true, context.defaultParameterColor)
    }
}
