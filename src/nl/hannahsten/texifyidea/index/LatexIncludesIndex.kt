package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StringStubIndexExtension
import nl.hannahsten.texifyidea.grammar.LatexParserDefinition
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * Index all commands that include other files, see [nl.hannahsten.texifyidea.util.getIncludeCommands].
 * @author Hannah Schellekens
 */
class LatexIncludesIndex : StringStubIndexExtension<LatexCommands>() {

    object Util : IndexCommandsUtilBase(IndexKeys.INCLUDES_KEY)

    override fun getKey() = Util.key()

    override fun getVersion() = LatexParserDefinition.Cache.FILE.stubVersion
}
