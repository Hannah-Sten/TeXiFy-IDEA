package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LArgument.Companion.required
import nl.hannahsten.texifyidea.lang.PredefinedCommandSet
import nl.hannahsten.texifyidea.lang.LatexContexts

/**
 * The set of basic commands that will be used for
 * * command/environment definition resolving;
 *
 */
object PredefinedDefinitionCommands : PredefinedCommandSet() {



    val regularDefinitionOfCommand = preambleCommands {

        val command = required("cmd", LatexContexts.CommandDeclaration)
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
    }

    val argSpecDefinitionOfCommand = preambleCommands {
        val command = required("cmd", LatexContexts.CommandDeclaration)
        val code = required("code", +LatexContexts.InsideDefinition)
        // xparse commands
        underPackage("xparse") {
            val argsSpec = "args spec".required(LatexContexts.Literal)
            "NewDocumentCommand".cmd(command, argsSpec, code) { "Define a new document command" }
            "RenewDocumentCommand".cmd(command, argsSpec, code) { "Renew a document command" }
            "ProvideDocumentCommand".cmd(command, argsSpec, code) { "Provide a document command" }
            "DeclareDocumentCommand".cmd(command, argsSpec, code) { "Declare a document command" }
        }
    }

    val definitionOfMathCommand = preambleCommands {
        val command = required("cmd", LatexContexts.CommandDeclaration)
        underPackage("amsmath") {
            "DeclareMathOperator".cmd(
                command, "operator".required(LatexContexts.Text)
            ) { "Declare a new math operator" }
        }
        underPackage("mathtools") {
            val leftDelimiter = required("left delimiter", LatexContexts.Text)
            val rightDelimiter = required("right delimiter", LatexContexts.Text)
            "DeclarePairedDelimiter".cmd(
                command, leftDelimiter, rightDelimiter
            ) { "Declare a paired delimiter" }
            "DeclarePairedDelimiterX".cmd(
                command, leftDelimiter, rightDelimiter
            ) { "Declare a paired delimiter" }
            "DeclarePairedDelimiterXPP".cmd(
                command, "pre code".required(LatexContexts.InsideDefinition), leftDelimiter, rightDelimiter,
                "post code".required(LatexContexts.InsideDefinition), "body".required(LatexContexts.InsideDefinition)
            ) { "Declare a paired delimiter with pre and post code" }
        }
    }

    val regularDefinitionOfEnvironment = preambleCommands {
        val envName = required("name", LatexContexts.EnvironmentDeclaration)
        val numArgs = LArgument.optional("num args", LatexContexts.Numeric)
        val defaultValue = "default".optional
        val beginCode = required("beginCode", +LatexContexts.InsideDefinition)
        val endCode = required("enddef", +LatexContexts.InsideDefinition)

        "newenvironment".cmd(envName, numArgs, defaultValue, beginCode, endCode) { "Define a new environment" }
        "renewenvironment".cmd(envName, numArgs, defaultValue, beginCode, endCode) { "Redefine an existing environment" }


    }

    val newTheoremDefinitionOfEnvironment = preambleCommands {
        val envName = required("name", LatexContexts.EnvironmentDeclaration)
        "newtheorem".cmd(
            envName, "numberedlike".optional, "caption".required(LatexContexts.Text), "within".optional
        ) { "Define a new theorem-like environment" }
        "newtheorem*".cmd(
            envName, "caption".required(LatexContexts.Text)
        ) { "Define a new theorem-like environment" }
    }

    val argSpecDefinitionOfEnvironment = preambleCommands {
        val envName = required("name", LatexContexts.EnvironmentDeclaration)
        val argsSpec = "args spec".required(LatexContexts.Literal)
        val beginCode = required("beginCode", +LatexContexts.InsideDefinition)
        val endCode = required("enddef", +LatexContexts.InsideDefinition)
        "NewDocumentEnvironment".cmd(envName, argsSpec, beginCode, endCode) { "Define a new document environment" }
        "RenewDocumentEnvironment".cmd(envName, argsSpec, beginCode, endCode) { "Renew a document environment" }
        "ProvideDocumentEnvironment".cmd(envName, argsSpec, beginCode, endCode) { "Provide a document environment" }
        "DeclareDocumentEnvironment".cmd(envName, argsSpec, beginCode, endCode) { "Declare a document environment" }
    }

    val xargsDefinitionOfEnvironment = buildCommands {
        setRequiredContext(LatexContexts.Preamble)
        val envName = required("name", LatexContexts.EnvironmentDeclaration)
        val numArgs = LArgument.optional("num args", LatexContexts.Numeric)
        val argsSpec = "args spec".required(LatexContexts.Literal)
        val beginCode = required("beginCode", +LatexContexts.InsideDefinition)
        val endCode = required("enddef", +LatexContexts.InsideDefinition)
        packageOf("xargs")
        "newenvironmentx".cmd(envName, numArgs, argsSpec, beginCode, endCode) { "Define a new environment with extended args" }
        "renewenvironmentx".cmd(envName, numArgs, argsSpec, beginCode, endCode) { "Redefine an environment with extended args" }
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


    val namesOfAllCommandDef = buildSet {
        add("def")
        add("let")
        regularDefinitionOfCommand.forEach { add(it.name) }
        argSpecDefinitionOfCommand.forEach { add(it.name) }
        definitionOfMathCommand.forEach { add(it.name) }
    }

    val namesOfAllEnvironmentDef = buildSet {
        regularDefinitionOfEnvironment.forEach { add(it.name) }
        argSpecDefinitionOfEnvironment.forEach { add(it.name) }
        xargsDefinitionOfEnvironment.forEach { add(it.name) }
        newTheoremDefinitionOfEnvironment.forEach { add(it.name) }
    }
}