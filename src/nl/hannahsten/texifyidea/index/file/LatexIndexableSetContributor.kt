package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.IndexableSetContributor
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.isTestProject
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

    private var extractedFiles = false

    override fun getAdditionalProjectRootsToIndex(project: Project): MutableSet<VirtualFile> {
        // Avoid indexing in tests
        if (ApplicationManager.getApplication().isUnitTestMode) {
            return mutableSetOf()
        }

        if (!TexifySettings.getInstance().enableExternalIndex) return mutableSetOf()

        // Add source files
        val roots = LatexSdkUtil.getSdkSourceRoots(project) { sdk, homePath -> sdk.getDefaultSourcesPath(homePath) }.toMutableSet()
        // Check if we possibly need to extract files first
        Log.debug("Indexing source roots $roots")
        for (root in roots) {
            if (root.path.contains("MiKTeX", ignoreCase = true) && !extractedFiles) {
                try {
                    if (!extractMiktexFiles(root)) return mutableSetOf()
                }
                catch (e: ArchiverException) {
                    // Ignore permission errors, nothing we can do about that
                    Log.debug("Exception when trying to extract MiKTeX source files: ${e.message}")
                    return mutableSetOf()
                }
            }
        }

        // Add style files (used in e.g. LatexExternalPackageInclusionIndex)
        // Unfortunately, since .sty is a LaTeX file type, these will all be parsed, which will take an enormous amount of time.
        // Note that using project-independent getAdditionalRootsToIndex does not fix this
        roots.addAll(LatexSdkUtil.getSdkSourceRoots(project) { sdkType, homePath -> sdkType.getDefaultStyleFilesPath(homePath) })

        return roots
    }

    /**
     * MiKTeX keeps the dtx files in tar.xz/tar.bz2 files, so we have to extract them ourselves.
     * We could check if the target files exist and in that case not extract again, however then we would never update packages
     * Because maintaining extraction dates seems a bit too much work for now, just extract again after reboot only
     *
     * @return If succeeded.
     */
    private fun extractMiktexFiles(root: VirtualFile): Boolean {
        val txArchiver = TarXZUnArchiver()
        File(root.path).list { _, name -> name.endsWith("tar.xz") }?.forEach { zipName ->
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