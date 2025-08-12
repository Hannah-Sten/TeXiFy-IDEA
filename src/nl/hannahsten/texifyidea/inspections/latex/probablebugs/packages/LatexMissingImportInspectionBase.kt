package nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LSemanticEnv
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.predefined.AllPredefinedCommands
import nl.hannahsten.texifyidea.lang.predefined.AllPredefinedEnvironments
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import nl.hannahsten.texifyidea.util.parser.traverse

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
abstract class LatexMissingImportInspectionBase : TexifyInspectionBase() {

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        if (!TexifySettings.Companion.getInstance().automaticDependencyCheck) {
            return emptyList()
        }
        val project = file.project
        if (!LatexProjectStructure.isProjectFilesetsAvailable(project)) {
            return emptyList()
        }
        val bundle = LatexDefinitionService.Companion.getInstance(project).getDefBundlesMerged(file)

        val descriptors = descriptorList()
        file.traverse().forEach {
            when (it) {
                is LatexCommands -> analyzeCommand(it, bundle, descriptors, manager, isOntheFly)
                is LatexEnvironment -> analyzeEnvironment(it, bundle, descriptors, manager, isOntheFly)
            }
        }

        return descriptors
    }

    private fun analyzeCommand(command: LatexCommands, bundle: DefinitionBundle, descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean) {
        val name = command.nameWithoutSlash ?: return
        if (bundle.lookupCommand(name) != null) return

        val contexts = LatexPsiUtil.resolveContextUpward(command, bundle)
        if (LatexContexts.CommandDeclaration in contexts) return // skip declaration of command

        val candidates = AllPredefinedCommands.findAll(name)
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
        val candidate = AllPredefinedEnvironments.lookupEnv(name)
        if (candidate == null) {
            reportUnknownEnvironment(name, e, descriptors, manager, isOntheFly)
        }
        else {
            reportEnvironmentMissingImport(e, candidate, descriptors, manager, isOntheFly)
        }
    }

    protected abstract fun reportCommandMissingImport(
        command: LatexCommands, candidates: List<LSemanticCommand>,
        descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean
    )

    protected abstract fun reportEnvironmentMissingImport(
        environment: LatexEnvironment, requiredEntity: LSemanticEnv,
        descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean
    )

    protected abstract fun reportUnknownCommand(
        command: LatexCommands, descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean
    )

    protected abstract fun reportUnknownEnvironment(
        name: String, environment: LatexEnvironment, descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean
    )
}