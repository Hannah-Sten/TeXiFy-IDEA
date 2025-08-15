package nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.LSemanticEntity
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.predefined.AllPredefined
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import nl.hannahsten.texifyidea.psi.nameWithoutSlash
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
        if (!TexifySettings.Companion.getState().automaticDependencyCheck) {
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