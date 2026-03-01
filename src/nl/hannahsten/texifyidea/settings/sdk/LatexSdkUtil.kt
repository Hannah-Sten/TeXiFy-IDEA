package nl.hannahsten.texifyidea.settings.sdk

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil.getExecutableName
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil.getLatexSdkForFile
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil.resolveSdkPath
import nl.hannahsten.texifyidea.util.getLatexRunConfigurations
import nl.hannahsten.texifyidea.util.runCommand
import nl.hannahsten.texifyidea.util.runCommandWithExitCode
import java.io.File

/**
 * Type alias for SDK path identifiers used as cache keys.
 * This represents the SDK home path or a resolved kpsewhich location
 * that uniquely identifies a LaTeX distribution.
 */
typealias SdkPath = String

/**
 * Utility functions for working with LaTeX SDKs and distributions.
 *
 * ## SDK Resolution
 *
 * This class supports both project-level and module-level SDK configuration.
 * When resolving SDKs using file-context-aware methods, the lookup order is:
 * 1. Module SDK (if the file belongs to a module with a LaTeX SDK configured)
 * 2. Project SDK (if it's a LaTeX SDK)
 * 3. PATH-based fallback (pdflatex in system PATH)
 *
 * ## Key Methods
 *
 * - [getExecutableName]: Get the full path to a LaTeX executable. Use the file-aware overloads
 *   to correctly resolve module-specific SDKs.
 * - [resolveSdkPath]: Get an SDK path identifier for use as a cache key. This correctly
 *   distinguishes between different SDK installations.
 * - [getLatexSdkForFile]: Get the SDK instance for a file, respecting module configuration.
 *
 * ## IDEs Without SDK Support
 *
 * In IDEs that don't support SDK configuration (e.g., PyCharm), or when no SDK is configured,
 * the methods fall back to checking if LaTeX is available in the system PATH.
 */
object LatexSdkUtil {

    val isPdflatexInPath: Boolean by lazy {
        "pdflatex --version".runCommand()?.contains("pdfTeX") == true
    }

    val pdflatexVersionText: String by lazy {
        getDistribution()
    }

    /**
     * Whether the user is using MikTeX or not.
     * This value is lazy, so only computed when first accessed, because it is unlikely that the user will change LaTeX distribution while using IntelliJ.
     */
    val isMiktexAvailable: Boolean by lazy {
        pdflatexVersionText.contains("MiKTeX")
    }

    private val isWslTexliveAvailable: Boolean by lazy {
        WslPathUtil.isWslTexliveAvailable
    }

    val isTlmgrInstalled: Boolean by lazy {
        "tlmgr --version".runCommand()?.contains("TeX Live") == true
    }

    // Cache for below function
    private var isTlgmrAvailable: Boolean? = null

    /**
     * Contrary to e.g. with pdflatex, we want to fail silently if tlmgr is not available.
     * Therefore we check if it really is available after getting the executable name/path.
     */
    fun isTlmgrAvailable(project: Project): Boolean {
        if (isTlmgrInstalled) {
            isTlgmrAvailable = true
            return true
        }
        isTlgmrAvailable?.let { return it }
        // If not set, find out if it is available
        val tlmgrExecutable = getExecutableName("tlmgr", project)
        isTlgmrAvailable = "$tlmgrExecutable --version".runCommand()?.contains("TeX Live") == true
        return isTlgmrAvailable!!
    }

    fun isAvailable(type: LatexDistributionType, project: Project): Boolean {
        if (type == LatexDistributionType.PROJECT_SDK && getLatexProjectSdk(project) != null) return true
        if (type == LatexDistributionType.MODULE_SDK && getAllLatexSdks().isNotEmpty()) return true
        if (type == LatexDistributionType.MIKTEX && isMiktexAvailable) return true
        if (type == LatexDistributionType.TEXLIVE && TexliveSdk.Cache.isAvailable) return true
        if (type == LatexDistributionType.DOCKER_MIKTEX && DockerSdk.Availability.isAvailable) return true
        if (type == LatexDistributionType.DOCKER_TEXLIVE && DockerSdk.Availability.isAvailable) return true
        if (type == LatexDistributionType.WSL_TEXLIVE && isWslTexliveAvailable) return true
        return false
    }

    /**
     * Given the path to the LaTeX home, find the parent path of the executables, e.g. /bin/x86_64-linux/
     */
    fun getPdflatexParentPath(homePath: String) = File("$homePath/bin").listFiles()?.firstOrNull()?.path

