package nl.hannahsten.texifyidea.gutter

import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.lang.commands.LatexXcolorCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.firstParentOfType
import nl.hannahsten.texifyidea.util.getRequiredArgumentValueByName
import nl.hannahsten.texifyidea.util.magic.ColorMagic
import nl.hannahsten.texifyidea.util.usesColor
import java.awt.Color

/**
 * Provides colors in the gutter.
 *
 * @author Abby
 */
object LatexElementColorProvider : ElementColorProvider {

    override fun setColorTo(element: PsiElement, color: Color) {}

    override fun getColorFrom(element: PsiElement): Color? {
        if (element is LeafPsiElement) {
            val command = element.firstParentOfType(LatexCommands::class)
            if (command.usesColor()) {
                val colorArgument = when (command?.name?.substring(1)) {
                    // Show the defined color.
                    in ColorMagic.colorDefinitions.map { it.command } -> {
                        command?.getRequiredArgumentValueByName("name")
                    }
                    // Show the used color.
                    in ColorMagic.takeColorCommands -> {
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

    fun findColor(colorName: String, file: PsiFile): Color? {
        val defaultHex = ColorMagic.defaultXcolors[colorName]

        return if (defaultHex != null) Color(defaultHex)
        else {
            val colorDefiningCommands = LatexCommandsIndex.getCommandsByNames(
                file,
                *ColorMagic.colorDefinitions.map { "\\${it.command}" }
                    .toTypedArray()
            )
            // If this color is a single color (not a mix, and thus does not contain a !)
            // and we did not find it in the default colors (above), it should be in the
            // first parameter of a color definition command. If not, we can not find the
            // color (and return null in the end).
            if (colorName.contains('!') || colorDefiningCommands.map { it.getRequiredArgumentValueByName("name") }.contains(colorName)) {

                val colorDefinitionCommand = colorDefiningCommands.find { it.getRequiredArgumentValueByName("name") == colorName }
                when (colorDefinitionCommand?.name?.substring(1)) {
                    LatexXcolorCommand.COLORLET.command -> {
                        getColorFromColorParameter(file, colorDefinitionCommand.getRequiredArgumentValueByName("color"))
                    }
                    LatexXcolorCommand.DEFINECOLOR.command, LatexXcolorCommand.PROVIDECOLOR.command -> {
                        getColorFromDefineColor(
                            colorDefinitionCommand.getRequiredArgumentValueByName("model-list"),
                            colorDefinitionCommand.getRequiredArgumentValueByName("spec-list")
                        )
                    }
                    else -> getColorFromColorParameter(file, colorName)
                }
            }
            else return null
        }
    }

    /**
     * Given the [color] parameter of a command, compute the defined color.
     */
    private fun getColorFromColorParameter(file: PsiFile, definitionText: String?): Color? {
        definitionText ?: return null
        val colorParts = definitionText.split("!").filter { it.isNotBlank() }
        val colors = colorParts.filter { it.all { c -> c.isLetter() } }
            .map { findColor(it, file) ?: return null }
        if (colors.isEmpty()) return null
        val numbers = colorParts.filter { it.all { c -> c.isDigit() } }
            .map { it.toInt() }
        var currentColor = colors.first()
        for ((i, color) in colors.withIndex()) {
            if (i > 0 && i - 1 in numbers.indices) currentColor = mix(currentColor, color, numbers[i - 1])
        }
        return currentColor
    }

    /**
     * Given the `model-list` and `spec-list` arguments of the \definecolor command,
     * get the corresponding color.
     */
    private fun getColorFromDefineColor(modelText: String?, specText: String?): Color? {
        modelText ?: return null
        specText ?: return null
        return try {
            when (modelText.toLowerCase()) {
                "rgb" -> fromRgbString(specText)
                "hsb" -> fromHsbString(specText)
                "cmy" -> fromCmyString(specText)
                "cmyk" -> fromCmykString(specText)
                "gray" -> fromGrayString(specText)
                "html" -> fromHtmlString(specText)
                else -> null
            }
        }
        // Exception occurs after typing a comma in an argument, as then we'd
        // try to format an empty string as a number.
        catch (e: NumberFormatException) {
            null
        }
        // Exception occurs when not enough color arguments have been typed.
        // E.g. we need three arguments (r, g, b) and have typed "255, 127".
        catch (e: IndexOutOfBoundsException) {
            null
        }
    }

    /*
     * Methods to parse the argument strings that define colors. Formula's taken
     * from the xcolor documentation.
     */

    /**
     * Mix two colors, used to support red!50!yellow color definitions.
     */
    private fun mix(a: Color, b: Color, percent: Int): Color {
        return (percent / 100.0).let {
            Color(
                (a.red * it + b.red * (1.0 - it)).toInt(),
                (a.green * it + b.green * (1.0 - it)).toInt(),
                (a.blue * it + b.blue * (1.0 - it)).toInt()
            )
        }
    }

    private fun fromRgbString(rgbText: String): Color {
        val rgb = rgbText.split(",").map { it.trim() }
        return try {
            rgb.map { it.toInt() }.let { Color(it[0], it[1], it[2]) }
        }
        catch (e: NumberFormatException) {
            rgb.map { it.toFloat() }.let { Color(it[0], it[1], it[2]) }
        }
    }

    private fun fromHsbString(hsbText: String): Color {
        val hsb = hsbText.split(",").map { it.trim() }
        return hsb.map { it.toFloat() }
            .let { Color.getHSBColor(it[0], it[1], it[2]) }
    }

    private fun fromCmykString(cmykText: String): Color {
        val cmyk = cmykText.split(",").map { it.trim() }
            .map { it.toFloat() }
        return cmyk.take(3)
            .map { (255 * (1 - cmyk.last()) * (1 - it)).toInt() }
            .let { Color(it[0], it[1], it[2]) }
    }

    private fun fromCmyString(cmyText: String): Color {
        val cmy = cmyText.split(",").map { it.trim() }.map { it.toFloat() }
        return Color(1 - cmy[0], 1 - cmy[1], 1 - cmy[2])
    }

    private fun fromGrayString(grayText: String): Color {
        fun Float.toRgb() = (this * 255).toInt()
        val gray = grayText.toFloat()
        return Color(gray.toRgb(), gray.toRgb(), gray.toRgb())
    }

    private fun fromHtmlString(htmlText: String): Color {
        return Color.decode("#$htmlText")
    }
}