package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StringStubIndexExtension
import nl.hannahsten.texifyidea.LatexParserDefinition
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * This index contains commands that define a label in their  optional parameters.
 */
class LatexParameterLabeledCommandsIndex : StringStubIndexExtension<LatexCommands>() {

    companion object : IndexUtilBase<LatexCommands>(LatexCommands::class.java, IndexKeys.LABELED_COMMANDS_KEY)

    @Suppress("RedundantCompanionReference")
    override fun getKey() = Companion.key()

    override fun getVersion() = LatexParserDefinition.FILE.stubVersion
}