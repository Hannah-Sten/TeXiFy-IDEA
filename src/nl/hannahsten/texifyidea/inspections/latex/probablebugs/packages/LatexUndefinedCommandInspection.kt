package nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import nl.hannahsten.texifyidea.index.file.LatexExternalCommandIndex
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexRegularCommand
import nl.hannahsten.texifyidea.util.parser.definedCommandName
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.definitionsInFileSet
import nl.hannahsten.texifyidea.util.includedPackages
import nl.hannahsten.texifyidea.util.insertUsepackage
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.cmd

/**
 * Warn when the user uses a command that is not defined in any included packages or LaTeX base.
 * This is an extension of [LatexMissingImportInspection], however, because this also
 * complains about commands that are not hardcoded in TeXiFy but come from any package,
 * and this index of commands is far from complete, it has to be disabled by default,
 * and thus cannot be included in the mentioned inspection.
 *
 * @author Thomas
 */
class LatexUndefinedCommandInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "UndefinedCommand"

    override fun getDisplayName() = "Command is not defined"

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
        val magicCommands = LatexRegularCommand.ALL.associate { Pair(it.cmd, setOf(it.dependency)) }
        val userDefinedCommands = file.definitionsInFileSet().filter { it.name in CommandMagic.commandDefinitions }
            .map { it.definedCommandName() }
            .associateWith { setOf(LatexPackage.DEFAULT) }

        // Join all the maps, map command name (with backslash) to all packages it is defined in
        val allKnownCommands = (indexedCommands.keys + magicCommands.keys + userDefinedCommands.keys).associateWith {
            indexedCommands.getOrDefault(it, setOf()) + magicCommands.getOrDefault(it, setOf()) + userDefinedCommands.getOrDefault(it, setOf())
        }

        return commandsInFile.filter { allKnownCommands.getOrDefault(it.name, emptyList()).intersect(includedPackages).isEmpty() }
            .map { command ->
                manager.createProblemDescriptor(
                    command,
                    "Command ${command.name} is not defined",
                    allKnownCommands.getOrDefault(command.name, emptyList()).map { ImportPackageFix(it) }.toTypedArray(),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly,
                    false
                )
            }
    }

    private class ImportPackageFix(val dependency: LatexPackage) : LocalQuickFix {

        override fun getFamilyName() = "Add import for package ${dependency.name}"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            descriptor.psiElement.containingFile.insertUsepackage(dependency)
        }
    }
}