package nl.hannahsten.texifyidea.util.files

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.*
import nl.hannahsten.texifyidea.file.BibtexFileType
import nl.hannahsten.texifyidea.file.ClassFileType
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.file.StyleFileType
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.index.LatexEnvironmentsIndex
import nl.hannahsten.texifyidea.index.LatexIncludesIndex
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.util.getLatexRunConfigurations
import nl.hannahsten.texifyidea.util.includedPackages
import nl.hannahsten.texifyidea.util.isTestProject
import nl.hannahsten.texifyidea.util.magic.FileMagic
import nl.hannahsten.texifyidea.util.parser.*

/**
 * Get the file search scope for this psi file.
 */
val PsiFile.fileSearchScope: GlobalSearchScope
    get() = runReadAction { GlobalSearchScope.fileScope(this) }

/**
 * Looks for all file inclusions in a given file, excluding installed LaTeX packages.
 *
 * @return A list containing all included files.
 */
fun PsiFile.findInclusions(): List<PsiFile> {
    return LatexIncludesIndex.Util.getItems(this)
        .flatMap { it.getIncludedFiles(false) }
        .toList()
}

/**
 * Checks if the file has LaTeX syntax.
 * Not every psi file has a virtualfile, e.g. in intention preview
 */
fun PsiFile.isLatexFile() = fileType == LatexFileType || fileType == StyleFileType || fileType == ClassFileType

fun VirtualFile.isLatexFile() = fileType == LatexFileType || fileType == StyleFileType || fileType == ClassFileType

/**
 * Checks if the file has a `.sty` extention. This is a workaround for file type checking.
 */
fun PsiFile.isStyleFile() = virtualFile?.extension == "sty"

/**
 * Checks if the file has a `.cls` extention. This is a workaround for file type checking.
 */
fun PsiFile.isClassFile() = virtualFile?.extension == "cls"

/**
 * Looks up the argument that is in the documentclass command, and if the file is found in the project return it.
 * Note this explicitly does not find files elsewhere on the system.
 */
fun PsiFile.documentClassFileInProject() = findFile("${documentClass()}.cls", supportsAnyExtension = true)

/**
 * If the file has a \documentclass command, return the class name, null otherwise.
 */
fun PsiFile.documentClass(): String? {
    return commandsInFile().asSequence()
        .filter { it.name == "\\documentclass" }
        .firstOrNull()
        ?.requiredParameter(0)
}

/**
 * Checks if the given package is included in the file set.
 *
 * @param packageName
 *          The name of the package to check for.
 * @return `true` when there is a package with name `packageName` in the file set, `false` otherwise.
 */
fun PsiFile.isUsed(packageName: String) = this.includedPackages().map { it.name }.contains(packageName)

/**
 * Checks if the given package is included into the file set.
 *
 * @param `package`
 *          The package to check for.
 * @return `true` when there is a package `package` included in the file set, `false` otherwise.
 */
@Suppress("unused")
fun PsiFile.isUsed(`package`: LatexPackage) = isUsed(`package`.name)

/**
 * Scans the whole document (recursively) for all referenced/included files, except installed LaTeX packages.
 * Never use this directly, use the cached [referencedFileSet] instead.
 *
 * @return A collection containing all the PsiFiles that are referenced from this file.
 */
// Suppress for Qodana only
@Suppress("RedundantSuspendModifier", "RedundantSuppression")
internal suspend fun PsiFile.referencedFiles(rootFile: VirtualFile, isImportPackageUsed: Boolean, usesLuatexPaths: Boolean): Set<PsiFile> {
    // Using a single set avoids infinite loops
    val result = mutableSetOf<PsiFile>()
    referencedFiles(result, rootFile, isImportPackageUsed, usesLuatexPaths)
    return result
}

@Suppress("RedundantSuspendModifier", "RedundantSuppression")
internal suspend fun PsiFile.referencedFiles(files: MutableCollection<PsiFile>, rootFile: VirtualFile, isImportPackageUsed: Boolean, usesLuatexPaths: Boolean) {
    LatexIncludesIndex.Util.getItemsNonBlocking(project, fileSearchScope).forEach command@{ command ->
        smartReadAction(project) { command.references }.filterIsInstance<InputFileReference>()
            .mapNotNull { smartReadAction(project) { it.resolve(false, rootFile, true, checkImportPath = isImportPackageUsed, checkAddToLuatexPath = usesLuatexPaths) } }
            .forEach {
                // Do not re-add all referenced files if we already did that
                if (it in files) return@forEach
                files.add(it)
                it.referencedFiles(files, rootFile, isImportPackageUsed, usesLuatexPaths)
            }
    }
}

/**
 * Looks up a file relative to this file.
 *
 * @param path The path relative to this file.
 * @param extensions Search for extensions in this order
 * @param supportsAnyExtension If true, the found file is accepted even if the extension is not in the provided non-empty list.
 * @return The found file, or `null` when the file could not be found.
 */
fun PsiFile.findFile(path: String, extensions: List<String>? = null, supportsAnyExtension: Boolean): PsiFile? {
    if (project.isDisposed) return null
    val directory = containingDirectory?.virtualFile

    val file = directory?.findFile(path, extensions ?: FileMagic.includeExtensions, supportsAnyExtension = supportsAnyExtension) ?: return scanRoots(path, extensions)
    val psiFile = PsiManager.getInstance(project).findFile(file)
    if (psiFile == null || LatexFileType != psiFile.fileType &&
        StyleFileType != psiFile.fileType &&
        BibtexFileType != psiFile.fileType
    ) {
        return scanRoots(path, extensions)
    }

    return psiFile
}

