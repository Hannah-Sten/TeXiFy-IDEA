package nl.stenwessel.texifyidea;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.FlexLexer;
import nl.stenwessel.texifyidea.grammar._LatexLexer;

/**
 * @author Sten Wessel
 */
public class LatexLexerAdapter extends FlexAdapter {

    public LatexLexerAdapter() {
        super(new _LatexLexer(null));
    }
}