    /**
     * Run pdflatex in the given directory and check if it is present and valid.
     */
    fun isPdflatexPresent(directory: String?): Boolean {
        // .exe is optional on windows
        val output = runCommandWithExitCode("$directory${File.separator}pdflatex", "--version", returnExceptionMessage = true).first ?: "No output given by $directory${File.separator}pdflatex --version"

        return output.contains("pdfTeX")
    }

    /**
     * Find the full name of the distribution in use, e.g. TeX Live 2019.
     */
    private fun getDistribution(): String {
        // Could be improved by using the (project-level) LaTeX SDK if pdflatex is not in PATH
        return parsePdflatexOutput(runCommand("pdflatex", "--version") ?: "")
    }

    /**
     * Parse the output of pdflatex --version and return the distribution.
     * Assumes the distribution name is in brackets at the end of the first line.
     */
    fun parsePdflatexOutput(output: String): String {
        val firstLine = output.split("\n")[0]
        val splitLine = firstLine.split("(", ")")

        // Get one-to-last entry, as the last one will be empty after the closing )
        return if (splitLine.size >= 2) {
            splitLine[splitLine.size - 2]
        }
        else {
            ""
        }
    }

    /**
     * Get executable name for a specific file context, respecting module SDK configuration.
     *
     * This method correctly distinguishes between different SDKs of the same distribution type
     * (e.g., TeX Live 2023 vs TeX Live 2024 in different modules) by using the SDK directly
     * rather than going through distribution type.
     *
     * @param executableName The base executable name (e.g., "pdflatex")
     * @param file The file context to determine the module SDK. If null, falls back to project SDK.
     * @param project The current project
     */
    fun getExecutableName(executableName: String, file: VirtualFile?, project: Project): String {
        // Get the SDK directly to preserve the specific SDK instance (not just distribution type)
        val sdk = if (file != null) {
            getLatexSdkForFile(file, project)
        }
        else {
            getLatexProjectSdk(project)
        }

        // If we have a valid SDK with a home path, use it directly
        if (sdk?.homePath != null) {
            val sdkType = sdk.sdkType as? LatexSdk
            sdkType?.getExecutableName(executableName, sdk.homePath!!)?.let { return it }
        }

        // Fall back to distribution-type-based resolution (for PATH fallback, run config paths, etc.)
        val distributionType = sdk?.let { (it.sdkType as? LatexSdk)?.getLatexDistributionType(it) }
        return getExecutableName(executableName, project, latexDistributionType = distributionType)
    }

    /**
     * Get executable name for a specific file context, respecting module SDK configuration.
     *
     * @param executableName The base executable name (e.g., "pdflatex")
     * @param psiFile The file context to determine the module SDK. If null, falls back to project SDK.
     * @param project The current project (required when psiFile is null)
     */
    fun getExecutableName(executableName: String, psiFile: PsiFile?, project: Project): String = getExecutableName(executableName, psiFile?.virtualFile, project)

    /**
     * Get executable name for a specific file context, respecting module SDK configuration.
     * Convenience overload when project can be inferred from the PsiFile.
     *
     * @param executableName The base executable name (e.g., "pdflatex")
     * @param psiFile The file context to determine the module SDK
     */
    fun getExecutableName(executableName: String, psiFile: PsiFile): String = getExecutableName(executableName, psiFile.virtualFile, psiFile.project)