/**
 * Looks up the file(s) included by a command relative to this file.
 *
 * @param command
 *         The include command
 * @return The found file(s), or an empty set when no files could be found.
 */
fun PsiFile.findIncludedFile(command: LatexCommands): Set<PsiFile> {
    val arguments = command.getAllRequiredArguments() ?: return emptySet()

    return arguments.filter { it.isNotEmpty() }.mapNotNull {
        val extension = FileMagic.automaticExtensions[command.name]
        if (extension != null) {
            findFile(it, listOf(extension), supportsAnyExtension = true)
        }
        else {
            findFile(it, supportsAnyExtension = true)
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
fun PsiFile.scanRoots(path: String, extensions: List<String>? = null): PsiFile? {
    val rootManager = ProjectRootManager.getInstance(project)
    rootManager.contentSourceRoots.forEach { root ->
        val file = root.findFile(path, extensions ?: FileMagic.includeExtensions, supportsAnyExtension = true)
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
 * @param commandName
 *          The name of the command including a backslash, or `null` for all commands.
 *
 * see LatexCommandsIndex.Util.getItems
 */
@JvmOverloads
fun PsiFile.commandsInFile(commandName: String? = null): Collection<LatexCommands> {
    return commandName?.let {
        this.allCommands().filter { it.name == commandName }
    } ?: this.allCommands()
}

/**
 * @see [LatexEnvironmentsIndex.Util.getItems]
 */
fun PsiFile.environmentsInFile(): Collection<LatexEnvironment> = LatexEnvironmentsIndex.Util.getItems(this)

/**
 * Get the editor of the file if it is currently opened. Note that the returned editor does not have to be a text editor,
 * e.g., when this file is a PDF file, the editor will be a PDF editor and not a text editor.
 *
 * @return null if the file is not opened.
 */
fun PsiFile.openedEditor(): FileEditor? {
    return FileEditorManager.getInstance(project).getSelectedEditor(virtualFile ?: return null)
}

/**
 * Get the text editor instance of the (text) file if it is currently opened.
 *
 * @return null if the file is not opened in a text editor.
 */
fun PsiFile.openedTextEditor(): Editor? = openedEditor()?.let {
    when (it) {
        is TextEditor -> it.editor
        else -> null
    }
}

/**
 * Get all the definitions in the file.
 */
fun PsiFile.definitions(): Collection<LatexCommands> {
    return LatexDefinitionIndex.Util.getItems(this)
        .filter { it.isDefinition() }
}

/**
 * Get all the definitions and redefinitions in the file.
 */
@Suppress("unused")
fun PsiFile.definitionsAndRedefinitions(): Collection<LatexCommands> {
    return LatexDefinitionIndex.Util.getItems(this)
}

/**
 * Get all bibtex run configurations that are probably used to compile this file.
 */
fun PsiFile.getBibtexRunConfigurations() = project
    .getLatexRunConfigurations()
    .filter { it.mainFile == findRootFile().virtualFile }
    .flatMap { it.bibRunConfigs }
    .map { it.configuration }
    .filterIsInstance<BibtexRunConfiguration>()

/**
 * Gets the smallest extractable expression at the given offset
 */
fun PsiFile.expressionAtOffset(offset: Int): PsiElement? {
    val element = findElementAt(offset) ?: return null

    return element.parents(true)
        .firstOrNull { it.elementType == LatexTypes.NORMAL_TEXT_WORD || it is LatexNormalText || it is LatexParameter || it is LatexMathContent || it is LatexCommandWithParams }
}

/**
 * Get "expression" within range specified. An expression is either a PsiElement, or a PsiElement with a specific extraction range in the case that the range lies entirely within a text block
 */
fun PsiFile.findExpressionInRange(startOffset: Int, endOffset: Int): LatexExtractablePSI? {
    val firstUnresolved = findElementAt(startOffset) ?: return null
    val startElement =
        if (firstUnresolved is PsiWhiteSpace)
            findElementAt(firstUnresolved.endOffset) ?: return null
        else
            firstUnresolved

    val lastUnresolved = findElementAt(endOffset - 1) ?: return null
    val endElement =
        if (lastUnresolved is PsiWhiteSpace)
            findElementAt(lastUnresolved.startOffset - 1) ?: return null
        else
            lastUnresolved

    val commonParent = PsiTreeUtil.findCommonParent(startElement, endElement) ?: return null

    // We will consider an exression to be a sentence or a substring out of text. Here we will mark that in the extraction range.
    return if (commonParent is LatexNormalText) {
        commonParent.asExtractable(TextRange(startOffset - commonParent.startOffset, endOffset - commonParent.startOffset))
    }
    else
        commonParent.asExtractable()
}

/**
 * Attempts to find the "expression" at the given offset
 */
fun PsiFile.findExpressionAtCaret(offset: Int): PsiElement? {
    val expr = expressionAtOffset(offset)
    val exprBefore = expressionAtOffset(offset - 1)
    return when {
        expr == null -> exprBefore
        exprBefore == null -> expr
        PsiTreeUtil.isAncestor(expr, exprBefore, false) -> exprBefore
        else -> expr
    }
}

fun PsiFile.rerunInspections() {
    if (!project.isTestProject()) {
        // PSI/document/model changes are not allowed during highlighting in tests
        DaemonCodeAnalyzer.getInstance(project).restart(this)
    }
}