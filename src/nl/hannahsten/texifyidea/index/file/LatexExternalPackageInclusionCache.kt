package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import nl.hannahsten.texifyidea.util.files.removeFileExtension

/**
 * Compute and cache for each package style file all the other style files it includes (directly or indirectly).
 * This is cached because even though it uses [LatexExternalPackageInclusionIndex], the computation could still
 * take some time and is thus cached in memory as it could potentially be called on every letter typed.
 */
object LatexExternalPackageInclusionCache {

    // todo maybe LatexPackage instead of string
    private var cache = mapOf<String, MutableSet<String>>()

    /**
     * Map every LaTeX package style file to all the style files it includes, directly or indirectly.
     */
    fun getAllPackageInclusions(project: Project): Map<String, Set<String>> {
        if (cache.isNotEmpty()) return cache

        FileBasedIndex.getInstance().getAllKeys(LatexExternalPackageInclusionIndex.id, project).forEach { key ->
            FileBasedIndex.getInstance().processValues(LatexExternalPackageInclusionIndex.id, key, null, { file, value ->
                cache.getOrDefault(file.name.removeFileExtension(), mutableSetOf()).add(key)
                true
            }, GlobalSearchScope.everythingScope(project))
        }

        // todo do some DFS for indirect inclusions

        return cache
    }
}