    /**
     * Get executable name of pdflatex, which in case it is not in PATH may be prefixed by the full path (or even by a docker command).
     *
     * This is the low-level method that resolves executables by distribution type. It is primarily used
     * by run configurations where the distribution type is explicitly specified, and as a fallback
     * when no SDK is configured.
     *
     * **Note:** When a file context is available, prefer the overloads that accept [VirtualFile] or [PsiFile],
     * as they correctly resolve module-specific SDKs. This method cannot distinguish between two SDKs
     * of the same distribution type (e.g., TeX Live 2023 vs 2024) because it only knows the distribution type,
     * not the specific SDK instance.
     *
     * @param executableName The base executable name (e.g., "pdflatex")
     * @param project The current project (used for project SDK fallback)
     * @param latexSdk If available, its homepath will be used to find the path to the executable
     * @param latexDistributionType The resolved distribution type, used in case the latexSdk is not available. When null, falls back to the project SDK's
     *        distribution type. Callers from run configurations should pass a concrete type (TEXLIVE, MIKTEX, etc.),
     *        not MODULE_SDK or PROJECT_SDK - those should be resolved by the caller first.
     *        When no distribution type can be determined, the function falls back to checking PATH.
     */
    fun getExecutableName(executableName: String, project: Project, latexSdk: Sdk? = null, latexDistributionType: LatexDistributionType? = null): String {
        if (latexSdk != null && latexSdk.homePath != null) {
            (latexSdk.sdkType as? LatexSdk)?.getExecutableName(executableName, latexSdk.homePath!!)?.let { return it }
        }

        // Resolve the effective distribution type from project SDK only.
        // We don't fall back to arbitrary module SDKs here since different modules may have
        // different SDK types configured - use the file-aware overloads when a file context is available.
        val effectiveDistributionType = latexDistributionType ?: getLatexDistributionType(project)

        // Prefixing the LaTeX compiler is not relevant for Docker distributions (perhaps the path to the docker executable)
        if (effectiveDistributionType?.isDocker() == true) return executableName

        // For concrete distribution types, try to find a matching SDK or use default paths
        if (effectiveDistributionType != null) {
            // First, try to find a configured SDK of this distribution type
            val matchingSdk = getAllLatexSdks().find { sdk ->
                (sdk.sdkType as? LatexSdk)?.getLatexDistributionType(sdk) == effectiveDistributionType
            }
            if (matchingSdk?.homePath != null) {
                (matchingSdk.sdkType as? LatexSdk)?.getExecutableName(executableName, matchingSdk.homePath!!)?.let { return it }
            }

            // If no SDK configured, try the default paths for this distribution type
            val sdkType = getSdkTypeForDistribution(effectiveDistributionType)
            if (sdkType != null) {
                val homePath = sdkType.suggestHomePaths().firstOrNull { sdkType.isValidSdkHome(it) }
                if (homePath != null) {
                    return sdkType.getExecutableName(executableName, homePath)
                }
            }
        }

        // If not, if it's in PATH then that also works
        if (isPdflatexInPath) {
            return executableName
        }

        // Maybe we're on a Mac but in a non-IntelliJ IDE, in which case the user provided the path to pdflatex in the run config (as it's not possible to configure an SDK)
        project.getLatexRunConfigurations().mapNotNull { it.primaryCompilerPath()?.substringBefore("/pdflatex") }.forEach {
            val file = File(it, executableName)
            if (file.isFile) return file.path
        }

        // Last resort: just return the executable name and hope it's in PATH
        return executableName
    }

    /**
     * Get the SDK type class for a given distribution type.
     */
    private fun getSdkTypeForDistribution(distributionType: LatexDistributionType): LatexSdk? = when (distributionType) {
        LatexDistributionType.TEXLIVE -> TexliveSdk()
        LatexDistributionType.MIKTEX -> if (SystemInfo.isWindows) MiktexWindowsSdk() else MiktexLinuxSdk()
        LatexDistributionType.WSL_TEXLIVE -> WslTexliveSdk()
        else -> null
    }

    /**
     * Get all configured LaTeX SDKs from the application-level SDK table.
     * This returns all SDKs whose type extends [LatexSdk].
     */
    fun getAllLatexSdks(): List<Sdk> = ProjectJdkTable.getInstance().allJdks.filter { it.sdkType is LatexSdk }

    /**
     * If a LaTeX SDK is selected as project SDK, return it, otherwise return null.
     */
    fun getLatexProjectSdk(project: Project): Sdk? {
        val sdk = ProjectRootManager.getInstance(project).projectSdk
        if (sdk?.sdkType is LatexSdk) {
            return sdk
        }
        return null
    }

    /**
     * Get the LaTeX SDK for a specific module.
     * Returns null if the module has no SDK or the SDK is not a LaTeX SDK.
     */
    fun getLatexModuleSdk(module: Module): Sdk? {
        val sdk = ModuleRootManager.getInstance(module).sdk
        if (sdk?.sdkType is LatexSdk) {
            return sdk
        }
        return null
    }

    /**
     * Get the LaTeX SDK for the given file, checking module SDK first, then falling back to project SDK.
     * This supports per-module SDK configuration for multi-module projects.
     *
     * @param file The file to get the SDK for
     * @param project The project containing the file
     * @return The LaTeX SDK for the file's module, or the project SDK, or null if neither is a LaTeX SDK
     */
    fun getLatexSdkForFile(file: VirtualFile, project: Project): Sdk? {
        // First try to get module-specific SDK
        val module = ModuleUtilCore.findModuleForFile(file, project)
        if (module != null) {
            getLatexModuleSdk(module)?.let { return it }
        }
        // Fall back to project SDK
        return getLatexProjectSdk(project)
    }

