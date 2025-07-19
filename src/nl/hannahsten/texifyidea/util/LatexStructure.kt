package nl.hannahsten.texifyidea.util

import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexComposite
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.psi.nextContextualSibling
import nl.hannahsten.texifyidea.psi.prevContextualSibling
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped
import nl.hannahsten.texifyidea.util.parser.firstParentOfType

/**
 * Utility functions for the Latex file structure.
 */
object LatexStructure {

    /**
     * Get the name of the command that is defined by a definition command stub.
     */
    fun getDefinedCommandName(defStub: LatexCommandsStub): String? {
        if (defStub.requiredParams.isNotEmpty()) {
            return defStub.requiredParams[0].trim()
        }
        // \def\cmd\something
        // find the next command after it, which can be a bit slow, but I don't know a better way
        val children = defStub.parentStub!!.childrenStubs
        val siblingIndex = children.indexOfFirst { it === defStub } + 1
        if (siblingIndex < 0 || siblingIndex >= children.size) return null
        val sibling = children[siblingIndex] as? LatexCommandsStub ?: return null
        // let us not treat \let\cmd\relax as a definition
        val nextSibling = children.getOrNull(siblingIndex + 1)
        if (nextSibling is LatexCommandsStub && nextSibling.commandToken == "\\relax") {
            return null
        }
        return sibling.commandToken
    }

    fun getDefinedCommandName(defCommand: LatexCommands): String? {
        val defStub = defCommand.stub
        if (defStub != null) {
            return getDefinedCommandName(defStub)
        }
        // we use the PSI tree now, since the operation of finding the next command seems to be expensive with stubs
        if (defCommand.parameterList.isNotEmpty()) {
            return defCommand.parameterList[0].findFirstChildTyped<LatexCommands>()?.name
        }
        // \def\cmd\something
        val nextCommand = defCommand.nextContextualSibling { it is LatexCommands } as? LatexCommands ?: return null
        return nextCommand.name
    }

    fun isInsideDefinition(cmd: LatexComposite): Boolean {
        return isInsideNewCommandDef(cmd) || isInsidePlainDef(cmd)
    }

    /**
     * Check if the command is inside a definition command as a parameter, like `\newcommand{\cmd}{}`.
     */
    private fun isInsideNewCommandDef(cmd: LatexComposite): Boolean {
        // command - parameter - required_parameter - required_param_content - parameter_text - command
        //                                                                                    - NormalTextWord
        // we leave some space
        val parentParameter = cmd.firstParentOfType<LatexParameter>(5) ?: return false
        val defCommand = parentParameter.firstParentOfType<LatexCommands>(1) ?: return false
        val name = defCommand.name
        if (name !in CommandMagic.definitions) return false
        return defCommand.firstParameter() === parentParameter // they should be exactly the same object
    }

    private fun isInsidePlainDef(cmd: LatexComposite): Boolean {
        // \def\cmd\something
        val prevCmd = cmd.prevContextualSibling { it is LatexCommands } as? LatexCommands ?: return false
        if(prevCmd.name !in CommandMagic.definitions) return false
        if(prevCmd.hasRequiredParameter()) return false
        return true
    }
}
