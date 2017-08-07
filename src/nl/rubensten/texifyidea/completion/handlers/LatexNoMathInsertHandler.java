package nl.rubensten.texifyidea.completion.handlers;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.openapi.editor.Editor;
import nl.rubensten.texifyidea.lang.LatexNoMathCommand;

/**
 * @author Sten Wessel
 */
public class LatexNoMathInsertHandler implements InsertHandler<LookupElement> {

    @Override
    public void handleInsert(InsertionContext context, LookupElement item) {
        LatexNoMathCommand command = (LatexNoMathCommand)item.getObject();

        switch (command) {
            case BEGIN:
                insertBegin(context);
                break;
            default:
                new LatexCommandArgumentInsertHandler().handleInsert(context, item);
        }
    }

    /**
     * Inserts the {@code LATEX.begin} live template.
     */
    private void insertBegin(InsertionContext context) {
        TemplateSettings templateSettings = TemplateSettings.getInstance();
        Template template = templateSettings.getTemplateById("LATEX.begin");

        Editor editor = context.getEditor();
        TemplateManager templateManager = TemplateManager.getInstance(context.getProject());
        templateManager.startTemplate(editor, template);
    }
}