    /**
     * Get the LaTeX SDK for the given PsiFile, checking module SDK first, then falling back to project SDK.
     * This supports per-module SDK configuration for multi-module projects.
     *
     * @param psiFile The PsiFile to get the SDK for
     * @return The LaTeX SDK for the file's module, or the project SDK, or null if neither is a LaTeX SDK
     */
    fun getLatexSdkForFile(psiFile: PsiFile): Sdk? {
        val virtualFile = psiFile.virtualFile ?: return getLatexProjectSdk(psiFile.project)
        return getLatexSdkForFile(virtualFile, psiFile.project)
    }

    /**
     * Get type of project SDK. If null or not a LaTeX sdk, return null.
     */
    fun getLatexProjectSdkType(project: Project): LatexSdk? = getLatexProjectSdk(project)?.sdkType as? LatexSdk

    /**
     * Get the LaTeX SDK type for the given file, checking module SDK first, then falling back to project SDK.
     */
    fun getLatexSdkTypeForFile(file: VirtualFile, project: Project): LatexSdk? = getLatexSdkForFile(file, project)?.sdkType as? LatexSdk

    fun getLatexDistributionType(project: Project): LatexDistributionType? {
        val sdk = getLatexProjectSdk(project) ?: return null
        return (sdk.sdkType as? LatexSdk)?.getLatexDistributionType(sdk)
    }

    /**
     * Resolve an SDK home path identifier for the given file context.
     * This can be used as a stable cache key for SDK-specific caches.
     *
     * Returns the SDK home path if a LaTeX SDK is configured for the file's module or project.
     * Otherwise, if pdflatex is in PATH, resolves the kpsewhich location as a stable identifier.
     *
     * @param file The file context to determine which SDK to use. If null, falls back to project SDK.
     * @param project The current project.
     * @return The SDK path identifier, or null if no LaTeX distribution can be found.
     */
    fun resolveSdkPath(file: VirtualFile?, project: Project): SdkPath? {
        // Use getLatexSdkForFile which correctly handles module SDK -> project SDK fallback
        val sdk = if (file != null) {
            getLatexSdkForFile(file, project)
        }
        else {
            getLatexProjectSdk(project)
        }

        // If we have an SDK with a home path, use that
        sdk?.homePath?.let { return it }

        // Otherwise, try to resolve kpsewhich from PATH and use its location as a key
        if (isPdflatexInPath) {
            val kpsewhichPath = if (SystemInfo.isWindows) {
                runCommand("where", "kpsewhich", timeout = 5)
            }
            else {
                runCommand("which", "kpsewhich", timeout = 5)
            }
            kpsewhichPath?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
        }

        return null
    }

    /**
     * Get the LaTeX distribution type for the given file, checking module SDK first, then falling back to project SDK.
     */
    fun getLatexDistributionTypeForFile(file: VirtualFile, project: Project): LatexDistributionType? {
        val sdk = getLatexSdkForFile(file, project) ?: return null
        return (sdk.sdkType as? LatexSdk)?.getLatexDistributionType(sdk)
    }

    /**
     * Collect SDK source paths, so paths to texmf-dist/source/latex, based on Project SDK if available (combining the default
     * for the SDK type and any user-added source roots) and otherwise on a random guess (ok not really).
     *
     * @param getRoots Given an sdk type and a home path, return a list of source roots.
     */
    fun getSdkSourceRoots(project: Project, getRoots: (LatexSdk, String) -> VirtualFile?): Set<VirtualFile> {
        // Get user provided and default source roots
        getLatexProjectSdk(project)?.let { sdk ->
            if (sdk.sdkType !is LatexSdk) return@let
            val userProvided = sdk.rootProvider.getFiles(OrderRootType.SOURCES).toSet()
            val default = if (sdk.homePath != null) setOf(getRoots(sdk.sdkType as LatexSdk, sdk.homePath!!)).filterNotNull() else emptySet()
            return userProvided + default
        }

        // If no sdk is known, guess something
        for (sdkType in setOf(TexliveSdk(), NativeTexliveSdk(), MiktexWindowsSdk())) {
            val roots = sdkType.suggestHomePaths().mapNotNull { homePath -> getRoots(sdkType, homePath) }.toSet()
            if (roots.isNotEmpty()) return roots
        }
        return emptySet()
    }
}
