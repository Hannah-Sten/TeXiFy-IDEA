package nl.rubensten.texifyidea.index

import com.intellij.psi.stubs.StringStubIndexExtension
import nl.rubensten.texifyidea.psi.LatexCommands

/**
 * @author Ruben Schellekens
 */
class LatexIncludesIndex : StringStubIndexExtension<LatexCommands>() {

    companion object : IndexCommandsUtilBase(IndexKeys.INCLUDES_KEY)

    override fun getKey() = Companion.key()
}
