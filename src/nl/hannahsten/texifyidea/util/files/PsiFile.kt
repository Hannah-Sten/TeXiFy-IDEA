package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.file.BibtexFileType
import nl.hannahsten.texifyidea.file.ClassFileType
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.file.StyleFileType
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.index.LatexEnvironmentsIndex
import nl.hannahsten.texifyidea.index.LatexIncludesIndex
import nl.hannahsten.texifyidea.lang.Package
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.util.*

/**
 * Get the file search scope for this psi file.
 */
val PsiFile.fileSearchScope: GlobalSearchScope
    get() = GlobalSearchScope.fileScope(this)

/**
 * Looks for all file inclusions in a given file, excluding installed LaTeX packages.
 *
 * @return A list containing all included files.
 */
fun PsiFile.findInclusions(): List<PsiFile> {
    return LatexIncludesIndex.getItems(this)
            .flatMap { it.getIncludedFiles(false) }
            .toList()
}

/**
 * Checks if the file has LaTeX syntax.
 */
fun PsiFile.isLatexFile() = fileType == LatexFileType ||
        fileType == StyleFileType || fileType == ClassFileType

/**
 * Checks if the file has a `.sty` extention. This is a workaround for file type checking.
 */
fun PsiFile.isStyleFile() = virtualFile.extension == "sty"

/**
 * Checks if the file has a `.cls` extention. This is a workaround for file type checking.
 */
fun PsiFile.isClassFile() = virtualFile.extension == "cls"

/**
 * Looks up the argument that is in the documentclass command, and if the file is found in the project return it.
 * Note this explicitly does not find files elsewhere on the system.
 */
fun PsiFile.documentClassFileInProject(): PsiFile? {
    val command = commandsInFile().asSequence()
            .filter { it.name == "\\documentclass" }
            .firstOrNull() ?: return null
    val argument = command.requiredParameter(0) ?: return null
    return findFile("$argument.cls")
}

/**
 * Checks if the given package is included in the file set.
 *
 * @param packageName
 *          The name of the package to check for.
 * @return `true` when there is a package with name `packageName` in the file set, `false` otherwise.
 */
fun PsiFile.isUsed(packageName: String) = PackageUtils.getIncludedPackages(this).contains(packageName)

/**
 * Checks if the given package is included into the file set.
 *
 * @param `package`
 *          The package to check for.
 * @return `true` when there is a package `package` included in the file set, `false` otherwise.
 */
@Suppress("unused")
fun PsiFile.isUsed(`package`: Package) = isUsed(`package`.name)

/**
 * Scans the whole document (recursively) for all referenced/included files, except installed LaTeX packages.
 * Never use this directly, use the cached [referencedFileSet] instead.
 *
 * @return A collection containing all the PsiFiles that are referenced from this file.
 */
internal fun PsiFile.referencedFiles(rootFile: VirtualFile): Set<PsiFile> {
    val result = HashSet<PsiFile>()
    referencedFiles(result, rootFile)
    return result
}

/**
 * Recursive implementation of [referencedFiles].
 */
private fun PsiFile.referencedFiles(files: MutableCollection<PsiFile>, rootFile: VirtualFile) {
    LatexIncludesIndex.getItems(project, fileSearchScope).forEach command@{ command ->
        command.references.filterIsInstance<InputFileReference>()
                .mapNotNull { it.resolve(false, rootFile) }
                .forEach {
                    // Do not re-add all referenced files if we already did that
                    if (it in files) return@forEach
                    files.add(it)
                    it.referencedFiles(files, rootFile)
                }
    }
}

/**
 * Looks up a file relative to this file.
 *
 * @param path
 *         The path relative to this file.
 * @return The found file, or `null` when the file could not be found.
 */
fun PsiFile.findFile(path: String, extensions: Set<String>? = null): PsiFile? {
    val directory = containingDirectory?.virtualFile

    val file = directory?.findFile(path, extensions
            ?: Magic.File.includeExtensions)
            ?: return scanRoots(path, extensions)
    val psiFile = PsiManager.getInstance(project).findFile(file)
    if (psiFile == null || LatexFileType != psiFile.fileType &&
            StyleFileType != psiFile.fileType &&
            BibtexFileType != psiFile.fileType) {
        return scanRoots(path, extensions)
    }

    return psiFile
}


/**
 * Looks up the file(s) included by a command relative to this file.
 *
 * @param command
 *         The include command
 * @return The found file(s), or an empty set when no files could not be found.
 */
fun PsiFile.findIncludedFile(command: LatexCommands): Set<PsiFile> {
    val arguments = command.getAllRequiredArguments() ?: return emptySet()

    return arguments.filter { it.isNotEmpty() }.mapNotNull {
        val extension = Magic.File.automaticExtensions[command.name]
        if (extension != null) {
            findFile(it, setOf(extension))
        }
        else {
            findFile(it)
        }
    }.toSet()
}

/**
 * [findFile] but then it scans all content roots.
 *
 * @param path
 *         The path relative to {@code original}.
 * @return The found file, or `null` when the file could not be found.
 */
fun PsiFile.scanRoots(path: String, extensions: Set<String>? = null): PsiFile? {
    val rootManager = ProjectRootManager.getInstance(project)
    rootManager.contentSourceRoots.forEach { root ->
        val file = root.findFile(path, extensions
                ?: Magic.File.includeExtensions)
        if (file != null) {
            return file.psiFile(project)
        }
    }

    return null
}

/**
 * Get the corresponding document of the PsiFile.
 */
fun PsiFile.document(): Document? = PsiDocumentManager.getInstance(project).getDocument(this)

/**
 * @see [LatexCommandsIndex.getItems]
 */
fun PsiFile.commandsInFile(): Collection<LatexCommands> = LatexCommandsIndex.getItems(this)

/**
 * @see [LatexEnvironmentsIndex.getItems]
 */
fun PsiFile.environmentsInFile(): Collection<LatexEnvironment> = LatexEnvironmentsIndex.getItems(this)

/**
 * Get the editor of the file if it is currently opened.
 */
fun PsiFile.openedEditor() = FileEditorManager.getInstance(project).selectedTextEditor

/**
 * Get all the definitions in the file.
 */
fun PsiFile.definitions(): Collection<LatexCommands> {
    return LatexDefinitionIndex.getItems(this)
            .filter { it.isDefinition() }
}

/**
 * Get all the definitions and redefinitions in the file.
 */
@Suppress("unused")
fun PsiFile.definitionsAndRedefinitions(): Collection<LatexCommands> {
    return LatexDefinitionIndex.getItems(this)
}