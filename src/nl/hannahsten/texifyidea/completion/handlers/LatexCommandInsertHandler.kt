package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateEditingListener
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TemplateState
import com.intellij.codeInsight.template.impl.TextExpression
import nl.hannahsten.texifyidea.completion.SimpleWithDefLookupElement
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LSemanticEnv
import nl.hannahsten.texifyidea.lang.predefined.AllPredefined
import nl.hannahsten.texifyidea.util.insertAndMove
import nl.hannahsten.texifyidea.util.magic.TypographyMagic

/**
 * @author Hannah Schellekens, Sten Wessel
 */
data class LatexCommandInsertHandler(
    val semantics: LSemanticCommand,
    /**
     * The arguments for insertion. Some optional arguments may be skipped, so this may be a subset of the full arguments.
     */
    val arguments: List<LArgument>
) : InsertHandler<SimpleWithDefLookupElement> {

    /**
     * Remove whitespaces and everything after that that was inserted by the lookup text.
     */
    private fun keepOnlyCommandToken(context: InsertionContext) {
        val editor = context.editor
        val offset = editor.caretModel.offset
        // context.startOffset is the offset of the start of the just inserted text.
        val commandEndOffset = context.startOffset + semantics.commandWithSlash.length
        if (commandEndOffset >= offset) return
        // Remove the command token and everything after it.
        editor.document.deleteString(commandEndOffset, offset)
    }

    override fun handleInsert(context: InsertionContext, item: SimpleWithDefLookupElement) {
        keepOnlyCommandToken(context)
        when (semantics.name) {
            "begin" -> {
                insertBegin(context)
            }

            in TypographyMagic.pseudoCodeBeginEndOpposites -> {
                insertPseudocodeEnd(semantics, context)
            }

            else -> {
                LatexCommandArgumentInsertHandler(arguments).handleInsert(context, item)
            }
        }

        RightInsertHandler.handleInsert(context, item, semantics)
        LatexAddImportInsertHandler.handleInsert(context, item)
    }

    private fun insertPseudocodeEnd(cmd: LSemanticCommand, context: InsertionContext) {
        val numberRequiredArguments = arguments.count { it.isRequired }

        val templateText = List(numberRequiredArguments) {
            $$"{$__Variable$$it$}"
        }.joinToString("") + $$"\n$END$\n\\$${TypographyMagic.pseudoCodeBeginEndOpposites[cmd.name]}"
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
        val templateText = $$"{$__Variable0$}$END$\n\\end{$__Variable0$}"
        val template = object : TemplateImpl("", templateText, "") {
            override fun isToReformat(): Boolean = true
        }
        template.addVariable(TextExpression(""), true)

        TemplateManager.getInstance(context.project)
            .startTemplate(context.editor, template, EnvironmentInsertRequiredArg(context))
    }

    companion object {

        val environmentInitialContentsMap = mapOf(
            "description" to "\\item ",
            "enumerate" to "\\item ",
            "itemize" to "\\item ",
        )

        /**
         * Insert a live template for the required arguments. When there are  no required
         * arguments, move to the content of the environment.
         */
        private fun insertRequiredArguments(environment: LSemanticEnv?, context: InsertionContext) {
            val numberRequiredArguments = environment?.arguments?.count { it.isRequired } ?: 0

            val templateText = List(numberRequiredArguments) { $$"{$__Variable$$it$}" }.joinToString("") + $$"\n$END$"
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
    private class EnvironmentInsertRequiredArg(val context: InsertionContext) : TemplateEditingListener {

        override fun beforeTemplateFinished(templateState: TemplateState, template: Template?) {
            val envName = templateState.getVariableValue("__Variable0")?.text ?: return
            val defBundle = LatexDefinitionService.getInstance(context.project).getDefBundlesMerged(context.file)
            val envSemantic = defBundle.lookupEnv(envName) ?: AllPredefined.lookupEnv(envName) // try to resolve it anyway
            insertRequiredArguments(envSemantic, context)
            envSemantic ?: return
            val editor = context.editor
            // Adding import for the environment is done in environment name autocompletion, not here
            // Add initial contents.
            val initial = environmentInitialContentsMap[envName] ?: ""
            editor.insertAndMove(context.editor.caretModel.offset, initial)
        }

        override fun templateFinished(template: Template, b: Boolean) {}
        override fun templateCancelled(template: Template?) {}
        override fun currentVariableChanged(templateState: TemplateState, template: Template?, i: Int, i1: Int) {}
        override fun waitingForInput(template: Template?) {}
    }
}