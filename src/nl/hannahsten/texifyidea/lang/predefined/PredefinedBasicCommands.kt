package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LArgument.Companion.required
import nl.hannahsten.texifyidea.lang.PredefinedCommandSet
import nl.hannahsten.texifyidea.lang.LatexContexts

object PredefinedBasicCommands : PredefinedCommandSet() {

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
            +"let"
            "edef".cmd(paramMacro, paramDef) { "LaTex primitive edef" }
            "gdef".cmd(paramMacro, paramDef) { "LaTex primitive gdef" }
            "xdef".cmd(paramMacro, paramDef) { "LaTex primitive xdef" }
            "futurelet".cmd(paramMacro, paramDef) { "LaTex primitive futurelet" }
            "relax".cmd { "Do nothing" }
        }
    }

    const val ARG_NAME_COMMAND_TOKEN = "commandToken"

    val definitionOfCommand = buildCommands {
        underContext(LatexContexts.Nothing) {
            // primitive commands, only
            +"def" // primitive command, but also considered a command definition
            +"let"
        }
        setRequiredContext(LatexContexts.Preamble)

        val command = required("commandToken", LatexContexts.CommandDeclaration)
        val numArgs = LArgument.optional("num args", LatexContexts.Numeric)
        val defaultOptional = "default".optional
        val code = required("code", +LatexContexts.InsideDefinition)

        "newcommand".cmd(command, numArgs, defaultOptional, code) { "Define a new command" }
        "newcommand*".cmd(command, numArgs, defaultOptional, code) { "Define a new command (starred variant)" }

        "newif".cmd(command) { "Define a new if conditional" }

        "providecommand".cmd(command, numArgs, defaultOptional, code) { "Provide a command if not defined" }
        "providecommand*".cmd(command, numArgs, defaultOptional, code) { "Provide a command if not defined (starred variant)" }

        "renewcommand".cmd(command, numArgs, defaultOptional, code) { "Redefine an existing command" }
        "renewcommand*".cmd(command, numArgs, defaultOptional, code) { "Redefine an existing command (starred variant)" }

        underPackage("xargs") {
            "newcommandx".cmd(command, numArgs, defaultOptional, code) { "Define a new command with extended args" }
            "renewcommandx".cmd(command, numArgs, defaultOptional, code) { "Redefine a command with extended args" }
            "providecommandx".cmd(command, numArgs, defaultOptional, code) { "Provide a command with extended args" }
            "DeclareRobustCommandx".cmd(command, numArgs, defaultOptional, code) { "Declare a robust command with extended args" }
        }

        // xparse commands
        underPackage("xparse") {
            val argsSpec = "args spec".required(LatexContexts.Literal)
            "NewDocumentCommand".cmd(command, argsSpec, code) { "Define a new document command" }
            "RenewDocumentCommand".cmd(command, argsSpec, code) { "Renew a document command" }
            "ProvideDocumentCommand".cmd(command, argsSpec, code) { "Provide a document command" }
            "DeclareDocumentCommand".cmd(command, argsSpec, code) { "Declare a document command" }
        }
    }

    val definitionOfEnvironment = buildCommands {
        setRequiredContext(LatexContexts.Preamble)

        val envName = required("name", LatexContexts.EnvironmentDeclaration)
        val numArgs = LArgument.optional("num args", LatexContexts.Numeric)
        val defaultOptional = "default".optional
        val beginCode = required("beginCode", +LatexContexts.InsideDefinition)
        val endCode = required("enddef", +LatexContexts.InsideDefinition)

        "newenvironment".cmd(envName, numArgs, defaultOptional, beginCode, endCode) { "Define a new environment" }
        "renewenvironment".cmd(envName, numArgs, defaultOptional, beginCode, endCode) { "Redefine an existing environment" }

        "newtheorem".cmd(
            envName, "numberedlike".optional, "caption".required(LatexContexts.Text), "within".optional
        ) { "Define a new theorem-like environment" }
        "newtheorem*".cmd(
            envName, "caption".required(LatexContexts.Text)
        ) { "Define a new theorem-like environment" }

        val argsSpec = "args spec".required(LatexContexts.Literal)
        "NewDocumentEnvironment".cmd(envName, argsSpec, beginCode, endCode) { "Define a new document environment" }
        "RenewDocumentEnvironment".cmd(envName, argsSpec, beginCode, endCode) { "Renew a document environment" }
        "ProvideDocumentEnvironment".cmd(envName, argsSpec, beginCode, endCode) { "Provide a document environment" }
        "DeclareDocumentEnvironment".cmd(envName, argsSpec, beginCode, endCode) { "Declare a document environment" }

        packageOf("xargs")
        "newenvironmentx".cmd(envName, numArgs, defaultOptional, beginCode, endCode) { "Define a new environment with extended args" }
        "renewenvironmentx".cmd(envName, numArgs, defaultOptional, beginCode, endCode) { "Redefine an environment with extended args" }
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
}