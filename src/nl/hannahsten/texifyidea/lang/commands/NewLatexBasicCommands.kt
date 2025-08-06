package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LArgument.Companion.required
import nl.hannahsten.texifyidea.lang.LClearContext
import nl.hannahsten.texifyidea.lang.PredefinedCommandSet
import nl.hannahsten.texifyidea.lang.LatexContexts

object NewLatexBasicCommands : PredefinedCommandSet() {


    val primitives = buildCommands {
        val envArg = required("environment", LatexContexts.Identifier)
        "begin".cmd(envArg)
        "end".cmd(envArg)


        underContext(LatexContexts.Nothing) { // The primitive commands will never be suggested for autocompletion.
            "begingroup".cmd { "Begin a group" }
            "endgroup".cmd { "End a group" }

            "catcode".cmd("char".required) { "Set category code for a character" }
            // the following primitive commands can not be totally covered by the command context
            val paramMacro = required("macro", LatexContexts.CommandDeclaration)
            val paramDef = required("definition", LatexContexts.InsideDefinition)
            +"def" // \def\cmd#1#2...#9{content}, too complex to handle in the command context
            "let".cmd(paramMacro, paramDef) { "LaTex primitive let" }
            "edef".cmd(paramMacro, paramDef) { "LaTex primitive edef" }
            "gdef".cmd(paramMacro, paramDef) { "LaTex primitive gdef" }
            "xdef".cmd(paramMacro, paramDef) { "LaTex primitive xdef" }
            "futurelet".cmd(paramMacro, paramDef) { "LaTex primitive futurelet" }
            "relax".cmd { "Do nothing" }
        }

    }


    val definitionCommands = buildCommands {
        setCommandContext(LatexContexts.Preamble)

        val command = required("cmd", LatexContexts.CommandDeclaration)
        val envName = required("name", LatexContexts.EnvironmentDeclaration)
        val numArgs = LArgument.optional("num args", LatexContexts.Numeric)
        val defaultOptional = "default".optional
        // The following context can be deeper
        val definition = required("definition", +LatexContexts.InsideDefinition)
        val begdef = required("begdef", +LatexContexts.InsideDefinition)
        val enddef = required("enddef", +LatexContexts.InsideDefinition)


        "newcommand".cmd(command, numArgs, defaultOptional, definition) { "Define a new command" }
        "newcommand*".cmd(command, numArgs, defaultOptional, definition) { "Define a new command (starred variant)" }

        "newif".cmd(command) { "Define a new if conditional" }

        "providecommand".cmd(command, numArgs, defaultOptional, definition) { "Provide a command if not defined" }
        "providecommand*".cmd(command, numArgs, defaultOptional, definition) { "Provide a command if not defined (starred variant)" }

        "renewcommand".cmd(command, numArgs, defaultOptional, definition) { "Redefine an existing command" }
        "renewcommand*".cmd(command, numArgs, defaultOptional, definition) { "Redefine an existing command (starred variant)" }

        "newenvironment".cmd(envName, numArgs, defaultOptional, begdef, enddef) { "Define a new environment" }
        "renewenvironment".cmd(envName, numArgs, defaultOptional, begdef, enddef) { "Redefine an existing environment" }


        "newtheorem".cmd(
            envName, "numberedlike".optional, "caption".required(LatexContexts.Text), "within".optional
        ) { "Define a new theorem-like environment" }
        "newtheorem*".cmd(
            envName, "caption".required(LatexContexts.Text)
        ) { "Define a new theorem-like environment" }

        packageOf("xargs")
        "newcommandx".cmd(command, numArgs, defaultOptional, definition) { "Define a new command with extended args" }
        "renewcommandx".cmd(command, numArgs, defaultOptional, definition) { "Redefine a command with extended args" }
        "providecommandx".cmd(command, numArgs, defaultOptional, definition) { "Provide a command with extended args" }
        "DeclareRobustCommandx".cmd(command, numArgs, defaultOptional, definition) { "Declare a robust command with extended args" }

        "newenvironmentx".cmd(command, numArgs, defaultOptional, begdef, enddef) { "Define a new environment with extended args" }
        "renewenvironmentx".cmd(command, numArgs, defaultOptional, begdef, enddef) { "Redefine an environment with extended args" }
    }


    val ifCommands = buildCommands {
        underContext(LatexContexts.Preamble) {
            +"if"
            +"ifcat"
            +"ifx"
            +"ifcase"
            +"ifnum"
            +"ifodd"
            +"ifhmode"
            +"ifvmode"
            +"ifmmode"
            +"ifinner"
            +"ifdim"
            +"ifvoid"
            +"ifhbox"
            +"ifvbox"
            +"ifeof"
            +"iftrue"
            +"iffalse"
            +"fi"
            +"else"
            +"or"
        }
    }

    val xparseCommands = buildCommands {
        setCommandContext(LatexContexts.Preamble)

        val cmdName = "name".required(LatexContexts.CommandDeclaration)
        val envName = "name".required(LatexContexts.EnvironmentDeclaration)
        val argsSpec = "args spec".required(LatexContexts.Literal)
        val code = required("code", LClearContext)
        val startCode = required("start code", LClearContext)
        val endCode = required("end code", LClearContext)

        "NewDocumentCommand".cmd(cmdName, argsSpec, code) { "Define a new document command" }
        "RenewDocumentCommand".cmd(cmdName, argsSpec, code) { "Renew a document command" }
        "ProvideDocumentCommand".cmd(cmdName, argsSpec, code) { "Provide a document command" }
        "DeclareDocumentCommand".cmd(cmdName, argsSpec, code) { "Declare a document command" }

        "NewDocumentEnvironment".cmd(envName, argsSpec, startCode, endCode) { "Define a new document environment" }
        "RenewDocumentEnvironment".cmd(envName, argsSpec, startCode, endCode) { "Renew a document environment" }
        "ProvideDocumentEnvironment".cmd(envName, argsSpec, startCode, endCode) { "Provide a document environment" }
        "DeclareDocumentEnvironment".cmd(envName, argsSpec, startCode, endCode) { "Declare a document environment" }
    }
}