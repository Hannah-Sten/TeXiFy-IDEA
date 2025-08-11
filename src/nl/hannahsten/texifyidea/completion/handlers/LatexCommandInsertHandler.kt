package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateEditingListener
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TemplateState
import com.intellij.codeInsight.template.impl.TextExpression
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.lang.Environment
import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LSemanticEnv
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.Argument
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.lang.commands.RequiredArgument
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.definitionsAndRedefinitionsInFileSet
import nl.hannahsten.texifyidea.util.magic.TypographyMagic
import nl.hannahsten.texifyidea.util.parser.isEnvironmentDefinition

/**
 * @author Hannah Schellekens, Sten Wessel
 */
class LatexCommandInsertHandler(val arguments: List<Argument>? = null) : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        removeWhiteSpaces(context)
        val command = item.`object` as LatexCommand
        when (command.command) {
            LatexGenericRegularCommand.BEGIN.command -> {
                insertBegin(context)
            }

            in TypographyMagic.pseudoCodeBeginEndOpposites -> {
                insertPseudocodeEnd(command.command, context)
            }

            else -> {
                LatexCommandArgumentInsertHandler(arguments).handleInsert(context, item)
            }
        }

        RightInsertHandler().handleInsert(context, item)
        LatexCommandPackageIncludeHandler().handleInsert(context, item)
    }

    private fun insertPseudocodeEnd(name: String, context: InsertionContext) {
        val numberRequiredArguments = LatexCommand.lookup(name)
            ?.first()?.arguments
            ?.count { it is RequiredArgument } ?: 0

        val templateText = List(numberRequiredArguments) {
            "{\$__Variable${it}\$}"
        }.joinToString("") + "\n\$END\$\n\\${TypographyMagic.pseudoCodeBeginEndOpposites[name]}"
        val parameterTemplate = object : TemplateImpl("", templateText, "") {
            override fun isToReformat(): Boolean = false
        }
        repeat(numberRequiredArguments) { parameterTemplate.addVariable(TextExpression(""), true) }

        TemplateManager.getInstance(context.project)
            .startTemplate(context.editor, parameterTemplate)
    }

    /**
     * Inserts a live template to make the end command match the begin command.
     */
    private fun insertBegin(context: InsertionContext) {
        val templateText = "{\$__Variable0\$}\$END\$\n\\end{\$__Variable0\$}"
        val template = object : TemplateImpl("", templateText, "") {
            override fun isToReformat(): Boolean = true
        }
        template.addVariable(TextExpression(""), true)

        TemplateManager.getInstance(context.project)
            .startTemplate(context.editor, template, EnvironmentInsertImports(context))
    }

    /**
     * Insert a live template for the required arguments. When there are  no required
     * arguments, move to the content of the environment.
     */
    private fun insertRequiredArguments(environment: Environment?, context: InsertionContext) {
        val numberRequiredArguments = environment?.arguments
            ?.count { it is RequiredArgument } ?: 0

        val templateText = List(numberRequiredArguments) { "{\$__Variable${it}\$}" }.joinToString("") + "\n\$END\$"
        val parameterTemplate = object : TemplateImpl("", templateText, "") {
            override fun isToReformat(): Boolean = true
        }
        repeat(numberRequiredArguments) { parameterTemplate.addVariable(TextExpression(""), true) }

        TemplateManager.getInstance(context.project)
            .startTemplate(context.editor, parameterTemplate)
    }

    companion object {
        /**
         * Remove whitespaces and everything after that that was inserted by the lookup text.
         */
        internal fun removeWhiteSpaces(context: InsertionContext) {
            val editor = context.editor
            val document = editor.document
            val offset = editor.caretModel.offset
            // context.startOffset is the offset of the start of the just inserted text.
            val insertedText = document.text.substring(context.startOffset, offset)
            val indexFirstSpace = insertedText.indexOfFirst { it == ' ' }
            if (indexFirstSpace == -1) return
            document.deleteString(context.startOffset + indexFirstSpace, offset)
        }
    }

    /**
     * Makes sure environments get imported if required.
     *
     * @author Hannah Schellekens
     */
    private inner class EnvironmentInsertImports(val context: InsertionContext) : TemplateEditingListener {

        override fun beforeTemplateFinished(templateState: TemplateState, template: Template?) {
            val envName = templateState.getVariableValue("__Variable0")?.text ?: return

            val environment = Environment[envName]
            insertRequiredArguments(environment, context)
            environment ?: return

            val pack = environment.dependency
            val file = context.file
            val editor = context.editor
            val envDefinitions = file.definitionsAndRedefinitionsInFileSet().asSequence()
                .filter { it.isEnvironmentDefinition() }
                .mapNotNull { it.requiredParameterText(0) }
                .toSet()

            // Include packages.
            if (!file.includedPackagesInFileset().contains(pack) && envName !in envDefinitions) {
                file.insertUsepackage(pack)
            }

            // Add initial contents.
            val initial = environment.initialContents
            editor.insertAndMove(editor.caretModel.offset, initial)
        }

        override fun templateFinished(template: Template, b: Boolean) {}
        override fun templateCancelled(template: Template?) {}
        override fun currentVariableChanged(templateState: TemplateState, template: Template?, i: Int, i1: Int) {}
        override fun waitingForInput(template: Template?) {}
    }
}

