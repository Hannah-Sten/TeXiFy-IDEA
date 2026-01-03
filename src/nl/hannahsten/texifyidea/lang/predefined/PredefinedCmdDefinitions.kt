package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LArgument.Companion.required
import nl.hannahsten.texifyidea.lang.LatexContextIntro
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.PredefinedCommandSet

/**
 * The set of basic commands that will be used for
 * * command/environment definition resolving;
 *
 */
object PredefinedCmdDefinitions : PredefinedCommandSet() {

    private val argCommandName = required("name", LatexContexts.CommandDeclaration)
    private val argCode = required("code", LatexContextIntro.add(LatexContexts.InsideDefinition))

    val regularDefinitionOfCommand = preambleCommands {

        val command = argCommandName
        val nargs = LArgument.optional("nargs", LatexContexts.Numeric)
        val defaultOptional = "default".optional
        val code = argCode

        "newcommand".cmd(command, nargs, defaultOptional, code) { "Define a new command" }
        "newcommand*".cmd(command, nargs, defaultOptional, code) { "Define a new command (starred variant)" }

        "newif".cmd(command) { "Define a new if conditional" }

        "providecommand".cmd(command, nargs, defaultOptional, code) { "Provide a command if not defined" }
        "providecommand*".cmd(command, nargs, defaultOptional, code) { "Provide a command if not defined (starred variant)" }

        "renewcommand".cmd(command, nargs, defaultOptional, code) { "Redefine an existing command" }
        "renewcommand*".cmd(command, nargs, defaultOptional, code) { "Redefine an existing command (starred variant)" }
        "DeclareRobustCommand".cmd(command, nargs, defaultOptional, code) { "Declare a robust command" }

        underPackage("xargs") {
            "newcommandx".cmd(command, nargs, defaultOptional, code) { "Define a new command with extended args" }
            "renewcommandx".cmd(command, nargs, defaultOptional, code) { "Redefine a command with extended args" }
            "providecommandx".cmd(command, nargs, defaultOptional, code) { "Provide a command with extended args" }
            "DeclareRobustCommandx".cmd(command, nargs, defaultOptional, code) { "Declare a robust command with extended args" }
        }
    }

    val definitionOfTextCommand = preambleCommands {
        val command = argCommandName
        val nargs = LArgument.optional("nargs", LatexContexts.Numeric)
        val defaultOptional = "default".optional
        val code = argCode
        val encoding = "encoding".required(LatexContexts.Literal)
        val slot = "slot".required(LatexContexts.Numeric)
        "DeclareTextCommand".cmd(command, encoding, nargs, defaultOptional, code) { "Declare a text command with encoding" }

        underPackage("fontenc") {
            "DeclareTextSymbol".cmd(command, encoding, slot) { "Declare a text symbol with encoding" }
            "DeclareTextAccent".cmd(command, encoding, slot) { "Declare a text accent with encoding" }
            "DeclareTextComposite".cmd(command, encoding, "char".required, slot) { "Declare a text composite character with encoding" }
        }
    }

    val argSpecDefinitionOfCommand = preambleCommands {
        val command = argCommandName
        val code = argCode
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
        val command = argCode
        underPackage("amsopn") {
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

    private val argEnvName = required("name", LatexContexts.EnvironmentDeclaration)
    private val argBeginCode = required("begin", LatexContextIntro.add(LatexContexts.InsideDefinition))
    private val argEndCode = required("end", LatexContextIntro.add(LatexContexts.InsideDefinition))

    val regularDefinitionOfEnvironment = preambleCommands {
        val numArgs = LArgument.optional("num args", LatexContexts.Numeric)
        val defaultValue = "default".optional
        "newenvironment".cmd(argEnvName, numArgs, defaultValue, argBeginCode, argEndCode) { "Define a new environment" }
        "renewenvironment".cmd(argEnvName, numArgs, defaultValue, argBeginCode, argEndCode) { "Redefine an existing environment" }
    }

    val newTheoremDefinitionOfEnvironment = preambleCommands {
        "newtheorem".cmd(
            argEnvName, "numberedlike".optional, "caption".required(LatexContexts.Text), "within".optional
        ) { "Define a new theorem-like environment" }
        "newtheorem*".cmd(
            argEnvName, "caption".required(LatexContexts.Text)
        ) { "Define a new theorem-like environment" }

        underPackage("mdframed") {
            "newmdenv".cmd("param".optional, argEnvName) {
                "Define a new mdframed environment"
            }
            "newmdtheoremenv".cmd(
                argEnvName, "numberedlike".optional, "caption".required(LatexContexts.Text)
            )
        }
    }

    val argSpecDefinitionOfEnvironment = preambleCommands {
        val argsSpec = "args spec".required(LatexContexts.Literal)
        "NewDocumentEnvironment".cmd(argEnvName, argsSpec, argBeginCode, argEndCode) { "Define a new document environment" }
        "RenewDocumentEnvironment".cmd(argEnvName, argsSpec, argBeginCode, argEndCode) { "Renew a document environment" }
        "ProvideDocumentEnvironment".cmd(argEnvName, argsSpec, argBeginCode, argEndCode) { "Provide a document environment" }
        "DeclareDocumentEnvironment".cmd(argEnvName, argsSpec, argBeginCode, argEndCode) { "Declare a document environment" }
    }

    val xargsDefinitionOfEnvironment = buildCommands {
        applicableIn(LatexContexts.Preamble)
        val numArgs = LArgument.optional("num args", LatexContexts.Numeric)
        val argsSpec = "args spec".required(LatexContexts.Literal)
        packageOf("xargs")
        "newenvironmentx".cmd(argEnvName, numArgs, argsSpec, argBeginCode, argEndCode) { "Define a new environment with extended args" }
        "renewenvironmentx".cmd(argEnvName, numArgs, argsSpec, argBeginCode, argEndCode) { "Redefine an environment with extended args" }
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
        definitionOfTextCommand.forEach { add(it.name) }
        definitionOfMathCommand.forEach { add(it.name) }
    }

    val namesOfAllEnvironmentDef = buildSet {
        regularDefinitionOfEnvironment.forEach { add(it.name) }
        argSpecDefinitionOfEnvironment.forEach { add(it.name) }
        xargsDefinitionOfEnvironment.forEach { add(it.name) }
        newTheoremDefinitionOfEnvironment.forEach { add(it.name) }
        PredefinedCmdGeneric.listingsDefinitionCommands.forEach { add(it.name) }
    }

    val namesOfAllDef: Set<String> = namesOfAllCommandDef + namesOfAllEnvironmentDef
}