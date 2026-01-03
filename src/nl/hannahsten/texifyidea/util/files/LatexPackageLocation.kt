package nl.hannahsten.texifyidea.util.files

import arrow.atomic.AtomicInt
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.pathOrNull
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.settings.sdk.SdkPath
import nl.hannahsten.texifyidea.settings.sdk.TectonicSdk
import nl.hannahsten.texifyidea.util.AbstractBlockingCacheService
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.isTestProject
import nl.hannahsten.texifyidea.util.runCommand
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.isRegularFile
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/**
 * Cache locations of LaTeX packages in memory, because especially on Windows they can be expensive to retrieve
 * (requires a run of kpsewhich), which takes too long to do on every character typed by the user.
 * Can also be used for tex/bib files and whatever can be used with kpsewhich (probably any file that is in its search path).
 *
 * The cache is keyed by [SdkPath], so different LaTeX distributions (e.g., in different modules)
 * get separate caches. This supports multi-module projects where modules may use different SDKs.
 *
 * Extends [AbstractBlockingCacheService] for consistent caching behavior with expiration support.
 */
object LatexPackageLocation : AbstractBlockingCacheService<SdkPath, Map<String, Path>>() {

    private val EXPIRATION_TIME: Duration = 100.hours

    /** Track retries per SDK path to avoid infinite retry loops. */
    private val retries = ConcurrentHashMap<SdkPath, AtomicInt>()

    /**
     * Context for computing cache values, stored temporarily during computation.
     * This is needed because [computeValue] only receives the key.
     */
    private data class ComputeContext(val file: VirtualFile?, val project: Project)
    private val computeContexts = ConcurrentHashMap<SdkPath, ComputeContext>()

    /**
     * Computes the package location map for the given SDK path.
     * Fills the cache with all paths of all files in the LaTeX installation.
     * Note: this can take a long time.
     *
     * Uses the stored context if available.
     */
    override fun computeValue(key: SdkPath, oldValue: Map<String, Path>?): Map<String, Path> {
        if (isTestProject()) return emptyMap()

        val context = computeContexts[key]
        val retryCount = retries.computeIfAbsent(key) { AtomicInt(0) }

        // Get the executable name using the file context if available
        val executableName = if (context != null) {
            LatexSdkUtil.getExecutableName("kpsewhich", context.file, context.project)
        }
        else {
            // Fallback: just use kpsewhich directly (this path is unusual)
            "kpsewhich"
        }

        // We will get all search paths that kpsewhich has, expand them and find all files
        // Source: https://www.tug.org/texinfohtml/kpathsea.html#Casefolding-search
        // We cannot just fill the cache on the fly, because then we will also run kpsewhich when the user is still typing a package name, so we will run it once for every letter typed and this is already too expensive.
        // We cannot rely on ls-R databases because they are not always populated, and running mktexlsr may run into permission issues.
        // See NativeTexliveSdk.getDefaultStyleFilesPath which does the same thing, but for a specific file.
        // This ensures that the library root folders are consistent.
        val texPaths = runCommand(executableName, "article.cls", "plain.bst", timeout = 10)

        if (retryCount.getAndIncrement() <= 5 && texPaths.isNullOrBlank()) {
            return oldValue ?: emptyMap()
        }
        if (texPaths == null) return oldValue ?: emptyMap()

        // Parse paths like:
        // /usr/local/texlive/2025/texmf-dist/tex/latex/base/article.cls
        // /usr/local/texlive/2023/texmf-dist/bibtex/bst/base/plain.bst
        val rootFolders = texPaths.lines().mapNotNull { pathOrNull(it)?.parent?.parent?.parent }

        // /usr/local/texlive/2025/texmf-dist/tex and /usr/local/texlive/2023/texmf-dist/bibtex/bst/
        // search all the subdirectories of the root folders
        val result = mutableMapOf<String, Path>()
        rootFolders.forEach { root ->
            root.toFile().walk().map { it.toPath() }.filter { it.isRegularFile() }
                .forEach {
                    val fileName = it.fileName.toString()
                    // If the file is already in the map, we keep the first one we found
                    result.putIfAbsent(fileName, it)
                }
        }
        Log.debug("Latex package location cache generated for $key with ${result.size} paths")
        return result
    }

