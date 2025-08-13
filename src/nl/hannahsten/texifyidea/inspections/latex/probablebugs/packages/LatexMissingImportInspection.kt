package nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LSemanticEnv
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.PackageUtils
import java.util.*

/**
 * Missing import inspection for LaTeX files, works on predefined commands and environments.
 *
 * @author Hannah Schellekens
 */
class LatexMissingImportInspection : LatexMissingImportInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "MissingImport"

    override val ignoredSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "Missing imports"

    override fun reportCommandMissingImport(
        command: LatexCommands, candidates: List<LSemanticCommand>,
        descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean
    ) {
        val packageNames = candidates.mapNotNull { it.dependency.toPackageName() }
        if (packageNames.isEmpty()) return
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

    override fun reportEnvironmentMissingImport(
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

    override fun reportUnknownCommand(command: LatexCommands, descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean) {
    }

    override fun reportUnknownEnvironment(name: String, environment: LatexEnvironment, descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean) {
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