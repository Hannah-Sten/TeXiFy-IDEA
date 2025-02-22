package nl.hannahsten.texifyidea.util.files

import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findFile
import com.intellij.platform.util.progress.ProgressReporter
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.index.BibtexEntryIndex
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.index.LatexIncludesIndex
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.parser.isDefinition
import nl.hannahsten.texifyidea.util.parser.requiredParameter
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
internal suspend fun Project.findReferencedFileSetWithoutCache(reporter: ProgressReporter?): Map<PsiFile, Set<PsiFile>> {
    // Find all root files.
    val project = this
    val scope = GlobalSearchScope.projectScope(project)
    val roots = LatexIncludesIndex.Util.getItemsNonBlocking(project, scope)
        .map { smartReadAction(this) { it.containingFile } }
        .distinct()
        .filter { smartReadAction(this) { it.isRoot() } }
        .toSet()

    return roots
        .associateWith { root ->
            // Map root to all directly referenced files.
            reporter?.sizedStep((1000 / roots.size).toInt()) {
                root.referencedFiles(root.virtualFile) + root
            } ?: (root.referencedFiles(root.virtualFile) + root)
        }
}

/**
 * Check for tectonic.toml files in the project.
 * These files can input multiple tex files, which would then be in the same file set.
 * Example file: https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/3773#issuecomment-2503221732
 * @return List of sets of files included by the same toml file.
 */
suspend fun findTectonicTomlInclusions(project: Project): List<Set<PsiFile>> {
    // Actually, according to https://tectonic-typesetting.github.io/book/latest/v2cli/build.html?highlight=tectonic.toml#remarks Tectonic.toml files can appear in any parent directory, but we only search in the project for now
    val tomlFiles = smartReadAction(project) { findTectonicTomlFiles(project) }
    val filesets = tomlFiles.mapNotNull { tomlFile ->
        val data = TomlMapper().readValue(File(tomlFile.path), Map::class.java)
        val output = (data.getOrDefault("output", null) as? List<*> ?: return@mapNotNull null).firstOrNull() as? Map<*, *>
        // The Inputs field was added after 0.15.0, at the moment of writing unreleased so we cannot check the version
        val inputs = if (output?.keys?.contains("inputs") == true) {
            output.getOrDefault("inputs", listOf("_preamble.tex", "index.tex", "_postamble.tex")) as? List<*> ?: return@mapNotNull null
        }
        else {
            // See https://tectonic-typesetting.github.io/book/latest/ref/tectonic-toml.html#contents
            val preamble = output?.getOrDefault("preamble", "_preamble.tex") as? String ?: return@mapNotNull null
            val index = output.getOrDefault("index", "index.tex") as? String ?: return@mapNotNull null
            val postamble = output.getOrDefault("postamble", "_postamble.tex") as? String ?: return@mapNotNull null
            listOf(preamble, index, postamble)
        }
        // Inputs can be either a map "inline" -> String or file name
        // Actually it can also be just a single file name, but then we don't need all this gymnastics
        inputs.filterIsInstance<String>().mapNotNull {
            smartReadAction(project) { tomlFile.parent.findFile("src/$it")?.psiFile(project) }
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
 * A toml file can be in any parent directory.
 */
fun VirtualFile.hasTectonicTomlFile() = findTectonicTomlFile() != null

fun VirtualFile.findTectonicTomlFile(): VirtualFile? {
    var parent = this
    for (i in 0..20) {
        if (parent.parent != null && parent.parent.isDirectory && parent.parent.exists()) {
            parent = parent.parent
        }
        else {
            break
        }

        parent?.findFile("Tectonic.toml")?.let { return it }
    }
    return null
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

/**
 * The addtoluatexpath package supports adding to \input@path in different ways
 */
fun addToLuatexPathSearchDirectories(project: Project): List<VirtualFile> {
    val direct = runReadAction { LatexCommandsIndex.Util.getCommandsByNames(setOf(LatexGenericRegularCommand.ADDTOLUATEXPATH.cmd), project, GlobalSearchScope.projectScope(project)) }
        .mapNotNull { command -> runReadAction { command.requiredParameter(0) } }
        .flatMap { it.split(",") }
    val viaUsepackage = runReadAction { LatexIncludesIndex.Util.getCommandsByNames(CommandMagic.packageInclusionCommands, project, GlobalSearchScope.projectScope(project)) }
        .filter { runReadAction { it.requiredParameter(0) } == LatexPackage.ADDTOLUATEXPATH.name }
        .flatMap { runReadAction { it.getOptionalParameterMap().keys } }
        .flatMap { it.text.split(",") }

    val luatexPathDirectories = (direct + viaUsepackage).flatMap {
        val basePath = LocalFileSystem.getInstance().findFileByPath(it.trimEnd('/', '*')) ?: return@flatMap emptyList()
        if (it.endsWith("/**")) {
            basePath.allChildDirectories()
        }
        else if (it.endsWith("/*")) {
            basePath.children.filter { it.isDirectory }
        }
        else {
            listOf(basePath)
        }
    }
    return luatexPathDirectories
}
