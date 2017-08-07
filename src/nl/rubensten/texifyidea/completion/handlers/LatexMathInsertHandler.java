package nl.rubensten.texifyidea.completion.handlers;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;

/**
 * @author Ruben Schellekens, Sten Wessel
 */
public class LatexMathInsertHandler implements InsertHandler<LookupElement> {

    @Override
    public void handleInsert(InsertionContext context, LookupElement item) {
        new LatexCommandArgumentInsertHandler().handleInsert(context, item);
        new LatexCommandPackageIncludeHandler().handleInsert(context, item);
    }
}
