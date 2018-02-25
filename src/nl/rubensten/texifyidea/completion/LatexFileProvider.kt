package nl.rubensten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.PlatformIcons
import com.intellij.util.ProcessingContext
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.completion.handlers.CompositeHandler
import nl.rubensten.texifyidea.completion.handlers.FileNameInsertionHandler
import nl.rubensten.texifyidea.completion.handlers.LatexReferenceInsertHandler
import nl.rubensten.texifyidea.util.*
import java.util.*
import java.util.regex.Pattern

/**
 * @author Ruben Schellekens
 */
class LatexFileProvider : CompletionProvider<CompletionParameters>() {

    companion object {

        private val TRIM_SLASH = Pattern.compile("/[^/]*$")
        private val TRIM_BACK = Pattern.compile("\\.\\./")
    }

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        // Get base data.
        val baseFile = parameters.originalFile.virtualFile
        val autocompleteText = processAutocompleteText(parameters.originalPosition!!.text)

        val baseDirectory = if (parameters.originalFile.isLatexFile()) {
            parameters.originalFile.findRootFile().containingDirectory.virtualFile
        }
        else baseFile.parent

        addByDirectory(baseDirectory, autocompleteText, result)

        // When the base directory is a sources directory => add items of all source directories.
        val rootManager = ProjectRootManager.getInstance(parameters.originalFile.project)
        rootManager.contentSourceRoots.asSequence()
                .filter { it != baseDirectory }
                .toSet()
                .forEach { addByDirectory(it, autocompleteText, result) }

        // Add all included graphicspaths.
        val graphicsPaths = parameters.originalFile.commandsInFileSet().filter { it.commandToken.text == "\\graphicspath" }
        graphicsPaths.asSequence()
                .mapNotNull { it.requiredParameter(0) }
                .mapNotNull { it.println(); baseDirectory.findFileByRelativePath(it) }
                .filter { it.isDirectory && it != baseDirectory }
                .toSet()
                .forEach { addByDirectory(it, autocompleteText, result) }
    }

    private fun addByDirectory(baseDirectory: VirtualFile, autoCompleteText: String, result: CompletionResultSet) {
        val directoryPath = baseDirectory.path + "/" + autoCompleteText
        var searchDirectory = getByPath(directoryPath)
        var autocompleteText = autoCompleteText

        if (searchDirectory == null) {
            autocompleteText = trimAutocompleteText(autocompleteText)
            searchDirectory = if (autocompleteText.isEmpty()) {
                baseDirectory
            }
            else {
                getByPath(baseDirectory.path + "/" + autocompleteText)
            }
        }

        if (searchDirectory == null) {
            return
        }

        if (autocompleteText.isNotEmpty() && !autocompleteText.endsWith("/")) {
            return
        }

        // Find stuff.
        val directories = getContents(searchDirectory, true)
        val files = getContents(searchDirectory, false)

        // Add directories.
        for (directory in directories) {
            val directoryName = directory.presentableName
            result.addElement(
                    LookupElementBuilder.create(noBack(autocompleteText) + directory.name)
                            .withPresentableText(directoryName)
                            .withIcon(PlatformIcons.PACKAGE_ICON)
            )
        }

        // Add return directory.
        result.addElement(
                LookupElementBuilder.create("..")
                        .withIcon(PlatformIcons.PACKAGE_ICON)
        )

        // Add files.
        for (file in files) {
            val fileName = file.presentableName
            val icon = TexifyIcons.getIconFromExtension(file.extension)
            result.addElement(
                    LookupElementBuilder.create(noBack(autocompleteText) + file.name)
                            .withPresentableText(fileName)
                            .withInsertHandler(CompositeHandler<LookupElement>(
                                    LatexReferenceInsertHandler(),
                                    FileNameInsertionHandler()
                            ))
                            .withIcon(icon)
            )
        }

        result.addLookupAdvertisement(Kindness.getKindWords())
    }

    private fun noBack(stuff: String): String {
        return TRIM_BACK.matcher(stuff).replaceAll("")
    }

    private fun trimAutocompleteText(autoCompleteText: String): String {
        return if (!autoCompleteText.contains("/")) {
            ""
        }
        else TRIM_SLASH.matcher(autoCompleteText).replaceAll("/")

    }

    private fun processAutocompleteText(autocompleteText: String): String {
        var result = if (autocompleteText.endsWith("}")) {
            autocompleteText.substring(0, autocompleteText.length - 1)
        }
        else autocompleteText

        if (result.endsWith(".")) {
            result = result.substring(0, result.length - 1) + "/"
        }

        // Prevent double ./
        if (result.startsWith("./")) {
            result = result.substring(2)
        }

        // Prevent double /
        if (result.startsWith("/")) {
            result = result.substring(1)
        }

        return result
    }

    private fun getByPath(path: String): VirtualFile? {
        val fileSystem = LocalFileSystem.getInstance()
        return fileSystem.findFileByPath(path)
    }

    private fun getContents(base: VirtualFile?, directory: Boolean): List<VirtualFile> {
        val contents = ArrayList<VirtualFile>()

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
