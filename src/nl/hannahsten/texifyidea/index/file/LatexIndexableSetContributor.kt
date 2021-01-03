package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.IndexableSetContributor
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import org.codehaus.plexus.archiver.tar.TarBZip2UnArchiver
import org.codehaus.plexus.archiver.tar.TarXZUnArchiver
import org.codehaus.plexus.logging.console.ConsoleLoggerManager
import java.io.File

/**
 * Specify the paths that have to be indexed for the [LatexExternalCommandIndex].
 */
class LatexIndexableSetContributor : IndexableSetContributor() {

    private var extractedFiles = false

    override fun getAdditionalProjectRootsToIndex(project: Project): MutableSet<VirtualFile> {
        // Avoid indexing in tests
        return if (!project.name.contains("_temp_")) {
            val roots = LatexSdkUtil.getSdkSourceRoots(project).toMutableSet()
            for (root in roots) {
                // MiKTeX keeps the dtx files in tar.xz/tar.bz2 files, so we have to extract them ourselves.
                // We could check if the target files exist and in that case not extract again, however then we would never update packages
                // Because maintaining extraction dates seems a bit too much work for now, just extract again after reboot only
                if (root.path.contains("MiKTeX") && !extractedFiles) {
                    val txArchiver = TarXZUnArchiver()
                    txArchiver.enableLogging(ConsoleLoggerManager().also { it.initialize() }.getLoggerForComponent("noop"))
                    File(root.path).list { _, name -> name.endsWith("tar.xz") }?.forEach { zipName ->
                        txArchiver.sourceFile = File(root.path, zipName)
                        // Note that by keeping the target path the same for everything, some packages will install in source/latex and some in source/latex/latex depending on how they were zipped
                        txArchiver.destDirectory = File(root.path, "latex")
                        txArchiver.extract()
                    }
                    val bz2Archiver = TarBZip2UnArchiver()
                    bz2Archiver.enableLogging(ConsoleLoggerManager().also { it.initialize() }.getLoggerForComponent("noop"))
                    File(root.path).list { _, name -> name.endsWith("tar.bz2") }?.forEach { zipName ->
                        bz2Archiver.sourceFile = File(root.path, zipName)
                        bz2Archiver.destDirectory = File(root.path, "latex")
                        bz2Archiver.extract()
                    }
                    extractedFiles = true
                }
            }
            roots
        }
        else {
            mutableSetOf()
        }
    }

    override fun getAdditionalRootsToIndex() = mutableSetOf<VirtualFile>()
}