/**
 * @author Hannah Schellekens, Sten Wessel
 */
class NewLatexCommandInsertHandler(
    val semantics: LSemanticCommand,
    /**
     * The arguments for insertion. Some optional arguments may be skipped, so this may be a subset of the full arguments.
     */
    val arguments: List<LArgument>,
    val definitionBundle: DefinitionBundle
) : InsertHandler<LookupElement> {
    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        removeWhiteSpaces(context)
        when (semantics.name) {
            "begin" -> {
                insertBegin(context)
            }

            in TypographyMagic.pseudoCodeBeginEndOpposites -> {
                insertPseudocodeEnd(semantics, context)
            }

            else -> {
                NewLatexCommandArgumentInsertHandler(arguments).handleInsert(context, item)
            }
        }

        SemanticRightInsertHandler.handleInsert(context, item, semantics)
        // TODO: now automatic package inclusion is disabled as we only show command that are already included in packages.
    }

    private fun insertPseudocodeEnd(cmd: LSemanticCommand, context: InsertionContext) {
        val numberRequiredArguments = arguments.count { it.isRequired }

        val templateText = List(numberRequiredArguments) {
            "{\$__Variable${it}\$}"
        }.joinToString("") + "\n\$END\$\n\\${TypographyMagic.pseudoCodeBeginEndOpposites[cmd.name]}"
        val parameterTemplate = object : TemplateImpl("", templateText, "") {
            override fun isToReformat(): Boolean = false
        }
        repeat(numberRequiredArguments) { parameterTemplate.addVariable(TextExpression(""), true) }

        TemplateManager.getInstance(context.project)
            .startTemplate(context.editor, parameterTemplate)
    }

    /**
     * Inserts a live template to make the end command match the begin command.
     */
    private fun insertBegin(context: InsertionContext) {
        val templateText = "{\$__Variable0\$}\$END\$\n\\end{\$__Variable0\$}"
        val template = object : TemplateImpl("", templateText, "") {
            override fun isToReformat(): Boolean = true
        }
        template.addVariable(TextExpression(""), true)

        TemplateManager.getInstance(context.project)
            .startTemplate(context.editor, template, EnvironmentInsertImports(context, definitionBundle))
    }

    companion object {

        val environmentInitialContentsMap = mapOf(
            "description" to "\\item ",
            "enumerate" to "\\item ",
            "itemize" to "\\item ",
        )

        /**
         * Remove whitespaces and everything after that that was inserted by the lookup text.
         */
        internal fun removeWhiteSpaces(context: InsertionContext) {
            val editor = context.editor
            val document = editor.document
            val offset = editor.caretModel.offset
            // context.startOffset is the offset of the start of the just inserted text.
            val insertedText = document.text.substring(context.startOffset, offset)
            val indexFirstSpace = insertedText.indexOfFirst { it == ' ' }
            if (indexFirstSpace == -1) return
            document.deleteString(context.startOffset + indexFirstSpace, offset)
        }

        /**
         * Insert a live template for the required arguments. When there are  no required
         * arguments, move to the content of the environment.
         */
        private fun insertRequiredArguments(environment: LSemanticEnv?, context: InsertionContext) {
            val numberRequiredArguments = environment?.arguments?.count { it.isRequired } ?: 0

            val templateText = List(numberRequiredArguments) { "{\$__Variable${it}\$}" }.joinToString("") + "\n\$END\$"
            val parameterTemplate = object : TemplateImpl("", templateText, "") {
                override fun isToReformat(): Boolean = true
            }
            repeat(numberRequiredArguments) { parameterTemplate.addVariable(TextExpression(""), true) }

            TemplateManager.getInstance(context.project)
                .startTemplate(context.editor, parameterTemplate)
        }
    }

    /**
     * Makes sure environments get imported if required.
     *
     * @author Hannah Schellekens
     */
    private class EnvironmentInsertImports(
        val context: InsertionContext,
        val definitionBundle: DefinitionBundle
    ) : TemplateEditingListener {

        override fun beforeTemplateFinished(templateState: TemplateState, template: Template?) {
            val envName = templateState.getVariableValue("__Variable0")?.text ?: return
            val envSemantic = definitionBundle.lookupEnv(envName)
            insertRequiredArguments(envSemantic, context)
            envSemantic ?: return

            val pack = LatexPackage(envSemantic.dependency)
            val file = context.file
            val editor = context.editor
            val envDefinitions = file.definitionsAndRedefinitionsInFileSet().asSequence()
                .filter { it.isEnvironmentDefinition() }
                .mapNotNull { it.requiredParameterText(0) }
                .toSet()

            // Include packages.
            if (!file.includedPackagesInFileset().contains(pack) && envName !in envDefinitions) {
                file.insertUsepackage(pack)
            }

            // Add initial contents.
            val initial = environmentInitialContentsMap[envName] ?: ""
            editor.insertAndMove(editor.caretModel.offset, initial)
        }

        override fun templateFinished(template: Template, b: Boolean) {}
        override fun templateCancelled(template: Template?) {}
        override fun currentVariableChanged(templateState: TemplateState, template: Template?, i: Int, i1: Int) {}
        override fun waitingForInput(template: Template?) {}
    }
}
