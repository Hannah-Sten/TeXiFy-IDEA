package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StringStubIndexExtension
import nl.hannahsten.texifyidea.grammar.LatexParserDefinition
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * @author Hannah Schellekens
 */
open class LatexCommandsIndex : StringStubIndexExtension<LatexCommands>() {

    companion object : IndexCommandsUtilBase(IndexKeys.COMMANDS_KEY)

    @Suppress("RedundantCompanionReference") // Avoid type checking issues
    override fun getKey() = Companion.key()

    override fun getVersion() = LatexParserDefinition.Cache.FILE.stubVersion
}
