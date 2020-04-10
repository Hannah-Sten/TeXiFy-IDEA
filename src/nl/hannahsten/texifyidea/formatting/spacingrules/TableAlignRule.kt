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
import kotlin.math.max

/**
 * Align spaces to the right of &
 */
fun rightTableSpaceAlign(latexCommonSettings: CommonCodeStyleSettings, parent: ASTBlock, left: ASTBlock): Spacing? {

    if (parent.node?.psi?.firstParentOfType(LatexEnvironmentContent::class)?.firstParentOfType(LatexEnvironment::class)?.environmentName !in Magic.Environment.tableEnvironments) return null

    if (left.node?.text != "&") return null

    return createSpacing(
            minSpaces = 1,
            maxSpaces = 1,
            minLineFeeds = 0,
            keepLineBreaks = latexCommonSettings.KEEP_LINE_BREAKS,
            keepBlankLines = latexCommonSettings.KEEP_BLANK_LINES_IN_CODE)
}

/**
 * Align spaces to the left of \\
 */
fun tableLineSeparatorSpaceAlign(parent: ASTBlock, right: ASTBlock): Int? {

    val contentElement = parent.node?.psi?.firstParentOfType(LatexEnvironmentContent::class)
    if (contentElement?.firstParentOfType(LatexEnvironment::class)?.environmentName !in Magic.Environment.tableEnvironments) return null

    val tableLineSeparator = "\\\\"
    if (right.node?.text != tableLineSeparator) return null

    val content = contentElement?.text ?: return null
    val contentLines = content.split("\n").toMutableList()
    if (contentLines.size < 2) return null
    val indent = contentLines[1].getIndent()
    contentLines[0] = indent + contentLines.first()

    val removedSpaces = mutableListOf<Int>()

    // First remove spaces before \\, to calculate offsets consistently independent of the number of spaces before \\
    for (i in contentLines.indices) {
        val index = contentLines[i].lastIndexOf(tableLineSeparator)
        if (index == -1) continue
        val oldLength = contentLines[i].length
        contentLines[i] = contentLines[i].substring(0, index).dropLastWhile { it.isWhitespace() } + contentLines[i].substring(index, contentLines[i].length)
        removedSpaces.add(oldLength - contentLines[i].length)
    }

    // Find maximum index of \\, relative to line start
    val maxIndex = contentLines.fold(0) { max, line ->
        max(max, line.lastIndexOf(tableLineSeparator))
    }

    // Find relative and absolute (relative to file start) indices of all \\
    var currentOffset = contentElement.textOffset - indent.length

    val indices = contentLines.mapIndexedNotNull { i, line ->
        val absoluteOffset = currentOffset
        currentOffset += line.length + removedSpaces[i] + "\n".length
        val relativeOffset = line.lastIndexOf(tableLineSeparator)
        if (relativeOffset == -1) return@mapIndexedNotNull null
        Pair(relativeOffset, absoluteOffset + removedSpaces[i] + relativeOffset)
    }

    // Find the current \\
    val rightOffset = right.textRange.startOffset
    val relativeOffset = indices.find { it.second == rightOffset }?.first ?: return null

    return maxIndex - relativeOffset + 1
}

/**
 * Align spaces to the left of &
 */
