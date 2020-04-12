package nl.hannahsten.texifyidea.formatting.spacingrules

import com.intellij.formatting.ASTBlock
import com.intellij.formatting.Spacing
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import nl.hannahsten.texifyidea.formatting.createSpacing
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.LatexEnvironmentContent
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.firstParentOfType
import nl.hannahsten.texifyidea.util.getIndent

/**
 * Align spaces to the right of &
 */
fun rightTableSpaceAlign(latexCommonSettings: CommonCodeStyleSettings, parent: ASTBlock, left: ASTBlock): Spacing? {

    if (parent.node?.psi?.firstParentOfType(LatexEnvironmentContent::class)
                    ?.firstParentOfType(LatexEnvironment::class)?.environmentName !in Magic.Environment.tableEnvironments) return null

    if (left.node?.text != "&") return null

    return createSpacing(
            minSpaces = 1,
            maxSpaces = 1,
            minLineFeeds = 0,
            keepLineBreaks = latexCommonSettings.KEEP_LINE_BREAKS,
            keepBlankLines = latexCommonSettings.KEEP_BLANK_LINES_IN_CODE)
}

/**
 * Align spaces to the left of & or \\
 */
fun leftTableSpaceAlign(latexCommonSettings: CommonCodeStyleSettings, parent: ASTBlock, right: ASTBlock): Spacing? {
    // Check if parent is in environment content of a table environment
    val contentElement = parent.node?.psi?.firstParentOfType(LatexEnvironmentContent::class)
    if (contentElement?.firstParentOfType(LatexEnvironment::class)?.environmentName !in Magic.Environment.tableEnvironments) return null

    val tableLineSeparator = "\\\\"
    if (right.node?.text != "&" && right.node?.text != tableLineSeparator) return null

    val content = contentElement?.text ?: return null
    val contentLines = content.split(tableLineSeparator)
            .mapNotNull { if (it.isBlank()) null else it + tableLineSeparator }
            .toMutableList()
    if (contentLines.size < 2) return null
    val indent = content.split("\n").map { "\n" + it }[1].getIndent()

    // Fix environment content not starting with indent
    contentLines[0] = indent + contentLines.first()

    val absoluteAmpersandIndicesPerLine = getAmpersandOffsets(contentElement.textOffset, indent, contentLines)

    val contentWithoutRules = removeRules(content, tableLineSeparator)

    val spaces = getNumberOfSpaces(contentWithoutRules, tableLineSeparator, right, absoluteAmpersandIndicesPerLine, indent)
            ?: return null

    return createSpacing(
            minSpaces = spaces,
            maxSpaces = spaces,
            minLineFeeds = 0,
            keepLineBreaks = latexCommonSettings.KEEP_LINE_BREAKS,
            keepBlankLines = latexCommonSettings.KEEP_BLANK_LINES_IN_CODE)
}

/**
 * Find the current absolute offset of all the ampersands
 */
fun getAmpersandOffsets(contentTextOffset: Int, indent: String, contentLines: MutableList<String>): List<List<Int>> {
    // We added fake content, but it doesn't count for the text offset
    var currentOffset = contentTextOffset - indent.length

    return contentLines.map { line ->
        val indices = mutableListOf<Int>()
        line.withIndex().forEach { (i, it) ->
            if (it == '&') indices.add(currentOffset)
            if (it == '\\' && if (i < line.length - 1) line[i + 1] == '\\' else false) indices.add(currentOffset)
            currentOffset++
        }
        indices.toList()
    }
}

/**
 * Remove lines without &, and everything after \\ to avoid confusing while aligning the &'s
 * Assumes users place things such as \hline or \midrule after the \\ or on a separate line
 */
fun removeRules(content: String, tableLineSeparator: String): String {
    return content.split("\n").mapNotNull { line ->
        when {
            line.contains(tableLineSeparator) -> {
                // remove everything after \\
                line.split(tableLineSeparator).first() + tableLineSeparator
            }
            line.count { it == '&' } == 0 -> null
            else -> line
        }
    }.joinToString("\n")
}

