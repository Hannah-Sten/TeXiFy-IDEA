package nl.rubensten.texifyidea.index

import com.intellij.psi.stubs.StringStubIndexExtension
import nl.rubensten.texifyidea.psi.LatexCommands

/**
 * @author Ruben Schellekens
 */
open class LatexCommandsIndex : StringStubIndexExtension<LatexCommands>() {

    companion object : IndexCommandsUtilBase(IndexKeys.COMMANDS_KEY)

    override fun getKey() = Companion.key()
}