fun leftTableSpaceAlign(latexCommonSettings: CommonCodeStyleSettings, parent: ASTBlock, right: ASTBlock): Spacing? {
    // Check if parent is in environment content of a table environment
    val contentElement = parent.node?.psi?.firstParentOfType(LatexEnvironmentContent::class)
    if (contentElement?.firstParentOfType(LatexEnvironment::class)?.environmentName !in Magic.Environment.tableEnvironments) return null

    val tableLineSeparator = "\\\\"
    if (right.node?.text != "&" && right.node?.text != tableLineSeparator) return null

    val content = contentElement?.text ?: return null
    val contentLines = content.split(tableLineSeparator).mapNotNull { if (it.isBlank()) null else it + tableLineSeparator }.toMutableList()
    if (contentLines.size < 2) return null
    val indent = content.split("\n").map { "\n" + it }[1].getIndent()

    // Fix environment content not starting with indent
    contentLines[0] = indent + contentLines.first()

    val absoluteAmpersandIndicesPerLine = getAmpersandOffsets(contentElement.textOffset, indent, contentLines, tableLineSeparator)

    val contentWithoutRules = removeRules(content, tableLineSeparator)

    val spaces = getNumberOfSpaces(contentWithoutRules, tableLineSeparator, right, absoluteAmpersandIndicesPerLine, indent) ?: return null

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
fun getAmpersandOffsets(contentTextOffset: Int, indent: String, contentLines: MutableList<String>, tableLineSeparator: String): List<List<Int>> {
    // We added fake content, but it doesn't count for the text offset
    var currentOffset = contentTextOffset - indent.length

    return contentLines.map { line ->
        val indices = mutableListOf<Int>()
        line.withIndex().forEach { (i, it) ->
            if (it == '&') indices.add(currentOffset)
            if (it == '\\' && if (i < line.length - 1) line[i+1] == '\\' else false) indices.add(currentOffset)
            currentOffset++
        }
//        currentOffset += tableLineSeparator.length
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

    val contentLinesWithoutRules = contentWithoutRules.split(tableLineSeparator).mapNotNull { if (it.isBlank()) null else it + tableLineSeparator }.toMutableList()
    contentLinesWithoutRules[0] = indent + contentLinesWithoutRules.first()

    val newLinesAndRelativeIndices = removeExtraSpaces(contentLinesWithoutRules)

    val newContentLines = newLinesAndRelativeIndices.map { it.first }
    val relativeIndices = newLinesAndRelativeIndices.map { it.second }

    val levelIndices = getLevelIndices(newContentLines, relativeIndices)

    return getLevelOfRightBlock(right, absoluteAmpersandIndicesPerLine, relativeIndices, levelIndices)
}

/**
 * Remove all extra spaces and remember how many we removed
 *
 * @return List of pairs, each pair consists of a line and a list of indices (the offset in the line for this ampersand)
 */
private fun removeExtraSpaces(contentLinesWithoutRules: MutableList<String>): List<Pair<String, List<Int>>> {
    return contentLinesWithoutRules.map { line ->
        // (relative index after removing spaces, number of spaces removed on this line before this ampersand)
        val indices = mutableListOf<Int>()
        var newLine = ""
        var removedSpaces = 0
        line.withIndex().forEach { (i, value) ->
            when {
                value == '&' -> {
                    indices += i - removedSpaces
                    newLine += value
                }
                value == '\\' && if (i < line.length - 1) line[i+1] == '\\' else false -> {
                    indices += i - removedSpaces
                    newLine += value
                }
                value in setOf(' ', '\n') -> {
                    if (i > 0 && i < line.length - 1) {
                        if (line[i - 1] !in setOf(' ', '&', '\n') && line[i + 1] !in setOf(' ', '&', '\n')) newLine += value
                        else removedSpaces++
                    }
                }
                else -> {
                    newLine += value
                }
            }
        }
        Pair(newLine, indices.toList())
    }
}

/**
 * Get the desired index of the first &, second &, etc.
 * Relative to the line start
 * Because the placement of the second & depends on the placement of the first one,
 * we have to take them one by one
 */
private fun getLevelIndices(newContentLines: List<String>, relativeIndices: List<List<Int>>): MutableList<Int> {
    val levelIndices = mutableListOf<Int>()
    // Add 1 for the line separator \\
    val numberOfAmpersands = newContentLines.first().count { it == '&' } + 1
    for (level in (0 until numberOfAmpersands)) {
        for ((i, it) in relativeIndices.withIndex()) {
            if (it.isEmpty()) continue
            if (i >= relativeIndices.size) continue
            if (level >= relativeIndices.first().size) continue

            // Get new index of this level based on movements of lower levels of this line

            // Total added extra spaces on this line so far
            val totalAddedSpaces = if (level == 0) 0 else levelIndices[level - 1] - it[level - 1]

            val newIndex = totalAddedSpaces + it[level]

            // If this level does not exist yet
            if (level >= levelIndices.size) {
                levelIndices.add(newIndex)
            }
            // If the index is larger than the largest one so far
            else if (newIndex > levelIndices[level]) {
                levelIndices[level] = newIndex
            }
        }
    }
    return levelIndices
}

/**
 * Find level of 'right' block (which is a &)
 */
private fun getLevelOfRightBlock(right: ASTBlock, absoluteAmpersandIndicesPerLine: List<List<Int>>, relativeIndices: List<List<Int>>, levelIndices: MutableList<Int>): Int? {
    val rightElementOffset = right.node?.psi?.textOffset ?: return null
    // Current and desired index relative to line start, e.g. in case 'asdf__&` is desired, index 6
    var spaces: Int? = null
    for ((i, absoluteIndices) in absoluteAmpersandIndicesPerLine.withIndex()) {
        for (level in absoluteIndices.indices) {
            // Try to find the offset of the right & in the list of all & offsets
            if (absoluteIndices[level] == rightElementOffset) {
                val totalAddedSpaces = if (level == 0) 0 else levelIndices[level - 1] - relativeIndices[i][level - 1]

                val currentLevelIndex = totalAddedSpaces + relativeIndices[i][level]
                val desiredLevelIndex = if (level < levelIndices.size) {
                    levelIndices[level]
                }
                else {
                    break
                }

                // +1 for the space which always has to be there
                spaces = max(1, desiredLevelIndex - currentLevelIndex + 1)
                break
            }
        }
    }
    return spaces
}