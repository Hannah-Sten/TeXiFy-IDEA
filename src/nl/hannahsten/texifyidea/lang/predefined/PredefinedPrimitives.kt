package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.PredefinedCommandSet

object PredefinedPrimitives : PredefinedCommandSet() {
    val primitives = buildCommands {
        val envArg = LArgument.required("env", LatexContexts.Identifier)
        "begin".cmd(envArg)
        "end".cmd(envArg)
        underContext(LatexContexts.Nothing) { // The primitive commands will never be suggested for autocompletion.
            "begingroup".cmd { "Begin a group" }
            "endgroup".cmd { "End a group" }

            "catcode".cmd("char".required) { "Set category code for a character" }
            // the following primitive commands can not be totally covered by the command context
            val paramMacro = LArgument.required("macro", LatexContexts.CommandDeclaration)
            val paramDef = LArgument.required("definition", LatexContexts.InsideDefinition)
            +"def" // \def\cmd#1#2...#9{content}, too complex to handle in the command context
            +"let"
            "edef".cmd(paramMacro, paramDef) { "LaTex primitive edef" }
            "gdef".cmd(paramMacro, paramDef) { "LaTex primitive gdef" }
            "xdef".cmd(paramMacro, paramDef) { "LaTex primitive xdef" }
            "futurelet".cmd(paramMacro, paramDef) { "LaTex primitive futurelet" }
            "relax".cmd { "Do nothing" }
            +"csname"
            +"endcsname"
        }
    }
}