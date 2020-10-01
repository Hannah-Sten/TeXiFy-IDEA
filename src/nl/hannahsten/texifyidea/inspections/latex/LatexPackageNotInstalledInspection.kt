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

/**
 * Check if a LaTeX package is not installed (only for TeX Live, since MiKTeX downloads them automatically).
 */
class LatexPackageNotInstalledInspection : TexifyInspectionBase() {
    // This caches packages which are not installed, which is needed
    // otherwise we are running the expensive call to tlmgr basically on
    // every letter typed - exactly the same call with the same results
    private val knownNotInstalledPackages = mutableSetOf<String>()

    override val inspectionGroup: InsightGroup = InsightGroup.LATEX

    override val inspectionId: String =
            "PackageNotInstalled"

    override fun getDisplayName(): String {
        return "Package is not installed"
    }

    override fun isEnabledByDefault(): Boolean {
        return LatexDistribution.isTexliveAvailable
    }

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()
        // We have to check whether tlmgr is installed, for those users who don't want to install TeX Live in the official way
        if (LatexDistribution.isTexliveAvailable && SystemEnvironment.isTlmgrInstalled) {
            val installedPackages = TexLivePackages.packageList
            val customPackages = LatexDefinitionIndex.getCommandsByName("\\ProvidesPackage", file.project, file.project
                            .projectSearchScope)
                    .map { it.requiredParameter(0) }
                    .mapNotNull { it?.toLowerCase() }
            val packages = installedPackages + customPackages

            val commands = file.childrenOfType(LatexCommands::class)
                    .filter { it.name == "\\usepackage" || it.name == "\\RequirePackage" }

            for (command in commands) {
                val `package` = command.requiredParameters.firstOrNull()?.toLowerCase() ?: continue
                if (`package` !in packages) {
                    // Use the cache or manually check if the package is installed (e.g. rubikrotation is listed as rubik, so we need to check it separately).
                    if (knownNotInstalledPackages.contains(`package`) || "tlmgr search --file /$`package`.sty".runCommand()
                                    ?.isEmpty() == true) {
                        descriptors.add(manager.createProblemDescriptor(
                                command,
                                "Package is not installed or \\ProvidesPackage is missing",
                                InstallPackage(SmartPointerManager.getInstance(file.project).createSmartPsiElementPointer(file), `package`, knownNotInstalledPackages),
                                ProblemHighlightType.WARNING,
                                isOntheFly
                        ))
                        knownNotInstalledPackages.add(`package`)
                    }
                    else {
                        // Apparently the package is installed, but was not found initially by the TexLivePackageListInitializer (for example stackrel, contained in the oberdiek bundle)
                        TexLivePackages.packageList.add(`package`)
                    }
                }
            }
        }
        return descriptors
    }

    private class InstallPackage(val filePointer: SmartPsiElementPointer<PsiFile>, val packageName: String, val knownNotInstalledPackages: MutableSet<String>) : LocalQuickFix {
        override fun getFamilyName(): String = "Install $packageName"

        /**
         * Install the package in the background and add it to the list of installed
         * packages when done.
         */
        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            // I don't know if you actually could install multiple packages
            // with one fix, but it's not a bad idea to clear cache once in a while
            knownNotInstalledPackages.clear()
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
                            val output = "tlmgr install $tlname".runCommand()
                            val update = "tlmgr update --self"
                            if (output?.contains(update) == true) {
                                title = "Updating tlmgr..."
                                update.runCommand()
                                title = "Installing $packageName..."
                                "tlmgr install $tlname".runCommand()
                            }
                        }

                        override fun onSuccess() {
                            TexLivePackages.packageList.add(packageName)
                            // Rerun inspections
                            DaemonCodeAnalyzer.getInstance(project)
                                    .restart(filePointer.containingFile
                                            ?: return)
                        }
                    })
        }
    }
}