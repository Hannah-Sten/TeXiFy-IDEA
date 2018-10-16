@file:JvmName("FileSetFinder")

package nl.rubensten.texifyidea.util

import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.index.LatexIncludesIndex

/**
 * Finds all the files in the project that are somehow related using includes.
 * <p>
 * When A includes B and B includes C then A, B & C will all return a set containing A, B & C.
 *
 * @param baseFile
 *         The file to find the reference set of.
 * @return All the files that are cross referenced between each other.
 */
fun findReferencedFileSet(baseFile: PsiFile): Set<PsiFile> {
    // Setup.
    val project = baseFile.project
    val includes = LatexIncludesIndex.getItems(project)

    // Find all root files.
    val roots = includes.asSequence()
            .map { it.containingFile }
            .distinct()
            .filter { it.isRoot() }
            .toSet()

    // Map root to all directly referenced files.
    val sets = HashMap<PsiFile, Set<PsiFile>>()
    for (root in roots) {
        val referenced = TexifyUtil.getReferencedFiles(root).toSet() + root

        if (referenced.contains(baseFile)) {
            return referenced + baseFile
        }

        sets[root] = referenced
    }

    // Look for matching root.
    for (referenced in sets.values) {
        if (referenced.contains(baseFile)) {
            return referenced + baseFile
        }
    }

    return setOf(baseFile)
}