package nl.hannahsten.texifyidea.index.file

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent

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

    override fun map(inputData: FileContent): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()

        LatexDocsRegexer.getDocsByRegex(inputData, map, macroWithDocsRegex)
        LatexDocsRegexer.getDocsByRegex(inputData, map, describeMacroRegex)
        return map
    }
}