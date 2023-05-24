package nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages

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
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.TexLivePackages
import nl.hannahsten.texifyidea.util.projectSearchScope
import nl.hannahsten.texifyidea.util.psi.childrenOfType
import nl.hannahsten.texifyidea.util.psi.requiredParameter
import nl.hannahsten.texifyidea.util.runCommand
import java.util.*

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

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()
        // We have to check whether tlmgr is installed, for those users who don't want to install TeX Live in the official way
        if (LatexSdkUtil.isTlmgrAvailable(file.project)) {
            val installedPackages = TexLivePackages.packageList
            val customPackages = LatexDefinitionIndex.getCommandsByName(
                "\\ProvidesPackage", file.project,
                file.project
                    .projectSearchScope
            )
                .map { it.requiredParameter(0) }
                .mapNotNull { it?.lowercase(Locale.getDefault()) }
            val packages = installedPackages + customPackages

            val commands = file.childrenOfType(LatexCommands::class)
                .filter { it.name == "\\usepackage" || it.name == "\\RequirePackage" }

            for (command in commands) {
                val `package` = command.getRequiredParameters().firstOrNull()?.lowercase(Locale.getDefault()) ?: continue
                if (`package` !in packages) {
                    // Use the cache or check if the file reference resolves (in the same way we resolve for the gutter icon).
                    if (
                        knownNotInstalledPackages.contains(`package`) ||
                        command.references.filterIsInstance<InputFileReference>().mapNotNull { it.resolve() }.isEmpty()
                    ) {
                        descriptors.add(
                            manager.createProblemDescriptor(
                                command,
                                "Package is not installed or \\ProvidesPackage is missing",
                                InstallPackage(
                                    SmartPointerManager.getInstance(file.project).createSmartPsiElementPointer(file),
                                    `package`,
                                    knownNotInstalledPackages
                                ),
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                isOntheFly
                            )
                        )
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
            val tlmgrExecutable = LatexSdkUtil.getExecutableName("tlmgr", project)

            ProgressManager.getInstance()
                .run(object : Task.Backgroundable(project, "Installing $packageName...") {
                    override fun run(indicator: ProgressIndicator) {
                        val tlname = TexLivePackages.findTexLiveName(this, packageName, project)

                        if (tlname == null) {
                            Notification(
                                "LaTeX",
                                "Package $packageName not installed",
                                "Package $packageName was not installed because tlmgr could not find $packageName.sty anywhere. Try to install the package manually.",
                                NotificationType.ERROR
                            ).notify(project)

                            indicator.cancel()
                        }

                        title = "Installing $packageName..."
                        val output = "$tlmgrExecutable install $tlname".runCommand()
                        if (output?.contains("tlmgr update --self") == true) {
                            title = "Updating tlmgr..."
                            "$tlmgrExecutable update --self".runCommand()
                            title = "Installing $packageName..."
                            "$tlmgrExecutable install $tlname".runCommand()
                        }
                    }

                    override fun onSuccess() {
                        TexLivePackages.packageList.add(packageName)
                        // Rerun inspections
                        DaemonCodeAnalyzer.getInstance(project)
                            .restart(
                                filePointer.containingFile
                                    ?: return
                            )
                    }
                })
        }
    }
}