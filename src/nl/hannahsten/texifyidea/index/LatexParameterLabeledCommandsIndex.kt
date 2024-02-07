package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StringStubIndexExtension
import nl.hannahsten.texifyidea.grammar.LatexParserDefinition
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * This index contains commands that define a label in their  optional parameters.
 */
class LatexParameterLabeledCommandsIndex : StringStubIndexExtension<LatexCommands>() {

    object Util : IndexUtilBase<LatexCommands>(LatexCommands::class.java, IndexKeys.LABELED_COMMANDS_KEY)

    override fun getKey() = Util.key()

    override fun getVersion() = LatexParserDefinition.Cache.FILE.stubVersion
}