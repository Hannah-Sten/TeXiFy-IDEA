package nl.hannahsten.texifyidea.util

import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.firstParentOfType


fun LatexCommands.isInsideDefinition(): Boolean{
    // command - parameter - required_parameter - required_param_content - this
    // we leave some space
    val parentParameter = firstParentOfType<LatexParameter>(5) ?: return false
    val defCommand = parentParameter.firstParentOfType<LatexCommands>(1) ?: return false
    val name = defCommand.name
    if(name !in CommandMagic.commandDefinitionsAndRedefinitions) return false
    // TODO: deal with \let\Cmd\Something
    return defCommand.parameterList.firstOrNull() === parentParameter
}

