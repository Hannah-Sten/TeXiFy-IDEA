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
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.DefaultEnvironment
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.DEFAULT
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.PackageUtils
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.definitionsAndRedefinitionsInFileSet
import nl.hannahsten.texifyidea.util.findCommandDefinitions
import nl.hannahsten.texifyidea.util.includedPackages
import nl.hannahsten.texifyidea.util.magic.PackageMagic
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

        val descriptors = descriptorList()

        val includedPackages = file.includedPackages()
        analyseCommands(file, includedPackages, descriptors, manager, isOntheFly)
        analyseEnvironments(file, includedPackages, descriptors, manager, isOntheFly)

        return descriptors
    }

    private fun analyseEnvironments(
        file: PsiFile, includedPackages: Collection<LatexPackage>,
        descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager,
        isOntheFly: Boolean
    ) {
        val environments = file.childrenOfType(LatexEnvironment::class)
        val defined = file.definitionsAndRedefinitionsInFileSet().asSequence()
            .filter { it.isEnvironmentDefinition() }
            .mapNotNull { it.requiredParameter(0) }
            .toSet()

        outerLoop@ for (env in environments) {
            // Don't consider environments that have been defined.
            if (env.name()?.text in defined) {
                continue
            }

            val name = env.name()?.text ?: continue
            val environment = DefaultEnvironment[name] ?: continue
            val pack = environment.dependency

            if (pack == DEFAULT || includedPackages.contains(pack)) {
                continue
            }

            // Packages included in other packages
            for (packageInclusion in PackageMagic.packagesLoadingOtherPackages) {
                if (packageInclusion == pack && includedPackages.contains(packageInclusion.key)) {
                    continue@outerLoop
                }
            }

            descriptors.add(
                manager.createProblemDescriptor(
                    env,
                    TextRange(7, 7 + name.length),
                    "Environment requires ${pack.name} package",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly,
                    ImportEnvironmentFix(pack.name)
                )
            )
        }
    }

    private fun analyseCommands(
        file: PsiFile, includedPackages: Collection<LatexPackage>,
        descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager,
        isOntheFly: Boolean
    ) {
        // This loops over all commands, so we don't want to do this again for every command in the file for performance
        val commandDefinitionsInProject = file.project.findCommandDefinitions().map { it.definedCommandName() }

        val commands = file.commandsInFile()
        commandLoop@ for (command in commands) {
            // If we are actually defining the command, then it doesn't need any dependency
            if (command.parent.firstParentOfType(LatexCommands::class).isCommandDefinition()) {
                continue
            }

            // If defined within the project, also fine
            if (commandDefinitionsInProject.contains(command.name)) {
                continue
            }

            val name = command.commandToken.text.substring(1)
            val latexCommands = LatexCommand.lookup(name) ?: continue

            // In case there are multiple commands with this name, we don't know which one the user wants.
            // So we don't know which of the dependencies the user needs: we assume that if at least one of them is present it will be the right one.
            val dependencies = latexCommands.map { it.dependency }.toSet()

            if (dependencies.isEmpty() || dependencies.any { it.isDefault }) {
                continue
            }

            // Packages included in other packages
            for (packageInclusion in PackageMagic.packagesLoadingOtherPackages) {
                if (packageInclusion.value.intersect(dependencies).isNotEmpty() && includedPackages.contains(packageInclusion.key)) {
                    continue@commandLoop
                }
            }

            // If none of the dependencies are included
            if (includedPackages.toSet().intersect(dependencies).isEmpty()) {
                // We know dependencies is not empty
                val range = TextRange(0, latexCommands.minByOrNull { it.command.length }!!.command.length + 1)
                val dependencyNames = dependencies.joinToString { it.name }.replaceAfterLast(", ", "or ${dependencies.last().name}")
                val fixes = dependencies.map { ImportCommandFix(it) }.toTypedArray()
                descriptors.add(
                    manager.createProblemDescriptor(
                        command,
                        range,
                        "Command requires $dependencyNames package",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly,
                        *fixes
                    )
                )
            }
        }
    }

    /**
     * @author Hannah Schellekens
     */
    private class ImportCommandFix(val pack: LatexPackage) : LocalQuickFix {

        override fun getFamilyName() = "Add import for package '${pack.name}' which provides this command"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val file = command.containingFile

            if (!PackageUtils.insertUsepackage(file, pack)) {
                Notification("LaTeX", "Conflicting package detected", "The package ${pack.name} was not inserted because a conflicting package was detected.", NotificationType.INFORMATION).notify(project)
            }
        }
    }

    /**
     * @author Hannah Schellekens
     */
    private class ImportEnvironmentFix(val import: String) : LocalQuickFix {

        override fun getFamilyName() = "Add import for package '$import' which provides this environment"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val environment = descriptor.psiElement as? LatexEnvironment ?: return
            val thingy = DefaultEnvironment.fromPsi(environment) ?: return
            val file = environment.containingFile

            PackageUtils.insertUsepackage(file, thingy.dependency)
        }
    }
}