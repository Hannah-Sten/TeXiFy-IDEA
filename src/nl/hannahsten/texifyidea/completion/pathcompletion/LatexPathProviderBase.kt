package nl.hannahsten.texifyidea.completion.pathcompletion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfTypes
import com.intellij.util.PlatformIcons
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.TexifyIcons.FILE
import nl.hannahsten.texifyidea.completion.handlers.CompositeHandler
import nl.hannahsten.texifyidea.completion.handlers.FileNameInsertionHandler
import nl.hannahsten.texifyidea.completion.handlers.LatexReferenceInsertHandler
import nl.hannahsten.texifyidea.lang.commands.RequiredFileArgument
import nl.hannahsten.texifyidea.psi.LatexRequiredParam
import nl.hannahsten.texifyidea.util.expandCommandsOnce
import nl.hannahsten.texifyidea.util.files.findRootFiles
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.replaceAfterFrom
import java.io.File
import java.util.regex.Pattern

/**
 * @author Lukas Heiligenbrunner
 */
abstract class LatexPathProviderBase : CompletionProvider<CompletionParameters>() {

    private var parameters: CompletionParameters? = null
    private var resultSet: CompletionResultSet? = null
    private var validExtensions: List<String>? = null
    private var absolutePathSupport = true
    private var supportsAnyExtension = true

    companion object {

        private val TRIM_SLASH = Pattern.compile("/[^/]*$")
    }

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        this.parameters = parameters

        // We create a result set with the correct autocomplete text as prefix, which may be different when multiple LaTeX parameters (comma separated) are present
        val autocompleteText = processAutocompleteText(parameters.position.parentOfTypes(LatexRequiredParam::class)?.text ?: parameters.position.text)

        val parentCommand = context.get("type")
        if (parentCommand is RequiredFileArgument) {
            validExtensions = parentCommand.supportedExtensions
            absolutePathSupport = parentCommand.isAbsolutePathSupported
            supportsAnyExtension = parentCommand.supportsAnyExtension
        }

        var finalCompleteText = expandCommandsOnce(autocompleteText, project = parameters.originalFile.project, file = parameters.originalFile) ?: autocompleteText

