package nl.rubensten.texifyidea;

import com.intellij.lexer.FlexAdapter;
import nl.rubensten.texifyidea.grammar._LatexLexer;

/**
 * @author Sten Wessel
 */
public class LatexLexerAdapter extends FlexAdapter {

    public LatexLexerAdapter() {
        super(new _LatexLexer(null));
    }
}
