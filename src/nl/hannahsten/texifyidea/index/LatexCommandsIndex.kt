package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StringStubIndexExtension
import nl.hannahsten.texifyidea.grammar.LatexParserDefinition
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * @author Hannah Schellekens
 */
open class LatexCommandsIndex : StringStubIndexExtension<LatexCommands>() {

    object Util : IndexCommandsUtilBase(LatexStubIndexKeys.COMMANDS)

    override fun getKey() = Util.key()

    override fun getVersion() = LatexParserDefinition.Cache.FILE.stubVersion
}