    /**
     * Get or compute the cache for the given context.
     */
    private fun getOrComputeCache(file: VirtualFile?, project: Project): Map<String, Path> {
        val sdkPath = LatexSdkUtil.resolveSdkPath(file, project) ?: return emptyMap()
        return getOrComputeCacheBySdkPath(sdkPath, file, project)
    }

    /**
     * Get or compute the cache for the given SDK path.
     */
    private fun getOrComputeCacheBySdkPath(sdkPath: SdkPath, file: VirtualFile?, project: Project): Map<String, Path> {
        // Store context for use in computeValue
        computeContexts[sdkPath] = ComputeContext(file, project)
        try {
            return getOrComputeNow(sdkPath, EXPIRATION_TIME)
        }
        finally {
            // Clean up context after computation
            computeContexts.remove(sdkPath)
        }
    }

    /**
     * Get the full path to the location of the package with the given name, or null in case there was any problem.
     *
     * @param name Package name with extension.
     * @param file The file context to determine which SDK to use. If null, falls back to project SDK.
     * @param project The current project.
     */
    fun getPackageLocation(name: String, file: VirtualFile?, project: Project): Path? {
        if (ApplicationManager.getApplication().isUnitTestMode) {
            return null
        }

        // Tectonic does not have kpsewhich, but works a little differently
        val sdk = if (file != null) {
            LatexSdkUtil.getLatexSdkForFile(file, project)
        }
        else {
            LatexSdkUtil.getLatexProjectSdk(project)
        }

        if (sdk?.sdkType is TectonicSdk) {
            return pathOrNull((sdk.sdkType as TectonicSdk).getPackageLocation(name, sdk.homePath))
        }

        return getOrComputeCache(file, project)[name]
    }

    /**
     * Get the full path to the location of the package with the given name, or null in case there was any problem.
     *
     * @param name Package name with extension.
     * @param psiFile The file context to determine which SDK to use.
     */
    fun getPackageLocation(name: String, psiFile: PsiFile): Path? = getPackageLocation(name, psiFile.virtualFile, psiFile.project)

    /**
     * Get the full path to the location of the package with the given name, or null in case there was any problem.
     * Uses project SDK only (no file context).
     *
     * @param name Package name with extension.
     * @param project The current project.
     */
    fun getPackageLocation(name: String, project: Project): Path? = getPackageLocation(name, null, project)

    /**
     * Get the full path to the location of the package with the given name, using a pre-resolved SDK path.
     * This is useful when the SDK path has already been resolved and cached by the caller.
     *
     * @param name Package name with extension.
     * @param sdkPath The pre-resolved SDK path (from [resolveSdkPath] or [LatexSdkUtil.resolveSdkPath]).
     * @param project The current project.
     */
    fun getPackageLocationBySdkPath(name: String, sdkPath: SdkPath, project: Project): Path? {
        if (ApplicationManager.getApplication().isUnitTestMode) {
            return null
        }
        return getOrComputeCacheBySdkPath(sdkPath, null, project)[name]
    }

    /**
     * Get all known package names in the LaTeX installation for the given file context.
     *
     * @param file The file context to determine which SDK to use. If null, falls back to project SDK.
     * @param project The current project.
     */
    fun getAllPackageFileNames(file: VirtualFile?, project: Project): Set<String> {
        if (ApplicationManager.getApplication().isUnitTestMode) {
            return emptySet()
        }

        val sdkPath = LatexSdkUtil.resolveSdkPath(file, project) ?: return emptySet()
        // Only return cached values, don't trigger computation here
        return getValueOrNull(sdkPath)?.keys ?: emptySet()
    }

    /**
     * Get all known package names in the LaTeX installation for the given file context.
     *
     * @param psiFile The file context to determine which SDK to use.
     */
    fun getAllPackageFileNames(psiFile: PsiFile): Set<String> = getAllPackageFileNames(psiFile.virtualFile, psiFile.project)
}