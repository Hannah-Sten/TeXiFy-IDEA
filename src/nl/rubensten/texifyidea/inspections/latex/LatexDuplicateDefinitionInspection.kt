package nl.rubensten.texifyidea.inspections.latex

import com.google.common.collect.HashMultiset
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.util.definedCommandName
import nl.rubensten.texifyidea.util.definitions
import nl.rubensten.texifyidea.util.definitionsInFileSet
import nl.rubensten.texifyidea.util.isCommandDefinition

/**
 * @author Ruben Schellekens
 */
open class LatexDuplicateDefinitionInspection : TexifyInspectionBase() {

    override fun getInspectionGroup() = InsightGroup.LATEX

    override fun getInspectionId() = "DuplicateDefinition"

    override fun getDisplayName() = "Duplicate command definitions"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        // Find all defined commands.
        val defined = HashMultiset.create<String>()
        val definitions = file.definitionsInFileSet().filter { it.isCommandDefinition() }
        for (command in definitions) {
            val name = command.definedCommandName() ?: continue
            defined.add(name)
        }

        // Go monkeys.
        file.definitions()
                .filter { it.isCommandDefinition() }
                .forEach {
                    val definedCmd = it.definedCommandName() ?: return@forEach
                    if (defined.count(definedCmd) > 1) {
                        println("Add to ${it.text}")
                        descriptors.add(manager.createProblemDescriptor(
                                it,
                                "Command '$definedCmd' is defined multiple times",
                                true,
                                ProblemHighlightType.GENERIC_ERROR,
                                isOntheFly
                        ))
                    }
                }

        return descriptors
    }
}