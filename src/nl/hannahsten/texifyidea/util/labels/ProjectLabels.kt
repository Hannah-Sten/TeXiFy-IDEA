package nl.hannahsten.texifyidea.util.labels

import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.index.BibtexEntryIndex
import nl.hannahsten.texifyidea.index.LatexParameterLabeledCommandsIndex
import nl.hannahsten.texifyidea.index.LatexParameterLabeledEnvironmentsIndex
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.lang.alias.CommandManager
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.runInBackgroundWithoutProgress

/**
 * Finds all defined labels within the project, including bibtex entries.
 *
 * @return The found label commands.
 */
fun Project.findAllLabelsAndBibtexIds(): Collection<PsiElement> {
    val labelDefinitions = getLabelDefinitionCommands()
    val commands = NewCommandsIndex.getByNames(labelDefinitions, this)
//    val commands = LatexCommandsIndex.Util.getItems(this).findLatexCommandsLabels(this)
    val bibtexIds = BibtexEntryIndex().getIndexedEntries(this)
    val environments = LatexParameterLabeledEnvironmentsIndex.Util.getItems(this)
    val parameterLabeledCommands = LatexParameterLabeledCommandsIndex.Util.getItems(this)
    val result = ArrayList<PsiElement>(commands)
    result.addAll(bibtexIds)
    result.addAll(environments)
    result.addAll(parameterLabeledCommands)
    return result
}

/**
 * All commands that represent a reference to a label, including user defined commands.
 */
fun Project.getLabelReferenceCommands(): Set<String> {
    CommandManager.updateAliases(CommandMagic.labelReferenceWithoutCustomCommands, this)
    return CommandManager.getAliases(CommandMagic.labelReferenceWithoutCustomCommands.first())
}

/**
 * Get all commands defining labels, including user defined commands. This will not check if the aliases need to be updated.
 */
fun getLabelDefinitionCommandsNoUpdate() = CommandManager.getAliases(CommandMagic.labelDefinitionsWithoutCustomCommands.first())

/**
 * Get all commands defining labels, including user defined commands.
 * If you need to know which parameters of user defined commands define a label, use [CommandManager.labelAliasesInfo].
 *
 * This will check if the cache of user defined commands needs to be updated, based on the given project, and therefore may take some time.
 */
fun Project.getLabelDefinitionCommands(): Set<String> {
    // Check if updates are needed
    CommandManager.updateAliases(CommandMagic.labelDefinitionsWithoutCustomCommands, this)
    return CommandManager.getAliases(CommandMagic.labelDefinitionsWithoutCustomCommands.first())
}

/**
 * See [getLabelDefinitionCommands], but will not wait until the update is finished.
 */
fun Project.getLabelDefinitionCommandsAndUpdateLater(): Set<String> {
    // Check if updates are needed
    runInBackgroundWithoutProgress {
        smartReadAction(this) {
            CommandManager.updateAliases(CommandMagic.labelDefinitionsWithoutCustomCommands, this)
        }
    }
    return CommandManager.getAliases(CommandMagic.labelDefinitionsWithoutCustomCommands.first())
}
