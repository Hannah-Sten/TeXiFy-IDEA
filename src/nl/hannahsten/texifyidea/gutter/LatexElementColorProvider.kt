package nl.hannahsten.texifyidea.gutter

import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.lang.commands.LatexColorDefinitionCommand
import nl.hannahsten.texifyidea.lang.commands.RequiredArgument
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.magic.ColorMagic
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

/**
 * Provides colors in the gutter.
 *
 * @author Abby
 */
object LatexElementColorProvider : ElementColorProvider {

    /**
     * Set the color in the document based on changes in the color picker from the gutter.
     *
     * Only changes the color when we are in the gutter of a color definition. Do nothing when we are in the gutter
     * of a color usage.
     */
    override fun setColorTo(element: PsiElement, color: Color) {
        if (element is LeafPsiElement) {
            val command = element.firstParentOfType(LatexCommands::class)
            val commandTemplate = LatexColorDefinitionCommand.values().firstOrNull {
                it.commandWithSlash == command?.name
            } ?: return
            val colorModel = command?.getRequiredArgumentValueByName("model-list") ?: return
            val oldColor = command.getRequiredArgumentValueByName("spec-list") ?: return
            val newColorString = when (colorModel.toLowerCase()) {
                "rgb" -> color.toRgbString(integer = oldColor.split(",").firstOrNull()?.contains('.') == false)
                "hsb" -> color.toHsbString()
                "html" -> color.toHtmlStsring()
                "gray" -> color.toGrayString()
                "cmyk" -> color.toCmykString()
                "cmy" -> color.toCmyString()
                else -> null
            } ?: return

            val colorArgumentIndex =
                commandTemplate.arguments.filterIsInstance<RequiredArgument>().indexOfFirst { it.name == "spec-list" }
            if (colorArgumentIndex == -1) return

            val newColorParameter = LatexPsiHelper(element.project).createRequiredParameter(newColorString)
            val oldColorParameter = command.requiredParameters()[colorArgumentIndex]
            oldColorParameter.parent.node.replaceChild(oldColorParameter.node, newColorParameter.node)
        }
    }

