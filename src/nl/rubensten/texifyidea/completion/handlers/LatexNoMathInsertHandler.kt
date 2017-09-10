package nl.rubensten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateEditingListener
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateSettings
import com.intellij.codeInsight.template.impl.TemplateState
import nl.rubensten.texifyidea.lang.Environment
import nl.rubensten.texifyidea.lang.LatexNoMathCommand
import nl.rubensten.texifyidea.util.PackageUtils
import nl.rubensten.texifyidea.util.insertAndMove
import nl.rubensten.texifyidea.util.insertUsepackage

/**
 * @author Ruben Schellekens, Sten Wessel
 */
class LatexNoMathInsertHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val command = item.`object` as LatexNoMathCommand

        if (command == LatexNoMathCommand.BEGIN) {
            insertBegin(context)
        }
        else {
            LatexCommandArgumentInsertHandler().handleInsert(context, item)
        }

        LatexCommandPackageIncludeHandler().handleInsert(context, item)
    }

    /**
     * Inserts the `LATEX.begin` live template.
     */
    private fun insertBegin(context: InsertionContext) {
        val templateSettings = TemplateSettings.getInstance()
        val template = templateSettings.getTemplateById("LATEX.begin")

        val editor = context.editor
        val templateManager = TemplateManager.getInstance(context.project)
        templateManager.startTemplate(editor, template, EnvironmentInsertImports(context))
    }

    /**
     * Makes sure environments get imported if required.
     *
     * @author Ruben Schellekens
     */
    private inner class EnvironmentInsertImports(val context: InsertionContext) : TemplateEditingListener {

        override fun beforeTemplateFinished(templateState: TemplateState, template: Template) {
            val envName = templateState.getVariableValue("ENVNAME")?.text ?: return
            val environment = Environment[envName] ?: return
            val pack = environment.getDependency()
            val file = context.file
            val editor = context.editor

            // Include packages.
            if (!PackageUtils.getIncludedPackages(file).contains(pack.name)) {
                file.insertUsepackage(pack)
            }

            // Add initial contents.
            val initial = environment.initialContents
            editor.insertAndMove(editor.caretModel.offset, initial)
        }

        override fun templateFinished(template: Template?, b: Boolean) {}
        override fun templateCancelled(template: Template?) {}
        override fun currentVariableChanged(templateState: TemplateState, template: Template?, i: Int, i1: Int) {}
        override fun waitingForInput(template: Template?) {}
    }
}
