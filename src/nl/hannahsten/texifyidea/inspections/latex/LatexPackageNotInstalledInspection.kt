package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.run.latex.LatexDistribution
import nl.hannahsten.texifyidea.util.*

class LatexPackageNotInstalledInspection : TexifyInspectionBase() {
    override val inspectionGroup: InsightGroup = InsightGroup.LATEX

    override val inspectionId: String =
            "PackageNotInstalled"

    override fun getDisplayName(): String {
        return "Package is not installed"
    }

    override fun isEnabledByDefault(): Boolean {
        return LatexDistribution.isTexlive
    }

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()
        if (LatexDistribution.isTexlive) {
            val installedPackages = TexLivePackages.packageList
            val customPackages = LatexDefinitionIndex.getCommandsByName("\\ProvidesPackage", file.project, file.project
                            .projectSearchScope)
                    .map { it.requiredParameter(0) }
                    .map { it?.toLowerCase() }
            val packages = installedPackages + customPackages

            val commands = file.childrenOfType(LatexCommands::class)
                    .filter { it.name == "\\usepackage" || it.name == "\\RequirePackage" }

            for (command in commands) {
                val `package` = command.requiredParameters.first().toLowerCase()
                if (`package` !in packages) {
                    // Manually check if the package is installed (e.g. rubikrotation is listed as rubik, so we need to check it separately).
                    if ("tlmgr search --file /$`package`.sty".runCommand()
                                    ?.isEmpty() != false) {
                        descriptors.add(manager.createProblemDescriptor(
                                command,
                                "Package is not installed",
                                InstallPackage(SmartPointerManager.getInstance(file.project).createSmartPsiElementPointer(file), `package`),
                                ProblemHighlightType.WARNING,
                                isOntheFly
                        ))
                    }
                }
            }
        }
        return descriptors
    }

    private class InstallPackage(val filePointer: SmartPsiElementPointer<PsiFile>, val packageName: String) : LocalQuickFix {
        override fun getFamilyName(): String = "Install $packageName"

        /**
         * Install the package in the background and add it to the list of installed
         * packages when done.
         */
        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            ProgressManager.getInstance()
                    .run(object : Task.Backgroundable(project, "Installing $packageName...") {
                        override fun run(indicator: ProgressIndicator) {
                            val tlname = TexLivePackages.findTexLiveName(this, packageName)

                            if (tlname == null) {
                                Notification(
                                        "Package Not Installed",
                                        "Package $packageName not installed",
                                        "Package $packageName was not installed because tlmgr could not find $packageName.sty anywhere. Try to install the package manually.",
                                        NotificationType.ERROR
                                ).notify(project)

                                indicator.cancel()
                            }

                            title = "Installing $packageName..."
                            "tlmgr install $tlname".runCommand()
                        }

                        override fun onSuccess() {
                            TexLivePackages.packageList.add(packageName)
                            DaemonCodeAnalyzer.getInstance(project)
                                    .restart(filePointer.containingFile
                                            ?: return)
                        }

                    })
        }

    }
}