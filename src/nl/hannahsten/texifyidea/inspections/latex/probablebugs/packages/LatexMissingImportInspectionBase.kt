package nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.index.projectstructure.LatexProjectStructure
import nl.hannahsten.texifyidea.inspections.AbstractTexifyContextAwareInspection
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LSemanticEntity
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.predefined.AllPredefined
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import nl.hannahsten.texifyidea.psi.nameWithoutSlash
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil

/**
 *
 * Use the [LatexDefinitionService] to resolve definitions of commands and environments and check if they are not defined, which is further divided into two cases:
 * * Missing import: the command is hardcoded in TeXiFy, but the user has not imported the package that defines it.
 * * Unknown command: the command is not hardcoded in TeXiFy, we know nothing about it.
 *
 *
 * @see LatexMissingImportInspection
 * @see LatexUndefinedCommandInspection
 * @author Li Ernest
 */
abstract class LatexMissingImportInspectionBase(inspectionId: String) : AbstractTexifyContextAwareInspection(
    InsightGroup.LATEX,
    inspectionId,
    applicableContexts = null,
    excludedContexts = emptySet(),
    skipChildrenInContext = setOf(LatexContexts.Comment)
) {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (!TexifySettings.getState().automaticDependencyCheck) {
            return false
        }
        val project = file.project
        return LatexProjectStructure.isProjectFilesetsAvailable(project) && super.isAvailableForFile(file)
    }

    override fun inspectElement(element: PsiElement, contexts: LContextSet, bundle: DefinitionBundle, file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>) {
        when (element) {
            is LatexCommands -> analyzeCommand(element, bundle, descriptors, manager, isOnTheFly)
            is LatexEnvironment -> analyzeEnvironment(element, bundle, descriptors, manager, isOnTheFly)
        }
    }

    private fun analyzeCommand(command: LatexCommands, bundle: DefinitionBundle, descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean) {
        val name = command.nameWithoutSlash ?: return
        if (bundle.lookupCommand(name) != null) return

        val contexts = LatexPsiUtil.resolveContextUpward(command, bundle)
        if (LatexContexts.CommandDeclaration in contexts) return // skip declaration of command

        val candidates = AllPredefined.findAll(name)
        if (candidates.isNotEmpty()) {
            reportCommandMissingImport(command, candidates, descriptors, manager, isOntheFly)
        }
        else {
            reportUnknownCommand(command, descriptors, manager, isOntheFly)
        }
    }

    private fun analyzeEnvironment(e: LatexEnvironment, bundle: DefinitionBundle, descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean) {
        val name = e.getEnvironmentName()
        if (bundle.lookupEnv(name) != null) return
        val candidate = AllPredefined.findAll(name)
        if (candidate.isNotEmpty()) {
            reportEnvironmentMissingImport(e, candidate, descriptors, manager, isOntheFly)
        }
        else {
            reportUnknownEnvironment(name, e, descriptors, manager, isOntheFly)
        }
    }

    protected abstract fun reportCommandMissingImport(
        command: LatexCommands, candidates: List<LSemanticEntity>,
        descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean
    )

    protected abstract fun reportEnvironmentMissingImport(
        environment: LatexEnvironment, candidates: List<LSemanticEntity>,
        descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean
    )

    protected abstract fun reportUnknownCommand(
        command: LatexCommands, descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean
    )

    protected abstract fun reportUnknownEnvironment(
        name: String, environment: LatexEnvironment, descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean
    )
}