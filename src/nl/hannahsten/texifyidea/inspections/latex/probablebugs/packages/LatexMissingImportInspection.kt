package nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LSemanticEnv
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.lang.predefined.AllPredefinedCommands
import nl.hannahsten.texifyidea.lang.predefined.AllPredefinedEnvironments
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.PackageUtils
import nl.hannahsten.texifyidea.util.parser.*
import java.util.*

/**
 * Currently works for built-in commands and environments.
 *
 * @author Hannah Schellekens
 */
open class LatexMissingImportInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "MissingImport"

    override val ignoredSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "Missing imports"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        if (!TexifySettings.getInstance().automaticDependencyCheck) {
            return emptyList()
        }
        val project = file.project
        if (!LatexProjectStructure.isProjectFilesetsAvailable(project)) {
            return emptyList()
        }
        val bundle = LatexDefinitionService.getInstance(project).getDefBundlesMerged(file)

        val descriptors = descriptorList()
        file.traverse().forEach {
            when (it) {
                is LatexCommands -> analyzeCommand(it, bundle, descriptors, manager, isOntheFly)
                is LatexEnvironment -> analyzeEnvironment(it, bundle, descriptors, manager, isOntheFly)
            }
        }

        return descriptors
    }

    private fun String.toPackageName(): String? {
        return if (endsWith(".sty")) substring(0, length - 4)
        else null
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

    protected open fun reportUnknownCommand(
        command: LatexCommands, descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean
    ) {
        descriptors.add(
            manager.createProblemDescriptor(
                command,
                TextRange(0, command.commandToken.textLength),
                "Unknown command: ${command.name}",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOntheFly
            )
        )
    }

    protected open fun reportCommandMissingImport(
        command: LatexCommands, candidates: List<LSemanticCommand>,
        descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean
    ) {
        val packageNames = candidates.mapNotNull { it.dependency.toPackageName() }
        if(packageNames.isEmpty()) return
        val fixes = packageNames.map { ImportPackageFix(it) }.toTypedArray()
        val range = TextRange(0, command.commandToken.textLength)
        descriptors.add(
            manager.createProblemDescriptor(
                command,
                range,
                "Command requires any of the packages: ${packageNames.joinToString(", ")}",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOntheFly,
                *fixes
            )
        )
    }

    protected open fun reportUnknownEnvironment(
        name: String, environment: LatexEnvironment, descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean
    ) {
        descriptors.add(
            manager.createProblemDescriptor(
                environment,
                TextRange(7, 7 + name.length),
                "Unknown environment: $name",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOntheFly,
            )
        )
    }

    protected open fun reportEnvironmentMissingImport(
        environment: LatexEnvironment, requiredEntity: LSemanticEnv,
        descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean
    ) {
        val pack = requiredEntity.dependency.toPackageName() ?: return
        descriptors.add(
            manager.createProblemDescriptor(
                environment,
                TextRange(7, 7 + requiredEntity.name.length),
                "Environment requires package $pack",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOntheFly,
                ImportPackageFix(pack)
            )
        )
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

    /**
     * @author Hannah Schellekens
     */
    private class ImportPackageFix(val packName: String) : LocalQuickFix {

        override fun getFamilyName() = "Add import for package '$packName' which provides this environment"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            if (!PackageUtils.insertUsepackage(descriptor.psiElement.containingFile, LatexPackage(packName))) {
                Notification(
                    "LaTeX", "Conflicting package detected",
                    "The package $packName was not inserted because a conflicting package was detected.", NotificationType.INFORMATION
                ).notify(project)
            }
        }
    }
}