        // Process the expanded text again
        finalCompleteText = processAutocompleteText(finalCompleteText)
        resultSet = result.withPrefixMatcher(finalCompleteText)
        selectScanRoots(parameters.originalFile).forEach {
            addByDirectory(it, finalCompleteText)
        }
    }

    /**
     * return a List of Paths to be searched in
     * eg. project root
     * eg. \includegraphics roots
     */
    abstract fun selectScanRoots(file: PsiFile): Collection<VirtualFile>

    /**
     * enable folder search?
     */
    abstract fun searchFolders(): Boolean

    /**
     * enable file search?
     */
    abstract fun searchFiles(): Boolean

    /**
     * scan directory for completions
     */
    private fun addByDirectory(baseDirectory: VirtualFile, autoCompleteText: String) {
        // Check if path is relative or absolute
        if (File(autoCompleteText).isAbsolute) {
            if (absolutePathSupport) {
                // Split text in path and completion text
                val pathOffset = trimAutocompleteText(autoCompleteText)
                addAbsolutePathCompletion(pathOffset)
            }
        }
        else {
            val pathOffset = trimAutocompleteText(autoCompleteText)
            addRelativePathCompletion(baseDirectory, pathOffset)
        }
    }

    /**
     * add completion entries for relative path
     */
    private fun addRelativePathCompletion(projectDir: VirtualFile, pathOffset: String) {
        // Don't use LocalFileSystem.findByPath here. In a normal IntelliJ, projectDir.path will be the
        // full path (on the local file system) to the project directory, but in tests this will be just "src/"
        // causing LocalFileSystem.findByPath to always return null.
        projectDir.findFileByRelativePath(pathOffset)?.let { baseDir ->
            if (searchFolders()) {
                addFolderNavigations(pathOffset)
                getContents(baseDir, true).forEach {
                    addDirectoryCompletion(pathOffset, it)
                }
            }

            if (searchFiles()) getContents(baseDir, false).forEach {
                addFileCompletion(pathOffset, it)
            }
        }
    }

    /**
     * add completion entries for absolute path
     */
    private fun addAbsolutePathCompletion(baseDir: String) {
        if (baseDir.isBlank()) return
        LocalFileSystem.getInstance().refreshAndFindFileByPath(baseDir)?.let { dirFile ->
            if (searchFolders()) {
                addFolderNavigations(baseDir)
                getContents(dirFile, true).forEach {
                    addDirectoryCompletion(baseDir, it)
                }
            }

            if (searchFiles()) getContents(dirFile, false).forEach {
                addFileCompletion(baseDir, it)
            }
        }
    }

    /**
     * add basic folder navigation options such as ../ and ./
     */
    private fun addFolderNavigations(baseDir: String) {
        // Add current directory.
        resultSet?.addElement(
            LookupElementBuilder.create("$baseDir./")
                .withPresentableText(".")
                .withIcon(PlatformIcons.PACKAGE_ICON)
        )

        // Add return directory.
        resultSet?.addElement(
            LookupElementBuilder.create("$baseDir../")
                .withPresentableText("..")
                .withIcon(PlatformIcons.PACKAGE_ICON)
        )
    }

    /**
     * add Directory to autocompletion dialog
     */
    private fun addDirectoryCompletion(baseDir: String, foundFile: VirtualFile) {
        resultSet?.addElement(
            LookupElementBuilder.create(baseDir + foundFile.name + "/")
                .withPresentableText(foundFile.presentableName)
                .withIcon(PlatformIcons.PACKAGE_ICON)
        )
    }

    /**
     * add file to autocompletion dialog
     */
    private fun addFileCompletion(baseDir: String, foundFile: VirtualFile) {
        // Some commands like \input accept any file extension (supportsExtension), but showing only .tex files is probably a better user experience.
        if (validExtensions != null && validExtensions!!.isNotEmpty() && validExtensions!![0].isNotEmpty()) {
            if (validExtensions!!.contains(foundFile.extension).not()) return
        }

        val icon = TexifyIcons.getIconFromExtension(foundFile.extension, default = FILE)
        resultSet?.addElement(
            LookupElementBuilder.create(baseDir + foundFile.name)
                .withPresentableText(foundFile.nameWithoutExtension)
                .withTailText(".${foundFile.extension}", true)
                .withInsertHandler(
                    CompositeHandler(
                        LatexReferenceInsertHandler(),
                        FileNameInsertionHandler()
                    )
                )
                .withIcon(icon)
        )
    }

    /**
     * Get project roots
     * @return all Project Root directories as VirtualFile
     */
    fun getProjectRoots(): ArrayList<VirtualFile> {
        val resultList = ArrayList<VirtualFile>()

        parameters?.apply {
            // Get base data.
            val baseFile = this.originalFile.virtualFile

            if (this.originalFile.isLatexFile()) {
                this.originalFile.findRootFiles().mapNotNull { it.containingDirectory?.virtualFile }.forEach {
                    resultList.add(it)
                }
            }
            else resultList.add(baseFile.parent)

            val rootManager = ProjectRootManager.getInstance(this.originalFile.project)
            rootManager.contentSourceRoots.asSequence()
                .filter { it != resultList.firstOrNull() }
                .toSet()
                .filterNotNull()
                .forEach { resultList.add(it) }
        }

        return resultList
    }

    /**
     * @param autoCompleteText full path (relative or absolute) including the completion offset
     * @return path without autocompletion text including forward slash at end
     */
    private fun trimAutocompleteText(autoCompleteText: String): String {
        return if (!autoCompleteText.contains("/")) {
            ""
        }
        else TRIM_SLASH.matcher(autoCompleteText).replaceAll("/")
        // delete last subpath occurence
    }

    /**
     * Prepare auto-complete text before searching for files.
     *
     * This does the following:
     * - Removes any start '{' and ending '}'
     * - Remove 'IntelliJIdeaRulezzz'
     * - Removes any arguments before the last one (separated by ',')
     * - Remove starting './'
     * - Prevent '//' (removes the first '/')
     */
    private fun processAutocompleteText(autocompleteText: String): String {
        var result = autocompleteText.dropWhile { it == '{' }.dropLastWhile { it == '}' }.trim()

        // When the last parameter is autocompleted, parameters before that may also be present in
        // autocompleteText so we split on commas and take the last one. If it is not the last
        // parameter, no commas will be present so the split will do nothing.
        result = result.split(",").last()

        // Remove everything that comes after and including IntellijIdeaRulezzz, as that is the dummy for the caret.
        result = result.replaceAfterFrom("IntellijIdeaRulezzz", "")

        // Prevent double ./
        if (result.startsWith("./")) {
            result = result.substring(2)
        }
        // Prevent double ../
        if (result.startsWith("../")) {
            result = result.substring(3)
        }

        // Prevent double /
        if (result.startsWith("//")) {
            result = result.substring(1)
        }

        return result
    }

    /**
     * search in given path for subfiles or directories
     */
    private fun getContents(base: VirtualFile?, directory: Boolean): List<VirtualFile> {
        return base?.children?.filter { it.isDirectory == directory } ?: mutableListOf()
    }
}