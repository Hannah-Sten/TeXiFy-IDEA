package nl.hannahsten.texifyidea.documentation

import com.intellij.lang.parameterInfo.CreateParameterInfoContext
import com.intellij.lang.parameterInfo.ParameterInfoHandler
import com.intellij.lang.parameterInfo.ParameterInfoUIContext
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * This class appears to have something to do with parameter info, but no idea what it does.
 *
 * @author Sten Wessel
 */
class LatexParameterInfoHandler : ParameterInfoHandler<LatexCommands, LSemanticCommand> {

    private fun findLatexCommand(file: PsiFile, offset: Int): LatexCommands? {
        val element = file.findElementAt(offset)
        return PsiTreeUtil.getParentOfType(element, LatexCommands::class.java)
    }

    override fun findElementForParameterInfo(context: CreateParameterInfoContext): LatexCommands? {
        return findLatexCommand(context.file, context.offset)
    }

    override fun showParameterInfo(element: LatexCommands, context: CreateParameterInfoContext) {
        val semantics = LatexDefinitionService.resolveCommand(element) ?: return
        context.itemsToShow = arrayOf(semantics)
        context.showHint(element, element.textOffset, this)
    }

    override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): LatexCommands? {
        return findLatexCommand(context.file, context.offset)
    }

    override fun updateParameterInfo(element: LatexCommands, context: UpdateParameterInfoContext) {
        context.setCurrentParameter(0)
    }

    override fun updateUI(p: LSemanticCommand?, context: ParameterInfoUIContext) {
        if (p == null) {
            context.isUIComponentEnabled = false
            return
        }
        val idx = context.currentParameterIndex
        val arguments = p.arguments
        if (idx >= arguments.size) {
            context.isUIComponentEnabled = false
            return
        }
        context.setupUIComponentPresentation(p.nameWithSlash + p.arguments.joinToString(""), 0, 0, false, false, true, context.defaultParameterColor)
    }
}