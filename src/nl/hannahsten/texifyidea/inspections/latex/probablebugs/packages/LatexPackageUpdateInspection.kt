package nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.settings.sdk.TexliveSdk
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.childrenOfType
import nl.hannahsten.texifyidea.util.parser.requiredParameter
import nl.hannahsten.texifyidea.util.runCommand
import nl.hannahsten.texifyidea.util.runCommandWithExitCode

/**
 * Check for available updates for LaTeX packages.
 * Also see [LatexPackageNotInstalledInspection].
 */
class LatexPackageUpdateInspection : TexifyInspectionBase() {

    object Cache {
        /** Map package name to old and new revision number */
        var availablePackageUpdates = mapOf<String, Pair<String?, String?>>()
    }

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "PackageUpdate"

    override fun getDisplayName() = "Package has an update available"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        if (!LatexSdkUtil.isTlmgrAvailable(file.project) || !TexliveSdk.Cache.isAvailable) return emptyList()

        if (Cache.availablePackageUpdates.isEmpty()) {
            val tlmgrExecutable = LatexSdkUtil.getExecutableName("tlmgr", file.project)
            val result = runCommand(tlmgrExecutable, "update", "--list") ?: return emptyList()
            Cache.availablePackageUpdates = """update:\s*(?<package>[^ ]+).*local:\s*(?<local>\d+), source:\s*(?<source>\d+)""".toRegex()
                .findAll(result)
                .mapNotNull { Pair(it.groups["package"]?.value ?: return@mapNotNull null, Pair(it.groups["local"]?.value, it.groups["source"]?.value)) }
                .associate { it }
        }

        return file.childrenOfType<LatexCommands>()
            .filter { it.name in CommandMagic.packageInclusionCommands }
            .filter { it.requiredParameter(0) in Cache.availablePackageUpdates.keys }
            .mapNotNull {
                val packageName = it.requiredParameter(0) ?: return@mapNotNull null
                val packageVersions = Cache.availablePackageUpdates[packageName] ?: return@mapNotNull null
                manager.createProblemDescriptor(
                    it,
                    "Update available for package $packageName",
                    arrayOf(
                        UpdatePackage(SmartPointerManager.getInstance(file.project).createSmartPsiElementPointer(file), packageName, packageVersions.first, packageVersions.second),
                        UpdatePackage(SmartPointerManager.getInstance(file.project).createSmartPsiElementPointer(file), "--all", null, null),
                    ),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly,
                    false,
                )
            }
    }

    private class UpdatePackage(val filePointer: SmartPsiElementPointer<PsiFile>, val packageName: String, val old: String?, val new: String?) : LocalQuickFix {

        override fun getFamilyName(): String = if (packageName == "--all") "Update all packages" else if (old != null && new != null) "Update $packageName from revision $old to revision $new" else "Update $packageName"

        override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo {
            // Nothing is modified
            return IntentionPreviewInfo.Html("Run tlngr update $packageName")
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val message = if (packageName == "--all") "Updating all packages" else "Updating $packageName..."
            ProgressManager.getInstance().run(object : Backgroundable(project, message) {
                override fun run(indicator: ProgressIndicator) {
                    val tlmgrExecutable = LatexSdkUtil.getExecutableName("tlmgr", project)

                    val timeout: Long = if (packageName == "--all") 600 else 15
                    var (output, exitCode) = runCommandWithExitCode(tlmgrExecutable, "update", packageName, returnExceptionMessage = true, timeout = timeout)
                    if (output?.contains("tlmgr update --self") == true) {
                        val (tlmgrOutput, tlmgrExitCode) = runCommandWithExitCode(tlmgrExecutable, "update", "--self", returnExceptionMessage = true, timeout = 20)
                        if (tlmgrExitCode != 0) {
                            Notification(
                                "LaTeX",
                                "Package $packageName not updated",
                                "Could not update tlmgr: $tlmgrOutput",
                                NotificationType.ERROR
                            ).notify(project)
                            indicator.cancel()
                        }
                        title = message
                        val (secondOutput, secondExitCode) = runCommandWithExitCode(tlmgrExecutable, "update", packageName, returnExceptionMessage = true, timeout = timeout)
                        output = secondOutput
                        exitCode = secondExitCode
                    }

                    if (exitCode != 0) {
                        Notification(
                            "LaTeX",
                            if (packageName == "--all") "Could not update packages" else "Package $packageName not updated",
                            "Could not update $packageName${if (exitCode == 143) " due to a timeout" else ""}: $output",
                            NotificationType.ERROR
                        ).notify(project)
                        indicator.cancel()
                    }
                }

                override fun onSuccess() {
                    // Clear cache, since we changed something
                    Cache.availablePackageUpdates = mapOf()
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