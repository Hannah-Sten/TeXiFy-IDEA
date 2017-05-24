package nl.rubensten.texifyidea.completion.handlers;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import nl.rubensten.texifyidea.lang.LatexMathCommand;
import nl.rubensten.texifyidea.lang.LatexNoMathCommand;
import nl.rubensten.texifyidea.psi.LatexCommands;

import java.util.List;

/**
 * @author Ruben Schellekens
 */
public class LatexCommandArgumentInsertHandler implements InsertHandler<LookupElement> {

    @Override
    public void handleInsert(InsertionContext insertionContext, LookupElement lookupElement) {
        Object object = lookupElement.getObject();

        if (object instanceof LatexCommands) {
            insertCommands((LatexCommands)object, insertionContext);
        }
        else if (object instanceof LatexMathCommand) {
            insertMathCommand((LatexMathCommand)object, insertionContext);
        }
        else if (object instanceof LatexNoMathCommand) {
            insertNoMathCommand((LatexNoMathCommand)object, insertionContext);
        }
    }

    private void insertCommands(LatexCommands commands, InsertionContext context) {
        List<String> optional = commands.getOptionalParameters();
        if (optional.isEmpty()) {
            return;
        }

        int cmdParameterCount = 0;
        if (!optional.isEmpty()) {
            try {
                cmdParameterCount = Integer.parseInt(optional.get(0));
            }
            catch (NumberFormatException ignore) {
            }
        }

        if (cmdParameterCount > 0) {
            insert(context, "{}");
        }
    }

    private void insertMathCommand(LatexMathCommand mathCommand, InsertionContext context) {
        if (mathCommand.autoInsertRequired()) {
            insert(context, mathCommand.getCommand());
        }
    }

    private void insertNoMathCommand(LatexNoMathCommand noMathCommand, InsertionContext context) {
        if (noMathCommand.autoInsertRequired()) {
            insert(context, noMathCommand.getCommand());
        }
    }

    private void insert(InsertionContext context, String text) {
        Editor editor = context.getEditor();
        CaretModel caret = editor.getCaretModel();
        editor.getDocument().insertString(caret.getOffset(), "{}");
        caret.moveToOffset(caret.getOffset() + 1);
    }
}
