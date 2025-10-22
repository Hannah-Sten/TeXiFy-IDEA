package nl.hannahsten.texifyidea.formatting.spacingrules

import com.intellij.formatting.ASTBlock
import com.intellij.formatting.Spacing
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import nl.hannahsten.texifyidea.formatting.createSpacing
import nl.hannahsten.texifyidea.lang.predefined.EnvironmentNames
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.LatexEnvironmentContent
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import nl.hannahsten.texifyidea.util.getIndent
import nl.hannahsten.texifyidea.util.parser.firstParentOfType
import kotlin.math.min

/** At this length, we put table cells on their own line. */
const val LINE_LENGTH = 80

/**
 * Returns the table environment content if the parent is in a table environment.
 */
fun checkTableEnvironment(parent: ASTBlock, child: ASTBlock): LatexEnvironmentContent? {
    // Check if parent is in environment content of a table environment
    // node - no_math_content - environment_content - environment: We have to go two levels up
    val contentElement = parent.node?.psi?.firstParentOfType<LatexEnvironmentContent>(2) ?: return null
    val envNode = contentElement.parent as? LatexEnvironment ?: return null
    if (envNode.getEnvironmentName() != EnvironmentNames.TABULAR) return null
    // Ignore raw texts
    if (child.node?.elementType == LatexTypes.RAW_TEXT_TOKEN || parent.node?.elementType == LatexTypes.RAW_TEXT) return null
    return contentElement
}

/**
 * Align spaces to the right of &
 */
fun rightTableSpaceAlign(latexCommonSettings: CommonCodeStyleSettings, parent: ASTBlock, left: ASTBlock): Spacing? {
    checkTableEnvironment(parent, left) ?: return null
    val leftText = left.node?.text ?: return null
    if (!leftText.endsWith("&")) return null
    if (leftText.endsWith("\\&")) return null

    return createSpacing(
        minSpaces = 1,
        maxSpaces = 1,
        minLineFeeds = 0,
        keepLineBreaks = latexCommonSettings.KEEP_LINE_BREAKS,
        keepBlankLines = latexCommonSettings.KEEP_BLANK_LINES_IN_CODE
    )
}

/**
 * Align spaces to the left of & or \\
 */
