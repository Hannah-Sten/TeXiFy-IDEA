package nl.hannahsten.texifyidea.inspections.latex.redundancy

import com.google.common.collect.HashMultiset
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.traverseCommands
import nl.hannahsten.texifyidea.util.isInConditionalBranch
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.definedCommandName

/**
 * Warns for commands that are defined twice in the same fileset.
 *
 * @author Hannah Schellekens
 */
open class LatexDuplicateDefinitionInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "DuplicateDefinition"

    override fun getDisplayName() = "Duplicate command definitions"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        // Find all defined commands.
        val defined = HashMultiset.create<String>()
//        val definitions = file.definitionsInFileSet().filter { it.name in CommandMagic.regularStrictCommandDefinitions }
        val fileSetScope = LatexProjectStructure.buildFilesetScopeFor(file)
        val definitions = NewCommandsIndex.getByNames(CommandMagic.regularStrictCommandDefinitions, file.project, fileSetScope)
        for (command in definitions) {
            if (isInConditionalBranch(command)) continue
            val name = command.definedCommandName() ?: continue
            defined.add(name)
        }

        // Go monkeys.
        file.traverseCommands().filter {
            it.name in CommandMagic.definitions
        }.forEach {
            if (isInConditionalBranch(it)) return@forEach
            val definedCmd = it.definedCommandName() ?: return@forEach
            if (defined.count(definedCmd) > 1) {
                descriptors.add(
                    manager.createProblemDescriptor(
                        it,
                        "Command '$definedCmd' is defined multiple times",
                        true,
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly
                    )
                )
            }
        }

        return descriptors
    }
}
