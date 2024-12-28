package nl.hannahsten.texifyidea.util.files

import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.BibtexEntryIndex
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.index.LatexIncludesIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.parser.isDefinition
import java.io.File

/**
 * Finds all the files in the project that are somehow related using includes.
 *
 * When A includes B and B includes C then A, B & C will all return a set containing A, B & C.
 * There can be multiple root files in one file set.
 *
 * Be careful when using this function directly over something like [ReferencedFileSetService] where the result
 * values are cached.
 *
 * @return Map all root files which include any other file, to the file set containing that root file.
 */
// Internal because only ReferencedFileSetCache should call this
internal fun Project.findReferencedFileSetWithoutCache(): Map<PsiFile, Set<PsiFile>> {
    // Find all root files.
    return LatexIncludesIndex.Util.getItems(this)
        .asSequence()
        .map { it.containingFile }
        .distinct()
        .filter { it.isRoot() }
        .toSet()
        .associateWith { root ->
            // Map root to all directly referenced files.
            runReadAction { root.referencedFiles(root.virtualFile) } + root
        }
}

/**
 * Check for tectonic.toml files in the project.
 * These files can input multiple tex files, which would then be in the same file set.
 * Example file: https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/3773#issuecomment-2503221732
 * @return List of sets of files included by the same toml file.
 */
fun findTectonicTomlInclusions(project: Project): List<Set<PsiFile>> {
    // Actually, according to https://tectonic-typesetting.github.io/book/latest/v2cli/build.html?highlight=tectonic.toml#remarks Tectonic.toml files can appear in any parent directory, but we only search in the project for now
    val tomlFiles = findTectonicTomlFiles(project)
    val filesets = tomlFiles.mapNotNull { tomlFile ->
            val data = TomlMapper().readValue(File(tomlFile.path), Map::class.java)
            val outputList = data.getOrDefault("output", null) as? List<*> ?: return@mapNotNull null
            val inputs = (outputList.firstOrNull() as? Map<*, *>)?.getOrDefault("inputs", null) as? List<*> ?: return@mapNotNull null
            // Inputs can be either a map "inline" -> String or file name
            // Actually it can also be just a single file name, but then we don't need all this gymnastics
            inputs.filterIsInstance<String>().mapNotNull {
                tomlFile.parent.findFile("src/$it")?.psiFile(project)
            }.toSet()
        }

    return filesets
}

private fun findTectonicTomlFiles(project: Project): MutableSet<VirtualFile> {
    val tomlFiles = mutableSetOf<VirtualFile>()
    ProjectFileIndex.getInstance(project).iterateContent({ tomlFiles.add(it) }, { it.name == "Tectonic.toml" })
    return tomlFiles
}

/**
 * Finds all the files in the project that are somehow related using includes.
 *
 * When A includes B and B includes C then A, B & C will all return a set containing A, B & C.
 *
 * @return All the files that are cross referenced between each other.
 */
fun PsiFile.referencedFileSet(): Set<PsiFile> {
    return ReferencedFileSetService.getInstance().referencedFileSetOf(this)
}

/**
 * @see [BibtexEntryIndex.getIndexedEntriesInFileSet]
 */
fun PsiFile.bibtexIdsInFileSet() = BibtexEntryIndex().getIndexedEntriesInFileSet(this)

/**
 * @see [LatexCommandsIndex.Util.getItemsInFileSet]
 */
fun PsiFile.commandsInFileSet(): Collection<LatexCommands> = LatexCommandsIndex.Util.getItemsInFileSet(this)

/**
 * @see [LatexCommandsIndex.Util.getItemsAndFilesInFileSet]
 */
fun PsiFile.commandsAndFilesInFileSet(): List<Pair<PsiFile, Collection<LatexCommands>>> = LatexCommandsIndex.Util.getItemsAndFilesInFileSet(this)

/**
 * Get all the definitions in the file set.
 */
fun PsiFile.definitionsInFileSet(): Collection<LatexCommands> {
    return LatexDefinitionIndex.Util.getItemsInFileSet(this)
        .filter { it.isDefinition() }
}

/**
 * Get all the definitions and redefinitions in the file set.
 */
fun PsiFile.definitionsAndRedefinitionsInFileSet(): Collection<LatexCommands> {
    return LatexDefinitionIndex.Util.getItemsInFileSet(this)
}
