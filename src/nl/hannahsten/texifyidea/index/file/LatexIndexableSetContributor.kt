package nl.hannahsten.texifyidea.index.file

import arrow.atomic.AtomicBoolean
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.IndexableSetContributor
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.addToLuatexPathSearchDirectories
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.requiredParameter
import org.codehaus.plexus.archiver.ArchiverException
import org.codehaus.plexus.archiver.tar.TarBZip2UnArchiver
import org.codehaus.plexus.archiver.tar.TarXZUnArchiver
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * Specify the paths that have to be indexed for the [LatexExternalCommandIndex].
 */
class LatexIndexableSetContributor : IndexableSetContributor() {

    object Cache {
        var externalDirectFileInclusions = mutableMapOf<Project, Set<VirtualFile>>()

        val cacheFillInProgress = AtomicBoolean(false)
    }

    override fun getAdditionalProjectRootsToIndex(project: Project): MutableSet<VirtualFile> {
        // Avoid indexing in tests
        if (project.isTestProject()) {
            return mutableSetOf()
        }

        if (!TexifySettings.getInstance().enableExternalIndex) return mutableSetOf()

        // Add source files
        val roots = LatexSdkUtil.getSdkSourceRoots(project) { sdk, homePath -> sdk.getDefaultSourcesPath(homePath) }.toMutableSet()
        // Check if we possibly need to extract files first, but don't try more than once
        if (!extractedFiles) {
            extractedFiles = true
            for (root in roots) {
                if (root.path.contains("MiKTeX", ignoreCase = true)) {
                    // Run in the background with progress, we cannot wait for completion because that would block this thread,
                    // so in the worst case the files will only be indexed the next time indexing is triggered
                    ProgressManager.getInstance().run(object : Backgroundable(project, "Extracting MiKTeX package source files", true) {
                        override fun run(indicator: ProgressIndicator) {
                            try {
                                extractMiktexFiles(root, indicator)
                            }
                            catch (e: ArchiverException) {
                                // Ignore permission errors, nothing we can do about that
                                Log.debug("Exception when trying to extract MiKTeX source files: ${e.stackTraceToString()}")
                            }
                        }
                    })
                }
            }
        }

        // Add style files (used in e.g. LatexExternalPackageInclusionIndex)
        // Unfortunately, since .sty is a LaTeX file type, these will all be parsed, which will take an enormous amount of time.
        // Note that using project-independent getAdditionalRootsToIndex does not fix this
        roots.addAll(LatexSdkUtil.getSdkSourceRoots(project) { sdkType, homePath -> sdkType.getDefaultStyleFilesPath(homePath) })

        roots.addAll(getTexinputsPaths(project, rootFiles = listOf(), expandPaths = false).mapNotNull { LocalFileSystem.getInstance().findFileByPath(it) })

        // Using the index while building it may be problematic, cache the result and hope it doesn't create too much trouble
        fillExternalDirectFileInclusionsCache(project)
        roots.addAll(Cache.externalDirectFileInclusions.getOrDefault(project, emptySet()).filter { it.exists() })

        Log.debug("Indexing source roots $roots")
        return roots
    }

    private fun fillExternalDirectFileInclusionsCache(project: Project) {
        if (!Cache.externalDirectFileInclusions.keys.contains(project).not() || DumbService.isDumb(project)) return
        if (!Cache.cacheFillInProgress.getAndSet(true).not()) return
        // Don't wait for the result, as somehow this may block the UI? #4055 This function seems to be called quite often so let's hope it's okay to miss it the first time
        runInBackgroundNonBlocking(project, "Searching for inclusions by absolute path...") { reporter ->
            try {
                // Bibliography and direct input commands

                val commandNames = CommandMagic.includeOnlyExtensions.entries.filter { it.value.contains("bib") || it.value.contains("tex") }.map { it.key }.toSet()
                val includeCommands = readAction {
                    NewCommandsIndex.getByNames(commandNames, project)
                }

                val workSize = includeCommands.size
                val externalFiles = includeCommands
                    // We can't add single files, so take the parent
                    .mapNotNull {
                        reporter.sizedStep((PROGRESS_SIZE / workSize)) {
                            val path = smartReadAction(project) { if (!it.isValid) null else it.requiredParameter(0) } ?: return@sizedStep null
                            val file = if (File(path).isAbsolute) {
                                LocalFileSystem.getInstance().findFileByPath(path)
                            }
                            else {
                                smartReadAction(project) { if (!it.isValid) null else it.containingFile.parent }?.virtualFile?.findFileByRelativePath(path)
                            }
                            smartReadAction(project) { file?.parent }
                        }
                    }
                    .toMutableList()

                // addtoluatexpath package
                val luatexPathDirectories = readAction { addToLuatexPathSearchDirectories(project) }
                externalFiles.addAll(luatexPathDirectories)

                Cache.externalDirectFileInclusions[project] = externalFiles.toSet()
            }
            finally {
                Cache.cacheFillInProgress.set(false)
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
    private fun extractMiktexFiles(root: VirtualFile, indicator: ProgressIndicator): Boolean {
        val txArchiver = TarXZUnArchiver()
        val zips = File(root.path).list { _, name -> name.endsWith("tar.xz") } ?: return false
        // See AbstractProgressIndicatorBase.java:213
        indicator.isIndeterminate = false
        for ((index, zipName) in zips.withIndex()) {
            indicator.fraction = index.toDouble() / zips.size
            txArchiver.sourceFile = File(root.path, zipName)
            // Note that by keeping the target path the same for everything, some packages will install in source/latex and some in source/latex/latex depending on how they were zipped
            val destination = File(root.path, "latex")

            // If the user has e.g. a MiKTeX admin install, we do not have rights to extract zips
            if (!Files.isWritable(Path.of(root.path))) {
                extractedFiles = true
                Log.debug("MiKTeX installation path ${root.path} is not writable, cannot extract sources")
                return false
            }

            // Try to create if not exists
            if (!destination.exists() && !destination.mkdir()) {
                extractedFiles = true
                Log.debug("Could not create destination directory ${destination.absolutePath}")
                return false
            }

            txArchiver.destDirectory = destination
            txArchiver.extract()
        }
        val bz2Archiver = TarBZip2UnArchiver()
        File(root.path).list { _, name -> name.endsWith("tar.bz2") }?.forEach { zipName ->
            bz2Archiver.sourceFile = File(root.path, zipName)
            bz2Archiver.destDirectory = File(root.path, "latex")
            bz2Archiver.extract()
        }
        extractedFiles = true

        return true
    }

    override fun getAdditionalRootsToIndex() = mutableSetOf<VirtualFile>()
}

private var extractedFiles = false