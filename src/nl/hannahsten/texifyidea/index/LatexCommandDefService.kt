package nl.hannahsten.texifyidea.index

/**
 * Provide a unified definition service for LaTeX commands, including
 *   * those hard-coded in the plugin, see [nl.hannahsten.texifyidea.util.magic.CommandMagic], [nl.hannahsten.texifyidea.lang.commands.LatexRegularCommand]
 *   * those indexed by file-based index [nl.hannahsten.texifyidea.index.file.LatexExternalCommandIndex]
 *   * those indexed by stub-based index [nl.hannahsten.texifyidea.index.NewDefinitionIndex]
 */
class LatexCommandDefService