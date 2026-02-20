package nl.hannahsten.texifyidea.index.file

import arrow.atomic.AtomicBoolean
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.forEachWithProgress
import com.intellij.util.indexing.IndexableSetContributor
import nl.hannahsten.texifyidea.index.projectstructure.LatexProjectStructure
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.TexifyCoroutine
import nl.hannahsten.texifyidea.util.getTexinputsPaths
import org.codehaus.plexus.archiver.ArchiverException
import org.codehaus.plexus.archiver.tar.TarBZip2UnArchiver
import org.codehaus.plexus.archiver.tar.TarXZUnArchiver
import kotlin.io.path.isWritable
import kotlin.io.path.listDirectoryEntries

/**
 * Specify the paths that have to be indexed for the commands defined in packages.
 */
class LatexIndexableSetContributor : IndexableSetContributor() {

    private val extractedFiles: AtomicBoolean = AtomicBoolean(false)

    private fun extractFileIfNecessary(roots: Set<VirtualFile>, project: Project) {
        if (!extractedFiles.getAndSet(true)) {
            for (root in roots) {
                if (root.path.contains("MiKTeX", ignoreCase = true)) {
                    // Run in the background with progress, we cannot wait for completion because that would block this thread,
                    // so in the worst case the files will only be indexed the next time indexing is triggered
                    TexifyCoroutine.runInBackground {
                        withBackgroundProgress(project, "Extracting MiKTeX package source files") {
                            extractMiktexFiles(root)
                        }
                    }
                }
            }
        }
    }

    /**
     * MiKTeX keeps the dtx files in tar.xz/tar.bz2 files, so we have to extract them ourselves.
     * We could check if the target files exist and in that case not extract again, however then we would never update packages
     * Because maintaining extraction dates seems a bit too much work for now, just extract again after reboot only
     *
     * @return If succeeded.
     */
    private suspend fun extractMiktexFiles(root: VirtualFile): Boolean {
        val rootPath = root.toNioPathOrNull() ?: return false
        if (!rootPath.isWritable()) {
            Log.debug("MiKTeX installation path $rootPath is not writable, cannot extract sources")
            return false
        }
        val txArchiver = TarXZUnArchiver()
        val bz2Archiver = TarBZip2UnArchiver()
        val zips = rootPath.listDirectoryEntries("*.tar.{bz2,xz}")
        zips.forEachWithProgress { zipName ->
            val archiver = if (zipName.endsWith(".bz2")) bz2Archiver else txArchiver
            archiver.sourceFile = rootPath.resolve(zipName).toFile()
            // Note that by keeping the target path the same for everything, some packages will install in source/latex and some in source/latex/latex depending on how they were zipped
            val destination = rootPath.resolve("latex").toFile()

            // If the user has e.g. a MiKTeX admin install, we do not have rights to extract zips
            // Try to create if not exists
            if (!destination.exists()) {
                if (!destination.mkdir()) {
                    Log.debug("Could not create destination directory ${destination.absolutePath}")
                    return@forEachWithProgress
                }
                txArchiver.destDirectory = destination
                try {
                    txArchiver.extract()
                }
                catch (e: ArchiverException) {
                    Log.debug("Exception when trying to extract MiKTeX source files: ${e.stackTraceToString()}")
                }
            }
        }
        return true
    }

    override fun getAdditionalProjectRootsToIndex(project: Project): Set<VirtualFile> {
        // Avoid indexing in tests
        if (ApplicationManager.getApplication().isUnitTestMode) {
            return emptySet()
        }

        if (!TexifySettings.getState().enableExternalIndex) return emptySet()

        // Add source files
        val roots = LatexSdkUtil.getSdkSourceRoots(project) { sdk, homePath -> sdk.getDefaultSourcesPath(homePath) }.toMutableSet()
        // Check if we possibly need to extract files first, but don't try more than once
        extractFileIfNecessary(roots, project)

        // Add style files (used in e.g. LatexExternalPackageInclusionIndex)
        // Unfortunately, since .sty is a LaTeX file type, these will all be parsed, which will take an enormous amount of time.
        // Note that using project-independent getAdditionalRootsToIndex does not fix this
        roots.addAll(LatexSdkUtil.getSdkSourceRoots(project) { sdkType, homePath -> sdkType.getDefaultStyleFilesPath(homePath) })

        roots.addAll(getTexinputsPaths(project, rootFiles = listOf(), expandPaths = false).mapNotNull { LocalFileSystem.getInstance().findFileByPath(it) })

        // Using the index while building it may be problematic, cache the result and hope it doesn't create too much trouble
        findExternalDirectFileInclusionsTo(project, roots)
        Log.debug("Indexing source roots $roots")
        return roots
    }

    private fun findExternalDirectFileInclusionsTo(project: Project, roots: MutableSet<VirtualFile>) {
        val filesets = LatexProjectStructure.getFilesets(project) ?: return
        filesets.mapping.keys.filterTo(roots) { it.isValid }
    }

    override fun getAdditionalRootsToIndex(): Set<VirtualFile> = emptySet()
}