    /**
     * Get the color that is used in a command that uses color. This color will be shown in the gutter.
     */
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
            if (colorName.contains('!') || colorDefiningCommands.map { it.getRequiredArgumentValueByName("name") }
                    .contains(colorName)) {

                val colorDefinitionCommand =
                    colorDefiningCommands.find { it.getRequiredArgumentValueByName("name") == colorName }
                when (colorDefinitionCommand?.name?.substring(1)) {
                    LatexColorDefinitionCommand.COLORLET.command -> {
                        getColorFromColorParameter(file, colorDefinitionCommand.getRequiredArgumentValueByName("color"))
                    }
                    LatexColorDefinitionCommand.DEFINECOLOR.command, LatexColorDefinitionCommand.PROVIDECOLOR.command -> {
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
     * Given the color parameter [definitionText] of a command, compute the defined color.
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

    /**
     * Get the [Color] from an RGB string, where the RGB values are either
     * - integers in the range [0, 255],
     * - or floats in the range [0, 1].
     */
    private fun fromRgbString(rgbText: String): Color {
        val rgb = rgbText.split(",").map { it.trim() }
        return try {
            rgb.map { it.toInt().projectOnto(0..255) }.let { Color(it[0], it[1], it[2]) }
        }
        catch (e: NumberFormatException) {
            rgb.map { it.toFloat().projectOnto(0..1) }.let { Color(it[0], it[1], it[2]) }
        }
    }

    /**
     * Convert a [Color] object to an RGB string "R, G, B" with R, G, and B integers in the range [0, 255] if [integer]
     * is true, and R, G, and B floats in the range [0, 1] otherwise.
     */
    private fun Color.toRgbString(integer: Boolean = true): String =
        if (integer) "$red, $green, $blue"
        else listOf(red, green, blue).map { it / 255.0 }.joinToString(", ") { it.format() }

    /**
     * Get the [Color] from an HSB string, assuming that the values are in the range [0, 1].
     */
    private fun fromHsbString(hsbText: String): Color {
        val hsb = hsbText.split(",").map { it.trim() }
        return hsb.map { it.toFloat().projectOnto(0..1) }
            .let { Color.getHSBColor(
                it[0], it[1], it[2]) }
    }

    /**
     * Convert a color to an HSB string "hue, saturation, brightness" where each value is a float in the range [0, 1].
     */
    private fun Color.toHsbString(): String = Color.RGBtoHSB(red, green, blue, null)
        .joinToString(", ") { it.toDouble().format() }

    /**
     * Get a [Color] object from a cmyk (cyan, magenta, blue, black) string.
     */
    private fun fromCmykString(cmykText: String): Color {
        val cmyk = cmykText.split(",").map { it.trim() }
            .map { it.toFloat() }
        return cmyk.take(3)
            .map { (255 * (1 - cmyk.last()) * (1 - it)).toInt() }
            .map { it.projectOnto(0..255) }
            .let { Color(it[0], it[1], it[2]) }
    }

    /**
     * Convert a [Color] object to a cmyk string.
     */
    private fun Color.toCmykString(): String? {
        val rgb = listOf(red, green, blue).map { it / 255.0 }
        val k: Double = 1.0 - (rgb.maxOrNull() ?: return null)
        return rgb.map { (1.0 - it - k) / (1.0 - k) }.joinToString(", ") { it.format() } + ", $k"
    }

    /**
     * Get a [Color] from a cmy string.
     */
    private fun fromCmyString(cmyText: String): Color {
        val cmy = cmyText.split(",")
            .map { it.trim() }
            .map { it.toFloat().projectOnto(0..1) }
        return Color(1 - cmy[0], 1 - cmy[1], 1 - cmy[2])
    }

    /**
     * Convert a [Color] to a cmy string.
     */
    private fun Color.toCmyString() = listOf(red, green, blue)
        .map { 1.0 - (it / 255.0) }.joinToString(", ") { it.format() }

    /**
     * Convert a gray string (i.e., one number taken from the interval [0, 1]) to a [Color].
     */
    private fun fromGrayString(grayText: String): Color {
        fun Float.toRgb() = (this * 255).toInt()
        val gray = grayText.toFloat().projectOnto(0..255)
        return Color(gray.toRgb(), gray.toRgb(), gray.toRgb())
    }

    /**
     * Get a grayscale number from a [Color] object.
     *
     * When the color itself is not gray, it is converted to grayscale by using weights for each color vector [`[1]`](https://en.wikipedia.org/wiki/Grayscale#Converting_color_to_grayscale).
     *
     * 1. [https://en.wikipedia.org/wiki/Grayscale#Converting_color_to_grayscale](https://en.wikipedia.org/wiki/Grayscale#Converting_color_to_grayscale)
     */
    private fun Color.toGrayString() = listOf(0.2126, 0.7152, 0.0722)
        .zip(listOf(red, green, blue))
        .sumOf { (weight, rgb): Pair<Double, Int> -> weight * (rgb / 255.0) }
        .format()

    /**
     * Get a [Color] from a hex color string.
     */
    private fun fromHtmlString(htmlText: String): Color {
        return Color.decode("#$htmlText")
    }

    /**
     * Get the hex string of a [Color], without leading #.
     */
    private fun Color.toHtmlStsring() = "${red.toHexString()}${green.toHexString()}${blue.toHexString()}"

    /**
     * Project [this] onto [range] by taking
     * - the minimum of the [range] if [this] is smaller than every element in the [range],
     * - [this] if it is within the range, and
     * - the maximum of the [range] if [this] is larger than every element in the [range].
     */
    private fun Int.projectOnto(range: IntRange) = max(range.first, min(range.last, this))

    /**
     * @see [projectOnto]
     */
    private fun Float.projectOnto(range: IntRange) = max(range.first.toFloat(), min(range.last.toFloat(), this))

    private fun Double.format(digits: Int = 3) = String.format("%.${digits}f", this)
}