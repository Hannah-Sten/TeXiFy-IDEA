package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import nl.hannahsten.texifyidea.algorithm.DFS
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.util.files.removeFileExtension

/**
 * Compute and cache for each package style file all the other style files it includes (directly or indirectly).
 * This is cached because even though it uses [LatexExternalPackageInclusionIndex], the computation could still
 * take some time and is thus cached in memory as it could potentially be called on every letter typed.
 */
object LatexExternalPackageInclusionCache {

    private val cache = mutableMapOf<LatexPackage, Set<LatexPackage>>()

    /**
     * Map every LaTeX package style file to all the style files it includes, directly or indirectly.
     */
    fun getAllPackageInclusions(project: Project): Map<LatexPackage, Set<LatexPackage>> {
        if (cache.isNotEmpty()) return cache

        val directChildren = mapOf<LatexPackage, MutableSet<LatexPackage>>()

        // Get direct children from the index
        FileBasedIndex.getInstance().getAllKeys(LatexExternalPackageInclusionIndex.id, project).forEach { key ->
            FileBasedIndex.getInstance().processValues(LatexExternalPackageInclusionIndex.id, key, null, { file, _ ->
                directChildren.getOrDefault(LatexPackage(file.name.removeFileExtension()), mutableSetOf()).add(LatexPackage((key)))
                true
            }, GlobalSearchScope.everythingScope(project))
        }

        // Do some DFS for indirect inclusions
        for (latexPackage in cache.keys) {
            cache[latexPackage] = DFS(latexPackage) { parent -> cache[parent] ?: emptySet() }.execute()
        }

        return cache
    }
}