package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.rd.util.concurrentMapOf
import nl.hannahsten.texifyidea.algorithm.DFS
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.util.files.removeFileExtension
import nl.hannahsten.texifyidea.util.runInBackgroundBlocking
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Compute and cache for each package style file all the other style files it includes (directly or indirectly).
 * This is cached because even though it uses [LatexExternalPackageInclusionIndex], the computation could still
 * take some time and is thus cached in memory as it could potentially be called on every letter typed.
 */
object LatexExternalPackageInclusionCache {

    private val cache = concurrentMapOf<LatexPackage, Set<LatexPackage>>()

    // Make sure not to atttempt to re-fill the cache if the result was empty
    private var cacheHasBeenFilled = AtomicBoolean(false)

    /**
     * Map every LaTeX package style file to all the style files it includes, directly or indirectly.
     */
    @Synchronized
    fun updateOrGetCache(project: Project): Map<LatexPackage, Set<LatexPackage>> {
        if (cacheHasBeenFilled.get() || DumbService.isDumb(project)) return cache

        // Make sure the index is ready (#3754)
        if (FileBasedIndex.getInstance().getIndexModificationStamp(LatexExternalPackageInclusionIndex.Cache.id, project) < 0) return cache

        val directChildren = mutableMapOf<LatexPackage, MutableSet<LatexPackage>>()

        // Get direct children from the index
        // ???
        runInEdt {
            runInBackgroundBlocking(project, "Retrieving LaTeX package inclusions...") { indicator ->
                DumbService.getInstance(project).tryRunReadActionInSmartMode({ FileBasedIndex.getInstance().getAllKeys(LatexExternalPackageInclusionIndex.Cache.id, project) }, indicator.text)?.forEach { indexKey ->
                    DumbService.getInstance(project).tryRunReadActionInSmartMode({
                        FileBasedIndex.getInstance().processValues(
                            LatexExternalPackageInclusionIndex.Cache.id, indexKey, null, { file, _ ->
                                indicator.checkCanceled()
                                val key = LatexPackage(file.name.removeFileExtension())
                                directChildren[key] = directChildren.getOrDefault(key, mutableSetOf()).also { it.add(LatexPackage((indexKey))) }
                                true
                            },
                            GlobalSearchScope.everythingScope(project)
                        )
                    }, indicator.text)
                }

                // Do some DFS for indirect inclusions
                for (latexPackage in directChildren.keys) {
                    cache[latexPackage] = DFS(latexPackage) { parent -> directChildren[parent] ?: emptySet() }.execute()
                }
                cacheHasBeenFilled.set(true)
            }
        }

        return cache
    }

    /**
     * Given a certain set of [LatexPackage]s, return the set of all those packages plus all the packages they directly or indirectly include.
     */
    fun getAllIndirectlyIncludedPackages(packages: Collection<LatexPackage>, project: Project): Set<LatexPackage> {
        val result = packages.toMutableSet()
        val allInclusions = updateOrGetCache(project)
        for (latexPackage in packages) {
            result.addAll(allInclusions[latexPackage] ?: emptySet())
        }
        return result
    }
}