fun getNumberOfSpaces(contentWithoutRules: String, tableLineSeparator: String, right: ASTBlock, absoluteAmpersandIndicesPerLine: List<List<Int>>, indent: String): Int? {

    val contentLinesWithoutRules = contentWithoutRules.split(tableLineSeparator)
            .mapNotNull { if (it.isBlank()) null else it + tableLineSeparator }
            .toMutableList()
    if (contentLinesWithoutRules.isEmpty()) return null
    contentLinesWithoutRules[0] = indent + contentLinesWithoutRules.first()

    val relativeIndices = removeExtraSpaces(contentLinesWithoutRules)

    val spacesPerCell = getSpacesPerCell(relativeIndices, contentLinesWithoutRules)

    return getSpacesForRightBlock(right, absoluteAmpersandIndicesPerLine, spacesPerCell)
}

/**
 * Remove all extra spaces and remember how many we removed
 *
 * @return List of pairs, each pair consists of a line and a list of indices (the offset in the line for this ampersand)
 */
private fun removeExtraSpaces(contentLinesWithoutRules: MutableList<String>): List<List<Int>> {
    return contentLinesWithoutRules.map { line ->
        // (relative index after removing spaces, number of spaces removed on this line before this ampersand)
        val indices = mutableListOf<Int>()
        var removedSpaces = 0
        line.withIndex().forEach { (i, value) ->
            when {
                value == '&' -> {
                    indices += i - removedSpaces
                }
                value == '\\' && if (i < line.length - 1) line[i + 1] == '\\' else false -> {
                    indices += i - removedSpaces
                }
                value in setOf(' ', '\n') -> {
                    if (i > 0 && i < line.length - 1) {
                        if (!(line[i - 1] !in setOf(' ', '&', '\n') && line[i + 1] !in setOf(' ', '&', '\n', '\\'))) removedSpaces++
                    }
                }
                else -> {
                }
            }
        }
        indices.toList()
    }
}

/**
 * Get the desired index of the first &, second &, etc, relative to the line start.
 *
 * Get the number of spaces that has to be added in each cell.
 * 1 when we should do nothing, except the single required space.
 * Indexed by line, then by level.
 */
private fun getSpacesPerCell(relativeIndices: List<List<Int>>, contentLinesWithoutRules: MutableList<String>): List<List<Int>> {
    val nrLevels = relativeIndices.map { it.size }.max() ?: 0
    // If we are on a on a table line that is split over multiple `physical' lines,
    // ignore this line in all computations.
    fun String.ignore(): Boolean {
        val containsNewLines = split("\n").filter { it.isNotBlank() }.size > 1
        val lessCells = count { it == '&' } + 1 < nrLevels
        return containsNewLines || lessCells
    }

    // In each line, compute the width of each cell.
    val cellWidthsPerLine = relativeIndices.mapIndexed { i, line ->
        if (contentLinesWithoutRules[i].ignore()) List(nrLevels) {0}
        else listOf(line.first()) + line.zipWithNext { a, b -> b - a }
    }

    // Take the maximum width of each i-th cell over all lines.
    val cellWidths = cellWidthsPerLine.first().indices.map { level ->
        cellWidthsPerLine.map { it[level] }.max() ?: return mutableListOf()
    }

    // The number of spaces that has to be added to this cell is the
    // difference with the largest cell in this column, plus one additional
    // space that has to be added to every cell.
    return cellWidthsPerLine.mapIndexed { i, line ->
        line.mapIndexed { level, cellWidth ->
            if (contentLinesWithoutRules[i].ignore()) 1
            // Add 1 for the space that always has to be there.
            else cellWidths[level] - cellWidth + 1
        }
    }
}

/**
 * Find level of 'right' block (which is a & or \\)
 */
private fun getSpacesForRightBlock(right: ASTBlock, absoluteAmpersandIndicesPerLine: List<List<Int>>, spacesPerCell: List<List<Int>>): Int? {
    val rightElementOffset = right.node?.psi?.textOffset ?: return null
    // Current and desired index relative to line start, e.g. in case 'asdf__&` is desired, index 6
    for ((i, absoluteIndices) in absoluteAmpersandIndicesPerLine.withIndex()) {
        for (level in absoluteIndices.indices) {
            // Try to find the offset of the right & in the list of all & offsets
            if (absoluteIndices[level] == rightElementOffset) {
                return spacesPerCell[i][level]
            }
        }
    }
    return null
}