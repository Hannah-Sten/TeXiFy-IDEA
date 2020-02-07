package nl.hannahsten.texifyidea.completion.pathcompletion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.PlatformIcons
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.handlers.CompositeHandler
import nl.hannahsten.texifyidea.completion.handlers.FileNameInsertionHandler
import nl.hannahsten.texifyidea.completion.handlers.LatexReferenceInsertHandler
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.isLatexFile
import java.io.File
import java.util.regex.Pattern

/**
 * @author Lukas Heiligenbrunner
 */
abstract class LatexPathProviderBase : CompletionProvider<CompletionParameters>() {


    private var parameters: CompletionParameters? = null
    private var resultSet: CompletionResultSet? = null

    companion object {

        private val TRIM_SLASH = Pattern.compile("/[^/]*$")
        private val TRIM_BACK = Pattern.compile("\\.\\./")
    }

    init {

    }


    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        this.parameters = parameters

        // We create a result set with the correct autocomplete text as prefix, which may be different when multiple LaTeX parameters (comma separated) are present
        val autocompleteText = processAutocompleteText(parameters.position.text)
        resultSet = result.withPrefixMatcher(autocompleteText)

        selectScanRoots(parameters.originalFile).forEach {
            addByDirectory(it, autocompleteText, result)
        }
    }

    /**
     * return a List of Paths to be searched in
     * eg. project root
     * eg. \includegraphics roots
     */
    abstract fun selectScanRoots(file: PsiFile): ArrayList<VirtualFile>

    abstract fun searchFolders(): Boolean

    abstract fun searchFiles(): Boolean

    private fun addByDirectory(baseDirectory: VirtualFile, completionText: String, givenResultSet: CompletionResultSet) {

        println()
        println("baseDirectory.path=" + baseDirectory.path)
        println("autocompletetext=" + completionText)

        val result = givenResultSet.withPrefixMatcher(completionText)

        var searchDirectory: VirtualFile? = null
        var autoCompleteText = completionText

        var pathOffset = ""

        // search for right VirtualFile for searchfolder
        // Check if path is relative or absolute
        if (File(autoCompleteText).isAbsolute) {
            println("is absolute path")

            // Split text in path and completion text
            pathOffset = trimAutocompleteText(autoCompleteText)
            autoCompleteText = autoCompleteText.replaceBeforeLast(pathOffset, "")

            baseDirectory.fileSystem.findFileByPath(pathOffset)?.apply {
                searchDirectory = this
            }
        }
        else {
            val directoryPath = baseDirectory.path + "/" + autoCompleteText
            println("direcotrypath=" + directoryPath)
            println("is relative path")
            LocalFileSystem.getInstance().findFileByPath(directoryPath)?.apply {
                searchDirectory = this
                pathOffset = if (autoCompleteText != "")
                    autoCompleteText.substring(0, autoCompleteText.length - 1)
                else ""
                println("founf file=" + this.path)
            }

            // scan dir without completiontext if not found
            if (searchDirectory == null) {
                pathOffset = trimAutocompleteText(autoCompleteText)
                autoCompleteText = autoCompleteText.replaceBeforeLast(pathOffset, "")
                println("pathoffset=" + pathOffset)
                searchDirectory = if (pathOffset.isEmpty()) {
                    println("assigned basedirectory")
                    baseDirectory

                }
                else {
                    println("assigned trimmed dings")
                    LocalFileSystem.getInstance().findFileByPath(baseDirectory.path + "/" + pathOffset)
                }
            }

        }

        if (searchDirectory == null) {
            println("isnull")
            return
        }

        println("autocompletetext after=" + autoCompleteText)

        if (searchFolders()) {
            // Find stuff.
            val directories = getContents(searchDirectory, true)
            println("totalfoundfolders=" + directories.size)

            // Add return directory.
            result.addElement(
                    LookupElementBuilder.create(pathOffset + "../")
                            .withPresentableText("..")
                            .withIcon(PlatformIcons.PACKAGE_ICON)
                    // todo per relative path typed ../ completion doesnt work
            )

            // Add curr directory.
            result.addElement(
                    LookupElementBuilder.create(pathOffset + "./")
                            .withPresentableText(".")
                            .withIcon(PlatformIcons.PACKAGE_ICON)
            )

            // Add directories.
            for (directory in directories) {
                println(directory.name)
//                if(!directory.name.contains(autoCompleteText)) continue
                println("continued!")
                val directoryName = directory.presentableName
                println("presenatablename=" + directory.presentableName)
                result.addElement(
                        LookupElementBuilder.create(pathOffset + directory.name + "/")
                                .withPresentableText(directoryName)
                                .withIcon(PlatformIcons.PACKAGE_ICON)
                )
                println("FINISH=" + pathOffset + directory.name + "/")
            }
        }

        if (searchFiles()) {
            val files = getContents(searchDirectory, false)
            // Add files.
            for (file in files) {
                val fileName = file.presentableName
                val icon = TexifyIcons.getIconFromExtension(file.extension)
                result.addElement(
                        LookupElementBuilder.create(noBack(autoCompleteText) + file.name)
                                .withPresentableText(fileName)
                                .withInsertHandler(CompositeHandler<LookupElement>(
                                        LatexReferenceInsertHandler(),
                                        FileNameInsertionHandler()
                                ))
                                .withIcon(icon)
                )
            }
        }

    }


    public fun getProjectRoots(): ArrayList<VirtualFile> {
        val resultList = ArrayList<VirtualFile>()
        if (parameters == null) return resultList
        // Get base data.
        val baseFile = parameters!!.originalFile.virtualFile

        if (parameters!!.originalFile.isLatexFile()) {
            resultList.add(parameters!!.originalFile.findRootFile().containingDirectory.virtualFile)
        }
        else resultList.add(baseFile.parent)

        val rootManager = ProjectRootManager.getInstance(parameters!!.originalFile.project)
        rootManager.contentSourceRoots.asSequence()
                .filter { it != resultList.first() }
                .toSet()
                .forEach { resultList.add(it) }

        return resultList
    }


    private fun trimAutocompleteText(autoCompleteText: String): String {
        return if (!autoCompleteText.contains("/")) {
            ""
        }
        else TRIM_SLASH.matcher(autoCompleteText).replaceAll("/")
        // delete last subpath occurence
    }

    private fun noBack(stuff: String): String {
        return TRIM_BACK.matcher(stuff).replaceAll("")
    }


    /**
     * prepare auto-complete text
     */
    private fun processAutocompleteText(autocompleteText: String): String {
        var result = if (autocompleteText.endsWith("}")) {
            autocompleteText.substring(0, autocompleteText.length - 1)
        }
        else autocompleteText

        // When the last parameter is autocompleted, parameters before that may also be present in
        // autocompleteText so we split on commas and take the last one. If it is not the last
        // parameter, no commas will be present so the split will do nothing.
        result = result.replace("IntellijIdeaRulezzz", "")
                .split(",").last()

        if (result.endsWith(".")) {
            result = result.substring(0, result.length - 1) + "/"
        }

        // Prevent double ./
        if (result.startsWith("./")) {
            result = result.substring(2)
        }

        // Prevent double /
        if (result.startsWith("//")) {
            result = result.substring(1)
        }

        return result
    }

    private fun getContents(base: VirtualFile?, directory: Boolean): List<VirtualFile> {
        val contents = java.util.ArrayList<VirtualFile>()

        if (base == null) {
            return contents
        }

        for (file in base.children) {
            if (file.isDirectory == directory) {
                contents.add(file)
            }
        }

        return contents
    }
}