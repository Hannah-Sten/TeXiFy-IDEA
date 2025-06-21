package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StringStubIndexExtension
import nl.hannahsten.texifyidea.grammar.LatexParserDefinition
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * @author Hannah Schellekens
 */
open class LatexDefinitionIndex : StringStubIndexExtension<LatexCommands>() {

    object Util : IndexCommandsUtilBase(LatexStubIndexKeys.DEFINITIONS_KEY)

    override fun getKey() = Util.key()

    override fun getVersion() = LatexParserDefinition.Cache.FILE.stubVersion
}