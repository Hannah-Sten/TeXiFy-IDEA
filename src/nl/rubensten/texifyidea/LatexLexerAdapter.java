package nl.rubensten.texifyidea;

import com.intellij.lexer.FlexAdapter;
import nl.rubensten.texifyidea.grammar.LatexLexer;

/**
 * @author Sten Wessel
 */
public class LatexLexerAdapter extends FlexAdapter {

    public LatexLexerAdapter() {
        super(new LatexLexer(null));
    }
}
