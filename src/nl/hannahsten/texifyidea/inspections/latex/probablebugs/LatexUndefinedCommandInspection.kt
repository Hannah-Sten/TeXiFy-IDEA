package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import nl.hannahsten.texifyidea.index.file.LatexExternalCommandIndex
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexRegularCommand
import nl.hannahsten.texifyidea.util.definedCommandName
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import nl.hannahsten.texifyidea.util.files.definitionsInFileSet
import nl.hannahsten.texifyidea.util.includedPackages
import nl.hannahsten.texifyidea.util.magic.CommandMagic

/**
 * Warn when the user uses a command that is not defined in any included packages or LaTeX base.
 *
 * @author Thomas
 */
class LatexUndefinedCommandInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "UndefinedCommand"

    override fun getDisplayName() = "Undefined command"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val includedPackages = file.includedPackages().toSet().plus(LatexPackage.DEFAULT)
        val commandsInFile = file.commandsInFile()
        val commandNamesInFile = commandsInFile.map { it.name }
        // The number of indexed commands can be quite large (50k+) so we filter the large set based on the small one (in this file).
        val indexedCommands = FileBasedIndex.getInstance().getAllKeys(LatexExternalCommandIndex.id, file.project)
            .filter { it in commandNamesInFile }
            .associateWith { command ->
                val containingPackages = FileBasedIndex.getInstance().getContainingFiles(LatexExternalCommandIndex.id, command, GlobalSearchScope.everythingScope(file.project))
                    .map { LatexPackage.create(it) }
                    .toSet()
                containingPackages
            }
        val magicCommands = LatexRegularCommand.ALL.associate { Pair(it.command, setOf(it.dependency)) }
        val userDefinedCommands = file.definitionsInFileSet().filter { it.name in CommandMagic.commandDefinitions }
            .map { it.definedCommandName() }
            .associateWith { setOf(LatexPackage.DEFAULT) }

        // Join all the maps
        val allKnownCommands = (indexedCommands.keys + magicCommands.keys + userDefinedCommands.keys).associateWith {
            indexedCommands.getOrDefault(it, setOf()) + magicCommands.getOrDefault(it, setOf()) + userDefinedCommands.getOrDefault(it, setOf())
        }

        return commandsInFile.filter { allKnownCommands.getOrDefault(it.name, emptyList()).intersect(includedPackages).isEmpty() }
            .map { manager.createProblemDescriptor(it, it.textRange, "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOntheFly) }
    }

    // todo quickfix
}