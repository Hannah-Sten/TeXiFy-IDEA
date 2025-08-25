package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.indexing.SingleEntryFileBasedIndexExtension
import com.intellij.util.indexing.SingleEntryIndexer
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.externalizer.StringCollectionExternalizer.STRING_LIST_EXTERNALIZER
import nl.hannahsten.texifyidea.file.ClassFileType
import nl.hannahsten.texifyidea.file.StyleFileType
import nl.hannahsten.texifyidea.index.LatexFileBasedIndexKeys
import nl.hannahsten.texifyidea.index.file.LatexRegexBasedIndex.PACKAGE_FILE_INPUT_FILTER
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.predefined.PredefinedCmdDefinitions

private fun StringBuilder.makeCommandRegex(names: Collection<String>) {
    append(Regex.escape("\\"))
    append("(?:")
    names.joinTo(this, "|") { Regex.escape(it) }
    append(")")
    append("\\*?") // Optional star
}

class LatexRegexBasedDefinitionDataIndexer(val regex: Regex) : SingleEntryIndexer<List<String>>(false) {
    override fun computeValue(inputData: FileContent): List<String> {
        val lines = inputData.contentAsText.lineSequence()
        val names = mutableSetOf<String>()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith('%')) {
                continue
            }
            regex.findAll(trimmed).forEach { match ->
                match.groups["name"]?.let {
                    names.add(it.value)
                }
            }
        }
        return names.toList()
    }
}

abstract class AbstractLatexRegexBasedDefIndexExtension : SingleEntryFileBasedIndexExtension<List<String>>() {

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        // we only use regex for .sty and .cls files
        return PACKAGE_FILE_INPUT_FILTER
    }

    override fun getValueExternalizer(): DataExternalizer<List<String>> {
        return STRING_LIST_EXTERNALIZER
    }
}

object LatexIndexRegexes {
    val COMMAND_DEFINITION = Regex(
        buildString {
            makeCommandRegex(PredefinedCmdDefinitions.namesOfAllCommandDef)
            append("\\s*\\{?\\s*") // white space and optional opening brace
            append(Regex.escape("\\"))
            append("""(?<name>[a-zA-Z@]+\*?)""")
        }
    )

    val ENVIRONMENT_DEFINITION = Regex(
        buildString {
            makeCommandRegex(PredefinedCmdDefinitions.namesOfAllCommandDef)
            append("\\s*\\{\\s*") // white space and optional opening brace
            append("""(?<name>[a-zA-Z@]+\*?)""")
        }
    )
}

class LatexRegexBasedCommandDefIndex : AbstractLatexRegexBasedDefIndexExtension() {

    private val myIndexer = LatexRegexBasedDefinitionDataIndexer(LatexIndexRegexes.COMMAND_DEFINITION)

    override fun getIndexer(): SingleEntryIndexer<List<String>> {
        return myIndexer
    }

    override fun getVersion() = 1

    override fun getName(): ID<Int, List<String>> {
        return LatexFileBasedIndexKeys.REGEX_COMMAND_DEFINITIONS
    }
}

class LatexRegexBasedEnvironmentDefIndex : AbstractLatexRegexBasedDefIndexExtension() {
    private val myIndexer = LatexRegexBasedDefinitionDataIndexer(LatexIndexRegexes.ENVIRONMENT_DEFINITION)

    override fun getIndexer(): SingleEntryIndexer<List<String>> {
        return myIndexer
    }

    override fun getVersion() = 1

    override fun getName(): ID<Int, List<String>> {
        return LatexFileBasedIndexKeys.REGEX_ENVIRONMENT_DEFINITIONS
    }
}

object LatexRegexBasedIndex {
    val PACKAGE_FILE_INPUT_FILTER = DefaultFileTypeSpecificInputFilter(StyleFileType, ClassFileType)

    private fun retrieveIndexData(
        vf: VirtualFile,
        project: Project,
        key: ID<Int, List<String>>
    ): List<String> {
        if (DumbService.isDumb(project)) return emptyList()
        val fIndex = FileBasedIndex.getInstance()
        return fIndex.getSingleEntryIndexData(key, vf, project) ?: emptyList()
    }

    /**
     * Gets the command definitions for the given virtual file from the regex-based index.
     */
    fun getCommandDefinitions(vf: VirtualFile, project: Project): List<String> {
        return retrieveIndexData(vf, project, LatexFileBasedIndexKeys.REGEX_COMMAND_DEFINITIONS)
    }

    /**
     * Gets the environment definitions for the given virtual file from the regex-based index.
     */
    fun getEnvironmentDefinitions(vf: VirtualFile, project: Project): List<String> {
        return retrieveIndexData(vf, project, LatexFileBasedIndexKeys.REGEX_ENVIRONMENT_DEFINITIONS)
    }

    /**
     * Gets the package inclusions for the given virtual file from the regex-based index.
     */
    fun getPackageInclusions(vf: VirtualFile, project: Project): List<String> {
        return retrieveIndexData(vf, project, LatexFileBasedIndexKeys.REGEX_PACKAGE_INCLUSIONS)
    }

    fun processDtxDefinitions(lib: LatexLib, project: Project, callback: (LatexSimpleDefinition) -> Unit) {
        if (DumbService.isDumb(project)) return
        val fIndex = FileBasedIndex.getInstance()
        fIndex.processValues(
            LatexFileBasedIndexKeys.DTX_DEFINITIONS, lib.name, null,
            FileBasedIndex.ValueProcessor { file, definitions ->
                if (file.isValid) {
                    definitions.forEach { definition ->
                        callback(definition)
                    }
                }
                true
            },
            GlobalSearchScope.everythingScope(project)
        )
    }

    fun getDtxDefinitions(lib: LatexLib, project: Project): List<LatexSimpleDefinition> {
        if (DumbService.isDumb(project)) return emptyList()
        val fIndex = FileBasedIndex.getInstance()
        val result = fIndex.getValues(LatexFileBasedIndexKeys.DTX_DEFINITIONS, lib.name, GlobalSearchScope.everythingScope(project)).flatten()
        return result
    }
}