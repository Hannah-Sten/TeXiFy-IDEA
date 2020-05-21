package nl.hannahsten.texifyidea.completion.handlers;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.CaretModel;

/**
 * @author Sten Wessel
 */
public class LatexReferenceInsertHandler implements InsertHandler<LookupElement> {

    @Override
    public void handleInsert(InsertionContext context, LookupElement item) {
        CaretModel model = context.getEditor().getCaretModel();
        model.moveToOffset(model.getOffset() + 1);
    }
}
