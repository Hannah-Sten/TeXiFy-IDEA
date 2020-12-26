package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.IndexableSetContributor
import nl.hannahsten.texifyidea.file.LatexSourceFileType
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.files.allChildFiles

/**
 * Specify the paths that have to be indexed for the [LatexExternalCommandIndex].
 */
class LatexIndexableSetContributor : IndexableSetContributor() {
    override fun getAdditionalProjectRootsToIndex(project: Project): MutableSet<VirtualFile> {
        // todo automagically add source root
        // We have to filter on the right file extension, because otherwise we might be indexing .tex files as well which is not intended
        val filesToIndex = mutableSetOf<VirtualFile>()
        LatexSdkUtil.getLatexProjectSdk(project)?.rootProvider?.getFiles(OrderRootType.SOURCES)?.forEach { root ->
            filesToIndex.addAll(root.allChildFiles().filter { it.extension == LatexSourceFileType.defaultExtension })
        }

        return filesToIndex
    }

    override fun getAdditionalRootsToIndex() = mutableSetOf<VirtualFile>()
}