package nl.hannahsten.texifyidea.gutter

import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.reference.LatexColorReference
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped
import nl.hannahsten.texifyidea.util.parser.firstParentOfType
import nl.hannahsten.texifyidea.util.parser.getRequiredArgumentValueByName
import java.awt.Color
import java.util.*

/**
 * Provides colors in the gutter.
 *
 * @author Abby
 */
class LatexElementColorProvider : ElementColorProvider {

    /**
     * Set the color in the document based on changes in the color picker from the gutter.
     *
     * Only changes the color when we are in the gutter of a color definition. Do nothing when we are in the gutter
     * of a color usage.
     */
    override fun setColorTo(element: PsiElement, color: Color) {
        if (element is LeafPsiElement) {
            val command = element.firstParentOfType(LatexCommands::class) ?: return
            val semantics = LatexDefinitionService.resolveCommand(command) ?: return
            LatexPsiUtil.processArgumentsWithSemantics(command, semantics) process@{ param, arg ->
                arg ?: return@process
                if (arg.contextSignature.introduces(LatexContexts.ColorReference)) {
                    findDefinitionCommand(param)?.let {
                        // Change the color at the definition command
                        val definitionSemantics = LatexDefinitionService.resolveCommand(it) ?: return@let
                        setColorToDefinitionCommand(it, color, definitionSemantics, element)
                        return
                    }
                    // Try to set it to this command directly
                    setColorToDefinitionCommand(command, color, semantics, element)
                    return
                }
            }
        }
    }

    private fun setColorToDefinitionCommand(
        command: LatexCommands,
        color: Color,
        commandTemplate: LSemanticCommand,
        element: LeafPsiElement
    ) {
        val colorModel = command.getRequiredArgumentValueByName("model-list") ?: return
        val oldColor = command.getRequiredArgumentValueByName("spec-list") ?: return
        val newColorString = when (colorModel.lowercase(Locale.getDefault())) {
            "rgb" -> color.toRgbString(integer = oldColor.split(",").firstOrNull()?.contains('.') == false)
            "hsb" -> color.toHsbString()
            "html" -> color.toHtmlStsring()
            "gray" -> color.toGrayString()
            "cmyk" -> color.toCmykString()
            "cmy" -> color.toCmyString()
            else -> null
        } ?: return

        val colorArgumentIndex =
            commandTemplate.arguments.filter { it.isRequired }.indexOfFirst { it.name == "spec-list" }
        if (colorArgumentIndex == -1) return

        val newColorParameter = LatexPsiHelper(element.project).createRequiredParameter(newColorString)
        val oldColorParameter = command.requiredParameters()[colorArgumentIndex]
        oldColorParameter.parent.node.replaceChild(oldColorParameter.node, newColorParameter.node)
    }

    /**
     * Get the color that is used in a command that uses color. This color will be shown in the gutter.
     */
    override fun getColorFrom(element: PsiElement): Color? {
        if (element !is LeafPsiElement) return null
        if (element.elementType != LatexTypes.COMMAND_TOKEN) return null
        val command = element.firstParentOfType(LatexCommands::class) ?: return null
        val semantics = LatexDefinitionService.resolveCommand(command) ?: return null
        LatexPsiUtil.processArgumentsWithSemantics(command, semantics) process@{ param, arg ->
            arg ?: return@process
            if (arg.contextSignature.introduces(LatexContexts.ColorReference)) {
                val colorSourceCommand = findDefinitionCommand(param) ?: command
                return findColor(param.contentText(), element.containingFile, command = colorSourceCommand)
            }
        }
        return null
    }

    /**
     * If the color is user-defined, show that color instead of guessing based on the name
     */
    private fun findDefinitionCommand(param: LatexParameter): LatexCommands? {
        val parameterText = param.findFirstChildTyped<LatexParameterText>()?.references?.filterIsInstance<LatexColorReference>()?.firstOrNull()?.resolve()
        val source = parameterText?.firstParentOfType(LatexCommands::class).takeIf { it?.name in CommandMagic.colorReference.keys }
        return source
    }
}
