package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LArgument.Companion.required
import nl.hannahsten.texifyidea.lang.LClearContext
import nl.hannahsten.texifyidea.lang.LatexCommandBuilderScope.optional
import nl.hannahsten.texifyidea.lang.PredefinedCommandSet
import nl.hannahsten.texifyidea.lang.LatexContexts

object NewLatexBasicCommands : PredefinedCommandSet() {

    private val classArgument = required("class", LatexContexts.ClassName)
    private val packageArg = required("package", LatexContexts.PackageNames)
    private val texFileArg = required("tex file", LatexContexts.SingleTexFile)

    val ifCommands = buildCommands {
        underCmdContext(LatexContexts.Preamble) {
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

    val definitionCommands = buildCommands {
        setCommandContext(LatexContexts.Preamble)

        val command = required("cmd",LatexContexts.PlainCommand)
        val envName = required("name",LatexContexts.Identifier)
        val numArgs = LArgument.optional("num args", LatexContexts.Numeric)
        val defaultOptional = "default".optional
        // The following context can be deeper
        val definition = required("def", LClearContext)
        val begdefRequired = required("begdef", LClearContext)
        val enddefRequired = required("enddef", LClearContext)

        "catcode".cmd("char".required) { "Set category code for a character" }

        "newcommand".cmd(command, numArgs, defaultOptional, definition) { "Define a new command" }
        "newcommand*".cmd(command, numArgs, defaultOptional, definition) { "Define a new command (starred variant)" }

        "newif".cmd(command) { "Define a new if conditional" }

        "providecommand".cmd(command, numArgs, defaultOptional, definition) { "Provide a command if not defined" }
        "providecommand*".cmd(command, numArgs, defaultOptional, definition) { "Provide a command if not defined (starred variant)" }

        "renewcommand".cmd(command, numArgs, defaultOptional, definition) { "Redefine an existing command" }
        "renewcommand*".cmd(command, numArgs, defaultOptional, definition) { "Redefine an existing command (starred variant)" }

        "newenvironment".cmd(envName, numArgs, defaultOptional, begdefRequired, enddefRequired) { "Define a new environment" }
        "renewenvironment".cmd(envName, numArgs, defaultOptional, begdefRequired, enddefRequired) { "Redefine an existing environment" }
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

        "newenvironmentx".cmd(command, numArgs, defaultOptional, begdefRequired, enddefRequired) { "Define a new environment with extended args" }
        "renewenvironmentx".cmd(command, numArgs, defaultOptional, begdefRequired, enddefRequired) { "Redefine an environment with extended args" }
    }

    val basicFileInputCommands = buildCommands {
        // Most file inputs are in preamble, but can be adjusted per command if needed.
        val name = required("name", LatexContexts.Identifier)
        underCmdContext(LatexContexts.Preamble) {
            // TODO
            "documentclass".cmd(
                "options".optional(LatexContexts.Literal),
                classArgument
            ) {
                "Declare the document class"
            }

            "usepackage".cmd(
                "options".optional(LatexContexts.Literal),
                packageArg,
            ) {
                "Load a LaTeX package"
            }
            "LoadClass".cmd(
                "options".optional(LatexContexts.Literal),
                classArgument
            ) {
                "Load a class file"
            }
            "LoadClassWithOptions".cmd(classArgument)

            "ProvidesClass".cmd(name)
            "ProvidesPackage".cmd(name)
            "RequirePackage".cmd("options".optional, packageArg)

            "includeonly".cmd("tex files".required(LatexContexts.MultipleTexFiles)) {
                "Specify which files to include (comma-separated)"
            }

            "addtoluatexpath".cmd("paths".required(LatexContexts.Folder)) {
                "Add a relative path to the LaTeX search path"
            }
        }


        // Include and input commands.
        "include".cmd(texFileArg) {
            "Include a TeX file (page break before)"
        }

        "input".cmd(texFileArg) {
            "Input a TeX file (no page break)"
        }
    }

    val primitives = buildCommands {
        val envArg = required("environment", LatexContexts.Identifier)
        "begin".cmd(envArg)
        "end".cmd(envArg)
    }

    val xparseCommands = buildCommands {
        setCommandContext(LatexContexts.Preamble)

        val cmdName = "name".required(LatexContexts.PlainCommand)
        val envName = "name".required(LatexContexts.Identifier)
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