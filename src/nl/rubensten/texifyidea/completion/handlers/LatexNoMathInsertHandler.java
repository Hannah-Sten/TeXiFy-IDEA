package nl.rubensten.texifyidea.completion.handlers;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import nl.rubensten.texifyidea.lang.LatexNoMathCommand;

/**
 * @author Sten Wessel
 */
public class LatexNoMathInsertHandler implements InsertHandler<LookupElement> {

    @Override
    public void handleInsert(InsertionContext context, LookupElement item) {
        LatexNoMathCommand command = (LatexNoMathCommand)item.getObject();
        if (command == LatexNoMathCommand.BEGIN) {
            Template template = TemplateSettings.getInstance().getTemplateById("LATEX.begin");
            TemplateManager.getInstance(context.getProject()).startTemplate(context.getEditor(), template);
        }
    }
}
