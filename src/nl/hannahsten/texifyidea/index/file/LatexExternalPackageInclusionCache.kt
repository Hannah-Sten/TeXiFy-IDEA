package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.rd.util.concurrentMapOf
import nl.hannahsten.texifyidea.algorithm.DFS
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.util.files.removeFileExtension

/**
 * Compute and cache for each package style file all the other style files it includes (directly or indirectly).
 * This is cached because even though it uses [LatexExternalPackageInclusionIndex], the computation could still
 * take some time and is thus cached in memory as it could potentially be called on every letter typed.
 */
object LatexExternalPackageInclusionCache {

    private val cache = concurrentMapOf<LatexPackage, Set<LatexPackage>>()

    /**
     * Map every LaTeX package style file to all the style files it includes, directly or indirectly.
     */
    @Synchronized
    fun getAllPackageInclusions(project: Project): Map<LatexPackage, Set<LatexPackage>> {
        if (cache.isNotEmpty()) return cache

        val directChildren = mutableMapOf<LatexPackage, MutableSet<LatexPackage>>()

        // Get direct children from the index
        FileBasedIndex.getInstance().getAllKeys(LatexExternalPackageInclusionIndex.id, project).forEach { indexKey ->
            FileBasedIndex.getInstance().processValues(LatexExternalPackageInclusionIndex.id, indexKey, null, { file, _ ->
                val key = LatexPackage(file.name.removeFileExtension())
                directChildren[key] = directChildren.getOrDefault(key, mutableSetOf()).also { it.add(LatexPackage((indexKey))) }
                true
            }, GlobalSearchScope.everythingScope(project))
        }

        // Do some DFS for indirect inclusions
        for (latexPackage in directChildren.keys) {
            cache[latexPackage] = DFS(latexPackage) { parent -> directChildren[parent] ?: emptySet() }.execute()
        }

        return cache
    }

    /**
     * Given a certain set of [LatexPackage]s, return the set of all those packages plus all the packages they directly or indirectly include.
     */
    fun getAllIndirectlyIncludedPackages(packages: Collection<LatexPackage>, project: Project): Set<LatexPackage> {
        val result = packages.toMutableSet()
        val allInclusions = getAllPackageInclusions(project)
        for (latexPackage in packages) {
            result.addAll(allInclusions[latexPackage] ?: emptySet())
        }
        return result
    }
}