fun leftTableSpaceAlign(latexCommonSettings: CommonCodeStyleSettings, parent: ASTBlock, right: ASTBlock): Spacing? {
    val contentElement = checkTableEnvironment(parent, right) ?: return null

    val tableLineSeparator = "\\\\"
    val rightNodeText = right.node?.text ?: return null
    if (!rightNodeText.startsWith("&") && rightNodeText != tableLineSeparator) return null

    val content = contentElement.text ?: return null
    val contentLines = content.split(tableLineSeparator)
        .mapNotNull { if (it.isBlank()) null else it + tableLineSeparator }
        .toMutableList()
    if (contentLines.size < 2) return null
    val indent = content.split("\n").map { "\n" + it }.getOrNull(1)?.getIndent() ?: return null

    // Fix environment content not starting with indent
    contentLines[0] = indent + contentLines.first()

    val absoluteAmpersandIndicesPerLine = getAmpersandOffsets(contentElement.textOffset, indent, contentLines)

    val contentWithoutRules = removeRules(content, tableLineSeparator)

    var spaces =
        getNumberOfSpaces(contentWithoutRules, tableLineSeparator, right, absoluteAmpersandIndicesPerLine, indent)
            ?: return null

    // Convert a -1 return value to a line feed.
    var lineFeeds = 0
    // I think checking for 'ensure right margin is not exceeded' makes more sense, but don't know which option that is
    if (spaces < 0 && latexCommonSettings.WRAP_ON_TYPING > 0) {
        spaces = 0
        lineFeeds = 1
    }

    return createSpacing(
        minSpaces = spaces,
        maxSpaces = spaces,
        minLineFeeds = lineFeeds,
        keepLineBreaks = latexCommonSettings.KEEP_LINE_BREAKS,
        keepBlankLines = latexCommonSettings.KEEP_BLANK_LINES_IN_CODE
    )
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
            // Do not count escaped ampersands: \&
            if ((it == '&') && (i == 0 || line[i - 1] != '\\')) indices.add(currentOffset)
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

fun getNumberOfSpaces(
    contentWithoutRules: String,
    tableLineSeparator: String,
    right: ASTBlock,
    absoluteAmpersandIndicesPerLine: List<List<Int>>,
    indent: String
): Int? {
    val contentLinesWithoutRules = contentWithoutRules.split(tableLineSeparator)
        .mapNotNull { if (it.isBlank()) null else it + tableLineSeparator }
        .toMutableList()
    if (contentLinesWithoutRules.isEmpty()) return null
    contentLinesWithoutRules[0] = indent + contentLinesWithoutRules.first()

    val relativeIndices = removeExtraSpaces(contentLinesWithoutRules)

    val spacesPerCell = getSpacesPerCell(relativeIndices, contentLinesWithoutRules)

    return getSpacesForRightBlock(right, absoluteAmpersandIndicesPerLine, spacesPerCell, relativeIndices)
}

/**
 * Remove all extra spaces and remember how many we removed
 *
 * @return List of lists, each list is a list of indices (the offset in the line for this ampersand)
 */
private fun removeExtraSpaces(contentLinesWithoutRules: MutableList<String>): List<List<Int>> {
    return contentLinesWithoutRules.map { line ->
        // (relative index after removing spaces, number of spaces removed on this line before this ampersand)
        val indices = mutableListOf<Int>()
        var removedSpaces = 0
        line.withIndex().forEach { (i, value) ->
            when {
                // Ignore escaped ampersands
                value == '&' && if (i > 0) line[i - 1] != '\\' else true -> {
                    indices += i - removedSpaces
                }
                value == '\\' && if (i < line.length - 1) line[i + 1] == '\\' else false -> {
                    indices += i - removedSpaces
                }
                value in setOf(' ', '\n') -> {
                    if (i > 0 && i < line.length - 1) {
                        // Spaces after an ignored ampersand are not removed
                        val isAfterSpaceOrSeparator = (line[i - 1] in setOf(' ', '&', '\n') && (i < 2 || line[i - 2] != '\\'))
                        val isBeforeSpaceOrSeparator = line[i + 1] in setOf(' ', '&', '\n')
                        val isBeforeDoubleBackslash = i < line.length - 2 && line[i + 1] == '\\' && line[i + 2] == '\\'
                        if (isAfterSpaceOrSeparator || isBeforeSpaceOrSeparator || isBeforeDoubleBackslash) removedSpaces++
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
private fun getSpacesPerCell(
    relativeIndices: List<List<Int>>,
    contentLinesWithoutRules: MutableList<String>
): List<List<Int>> {
    val nrLevels = relativeIndices.maxOfOrNull { it.size } ?: 0

    // If we are on a on a table line that is split over multiple `physical' lines,
    // ignore this line in all computations.
    fun String?.ignore(): Boolean {
        if (this == null) return false
        val containsNewLines = split("\n").filter { it.isNotBlank() }.size > 1
        val lessCells = count { it == '&' } + 1 < nrLevels
        return containsNewLines || lessCells
    }

    // In each line, compute the width of each cell.
    val cellWidthsPerLine = relativeIndices.mapIndexed { i, line ->
        if (contentLinesWithoutRules.getOrNull(i)?.ignore() == true) List(nrLevels) { 0 }
        else listOf(line.first()) + line.zipWithNext { a, b ->
            // Empty cells should have width 0.
            (b - a).let {
                if (it == 1) 0 else it
            }
        }
    }

    // Take the maximum width of each i-th cell over all lines.
    val cellWidths = cellWidthsPerLine.first().indices.map { level ->
        cellWidthsPerLine.mapNotNull { it.getOrNull(level) }.maxOrNull() ?: return mutableListOf()
    }

    // The number of spaces that has to be added to this cell is the
    // difference with the largest cell in this column, plus one additional
    // space that has to be added to every cell.
    return cellWidthsPerLine.mapIndexed { i, line ->
        line.mapIndexed { level, cellWidth ->
            if (contentLinesWithoutRules.getOrNull(i)?.ignore() == true) 1
            // Add 1 for the space that always has to be there.
            else cellWidths.getOrNull(level)?.minus(cellWidth)?.plus(1) ?: 1
        }
    }
}

/**
 * Find level of 'right' block (which is a & or \\).
 *
 * @return Number of spaces, -1 if a newline is required.
 */
private fun getSpacesForRightBlock(
    right: ASTBlock,
    absoluteAmpersandIndicesPerLine: List<List<Int>>,
    spacesPerCell: List<List<Int>>,
    relativeIndices: List<List<Int>>
): Int? {
    val rightElementOffset = right.node?.psi?.textOffset ?: return null
    // Current and desired index relative to line start, e.g. in case 'asdf__&` is desired, index 6
    for ((i, absoluteIndices) in absoluteAmpersandIndicesPerLine.withIndex()) {
        for (level in absoluteIndices.indices) {
            // Try to find the offset of the right & in the list of all & offsets
            if (absoluteIndices[level] == rightElementOffset) {
                // For very long lines, it's a lot more readable to start & on a new line instead of inserting a whole bunch of spaces
                // Make sure not to only put the \\ on a new line
                val didPreviousCellGetNewline = if (level == 0) true else (
                    relativeIndices.getOrNull(i)?.getOrNull(level - 1)
                        ?: 0
                    ) > LINE_LENGTH
                if ((
                        relativeIndices.getOrNull(i)?.getOrNull(level)
                            ?: 0
                        ) > LINE_LENGTH && (didPreviousCellGetNewline || level < absoluteIndices.size - 1)
                ) return -1
                return spacesPerCell.getOrNull(min(i, spacesPerCell.size - 1))?.getOrNull(level)
            }
        }
    }
    return null
}