@file:Suppress("UseJBColor")

package nl.hannahsten.texifyidea.util

import arrow.core.max
import arrow.core.nonEmptyListOf
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.predefined.CommandNames
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.util.magic.ColorMagic
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import nl.hannahsten.texifyidea.util.parser.getOptionalArgumentValueByName
import nl.hannahsten.texifyidea.util.parser.getRequiredArgumentValueByName
import java.awt.Color
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * Find the color for the given command.
 */
fun findColor(colorName: String, file: PsiFile, command: LatexCommands, recursionDepth: Int = 0): Color? = when (command.name) {
    CommandNames.COLORLET -> {
        getColorFromColorParameter(file, command.getRequiredArgumentValueByName("color"), recursionDepth, command)
    }

    CommandNames.DEFINECOLOR, CommandNames.PROVIDE_COLOR -> {
        getColorFromDefineColor(
            command.getRequiredArgumentValueByName("model-list"),
            command.getRequiredArgumentValueByName("spec-list")
        )
    }

    CommandNames.DEFINECOLORSERIES -> {
        getColorFromDefineColor(
            command.getOptionalArgumentValueByName("b-model") ?: command.getRequiredArgumentValueByName("core model"),
            command.getRequiredArgumentValueByName("b-spec")
        )
    }

    else -> getColorFromColorParameter(file, colorName, recursionDepth, command)
}

fun getColorDefinitionElement(command: LatexCommands): LatexParameter? {
    val semantics = LatexDefinitionService.resolveCommand(command) ?: return null
    LatexPsiUtil.processArgumentsWithSemantics(command, semantics) process@{ param, arg ->
        if (arg?.contextSignature?.introduces(LatexContexts.ColorDefinition) == true) {
            return param
        }
    }
    return null
}

/**
 * Given the color parameter [definitionText] of a command, compute the defined color.
 */
private fun getColorFromColorParameter(file: PsiFile, definitionText: String?, recursionDepth: Int, command: LatexCommands): Color? {
    // Try a direct name match
    val defaultHex = ColorMagic.defaultXcolors[definitionText]

    @Suppress("UseJBColor") // Should show actual color also in dark mode
    if (defaultHex != null) return Color(defaultHex)

    // Infinite loops can occur with invalid color definitions.
    if (recursionDepth > 42) return null

    definitionText ?: return null
    val colorParts = definitionText.split("!").filter { it.isNotBlank() }
    val colors = colorParts.filter { it.all { c -> c.isLetter() } }
        .map { findColor(it, file, command, recursionDepth + 1) ?: return null }
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
        when (modelText.lowercase(Locale.getDefault())) {
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
    catch (_: NumberFormatException) {
        null
    }
    // Exception occurs when not enough color arguments have been typed.
    // E.g. we need three arguments (r, g, b) and have typed "255, 127".
    catch (_: IndexOutOfBoundsException) {
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
private fun mix(a: Color, b: Color, percent: Int): Color = (percent / 100.0).let {
    Color(
        (a.red * it + b.red * (1.0 - it)).toInt(),
        (a.green * it + b.green * (1.0 - it)).toInt(),
        (a.blue * it + b.blue * (1.0 - it)).toInt()
    )
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
    catch (_: NumberFormatException) {
        rgb.map { it.toFloat().projectOnto(0..1) }.let { Color(it[0], it[1], it[2]) }
    }
}

/**
 * Convert a [Color] object to an RGB string "R, G, B" with R, G, and B integers in the range [0, 255] if [integer]
 * is true, and R, G, and B floats in the range [0, 1] otherwise.
 */
fun Color.toRgbString(integer: Boolean = true): String =
    if (integer) "$red, $green, $blue"
    else listOf(red, green, blue).map { it / 255.0 }.joinToString(", ") { it.format() }

/**
 * Get the [Color] from an HSB string, assuming that the values are in the range [0, 1].
 */
private fun fromHsbString(hsbText: String): Color {
    val hsb = hsbText.split(",").map { it.trim() }
    return hsb.map { it.toFloat().projectOnto(0..1) }
        .let {
            Color.getHSBColor(
                it[0], it[1], it[2]
            )
        }
}

/**
 * Convert a color to an HSB string "hue, saturation, brightness" where each value is a float in the range [0, 1].
 */
fun Color.toHsbString(): String = Color.RGBtoHSB(red, green, blue, null)
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
fun Color.toCmykString(): String {
    val rgb = nonEmptyListOf(red, green, blue).map { it / 255.0 }
    val k: Double = 1.0 - rgb.max()
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
fun Color.toCmyString() = listOf(red, green, blue)
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
fun Color.toGrayString() = listOf(0.2126, 0.7152, 0.0722)
    .zip(listOf(red, green, blue))
    .sumOf { (weight, rgb): Pair<Double, Int> -> weight * (rgb / 255.0) }
    .format()

/**
 * Get a [Color] from a hex color string.
 */
private fun fromHtmlString(htmlText: String): Color = Color.decode("#$htmlText")

/**
 * Get the hex string of a [Color], without leading #.
 */
fun Color.toHtmlStsring() = "${red.toHexString()}${green.toHexString()}${blue.toHexString()}"

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
