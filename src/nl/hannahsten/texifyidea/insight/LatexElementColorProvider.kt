package nl.hannahsten.texifyidea.insight

import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.lang.LatexRegularCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.firstParentOfType
import nl.hannahsten.texifyidea.util.getRequiredArgumentValueByName
import nl.hannahsten.texifyidea.util.usesColor
import java.awt.Color

class LatexElementColorProvider : ElementColorProvider {
    override fun setColorTo(element: PsiElement, color: Color) {}

    override fun getColorFrom(element: PsiElement): Color? {
        if (element is LeafPsiElement) {
            val command = element.firstParentOfType(LatexCommands::class)
            if (command.usesColor()) {
                val colorArgument = when(command?.name?.substring(1)) {
                    // Show the defined color.
                    in Magic.Colors.colorDefinitions.map { it.command } -> {
                        command?.getRequiredArgumentValueByName("name")
                    }
                    // Show the used color.
                    in Magic.Colors.takeColorCommands -> {
                        command?.getRequiredArgumentValueByName("color")
                    }
                    else -> null
                } ?: return null
                // Find the color to show.
                if (command?.name == element.text) {
                    return findColor(colorArgument, element.containingFile)
                }
            }
        }
        return null
    }

    private fun findColor(colorName: String, file: PsiFile): Color? {
        val defaultHex = Magic.Colors.defaultXcolors[colorName]

        return if (defaultHex != null) Color(defaultHex)
        else {
            val colorDefiningCommands = LatexCommandsIndex.getCommandsByNames(file, *Magic.Colors.colorDefinitions.map { "\\${it.command}" }.toTypedArray())
            val colorDefinitionCommand = colorDefiningCommands.find { it.getRequiredArgumentValueByName("name") == colorName }
            when (colorDefinitionCommand?.name?.substring(1)) {
                LatexRegularCommand.COLORLET.command -> {
                    getColorFromColorParameter(file, colorDefinitionCommand.getRequiredArgumentValueByName("color"))
                }
                LatexRegularCommand.DEFINECOLOR.command -> {
                    getColorFromDefineColor(
                            file,
                            colorDefinitionCommand.getRequiredArgumentValueByName("model-list"),
                            colorDefinitionCommand.getRequiredArgumentValueByName("spec-list")
                    )
                }
                else -> getColorFromColorParameter(file, colorName)
            }
        }
    }

    /**
     * Given the [color] parameter of a command, compute the defined color.
     */
    private fun getColorFromColorParameter(file: PsiFile, definitionText: String?): Color? {
        definitionText ?: return null
        val colorParts = definitionText.split("!").filter { it.isNotBlank() }
        val colors = colorParts.filter { it.all { c -> c.isLetter() } }.map { findColor(it, file) }
        val numbers = colorParts.filter { it.all { c -> c.isDigit() } }.map { it.toInt() }
        var currentColor: Color? = null
        for ((i, color) in colors.withIndex()) {
            if (i == 0) currentColor = color
            else if (i - 1 in numbers.indices) currentColor = mix(currentColor!!, color!!, numbers[i - 1])
        }
        return currentColor
    }

    /**
     * Given the
     */
    private fun getColorFromDefineColor(file: PsiFile, modelText: String?, specText: String?): Color? {
        modelText ?: return null
        specText ?: return null
        return Color.GREEN
    }

    private fun mix(a: Color, b: Color, percent: Int): Color? {
        return (percent/100.0).let {
            Color((a.red * it + b.red * (1.0 - it)).toInt(),
                    (a.green * it + b.green * (1.0 - it)).toInt(),
                    (a.blue * it + b.blue * (1.0 - it)).toInt())
        }
    }
}