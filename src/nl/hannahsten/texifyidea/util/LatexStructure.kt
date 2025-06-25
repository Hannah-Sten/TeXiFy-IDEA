package nl.hannahsten.texifyidea.util

import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexComposite
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.firstParentOfType

fun LatexComposite.isInsideDefinition(): Boolean {
    // TODO: deal with \let\Cmd\Something
    // command - parameter - required_parameter - required_param_content - parameter_text - command
    //                                                                                    - NormalTextWord
    // we leave some space
    val parentParameter = firstParentOfType<LatexParameter>(5) ?: return false
    val defCommand = parentParameter.firstParentOfType<LatexCommands>(1) ?: return false
    val name = defCommand.name
    if(name !in CommandMagic.definitions) return false

    return defCommand.parameterList.firstOrNull() === parentParameter
}

