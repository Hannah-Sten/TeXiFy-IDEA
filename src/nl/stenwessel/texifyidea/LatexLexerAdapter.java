package nl.stenwessel.texifyidea;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.FlexLexer;

/**
 * @author Sten Wessel
 */
public class LatexLexerAdapter extends FlexAdapter {

    public LatexLexerAdapter() {
        super(new LatexLexer(null));
    }
}
