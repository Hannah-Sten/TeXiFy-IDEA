package nl.hannahsten.texifyidea.index.file

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent
import nl.hannahsten.texifyidea.util.magic.CommandMagic

/**
 * Similar to [LatexExternalCommandDataIndexer] but for environments.
 *
 * @author Thomas
 */
class LatexExternalEnvironmentDataIndexer : DataIndexer<String, String, FileContent> {

    /**
     * See [LatexExternalCommandDataIndexer].
     */
    private val macroWithDocsRegex = """(?=\\begin\{environment}\{(?<key>[a-zA-Z_:]+)}\s*(?<value>[\s\S]{0,500}))""".toRegex()

    private val describeMacroRegex =
        """(?=\\DescribeEnv\{?(?<key>[a-zA-Z_:]+\*?)}?\s*(?<value>[\s\S]{0,500}))""".toRegex()

    // e.g. \newenvironment{proof}{...} \newenvironment|\newtheorem|\NewDocumentEnvironment|\DeclareDocumentEnvironment|\ProvideDocumentEnvironment
    private val directDefinitionRegex by lazy {
        """(${CommandMagic.environmentDefinitions.joinToString("|").replace("\\", "\\\\")})[*<>]*\{(?<key>[a-zA-Z_:]+)}(?<value>)""".toRegex()
    }

    override fun map(inputData: FileContent): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()

        LatexDocsRegexer.getDocsByRegex(inputData, map, macroWithDocsRegex)
        LatexDocsRegexer.getDocsByRegex(inputData, map, describeMacroRegex)
        LatexDocsRegexer.getDocsByRegex(inputData, map, directDefinitionRegex)
        return map
    }
}