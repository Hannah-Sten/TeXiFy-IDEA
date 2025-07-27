package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LClearContext
import nl.hannahsten.texifyidea.lang.LatexCommandSet
import nl.hannahsten.texifyidea.lang.LatexContexts

object NewLatexBasicCommands : LatexCommandSet() {

    private val classArgument = LArgument.required("class", LatexContexts.CLASS_NAME)
    private val packageArg = LArgument.required("package", LatexContexts.PACKAGE_NAMES)
    private val texFileArg = LArgument.required("tex file", LatexContexts.SINGLE_TEX_FILE)

    val ifCommands = buildCommands {
        underCmdContext(LatexContexts.PREAMBLE) {
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
        setCommandContext(LatexContexts.PREAMBLE)

        val command = required("cmd", LatexContexts.COMMAND)
        val envName = required("name", LatexContexts.IDENTIFIER)
        val numArgs = optional("num args", LatexContexts.NUMERIC)
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
            envName, "numberedlike".optional, required("caption", LatexContexts.TEXT), "within".optional
        ) { "Define a new theorem-like environment" }
        "newtheorem*".cmd(
            envName, required("caption", LatexContexts.TEXT)
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
        val name = LArgument.required("name", LatexContexts.IDENTIFIER)
        underCmdContext(LatexContexts.PREAMBLE) {
            // TODO
            "documentclass".cmd(
                optional("options", LatexContexts.LITERAL),
                classArgument
            ) {
                "Declare the document class"
            }

            "usepackage".cmd(
                optional("options", LatexContexts.LITERAL),
                packageArg,
            ) {
                "Load a LaTeX package"
            }
            "LoadClass".cmd(
                optional("options", LatexContexts.LITERAL),
                classArgument
            ) {
                "Load a class file"
            }
            "LoadClassWithOptions".cmd(classArgument)

            "ProvidesClass".cmd(name)
            "ProvidesPackage".cmd(name)
            "RequirePackage".cmd("options".optional, packageArg)

            "includeonly".cmd(required("tex files", LatexContexts.MULTIPLE_TEX_FILES)) {
                "Specify which files to include (comma-separated)"
            }

            "addtoluatexpath".cmd(required("paths", LatexContexts.FOLDER)) {
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
        val envArg = LArgument.required("environment", LatexContexts.IDENTIFIER)
        "begin".cmd(envArg)
        "end".cmd(envArg)
    }

    val xparseCommands = buildCommands {
        setCommandContext(LatexContexts.PREAMBLE)

        val cmdName = required("name", LatexContexts.COMMAND)
        val envName = required("name", LatexContexts.IDENTIFIER)
        val argsSpec = required("args spec", LatexContexts.LITERAL)
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