package nl.rubensten.texifyidea;

import com.intellij.lang.Language;

/**
 * @author Sten Wessel
 */
public class LatexLanguage extends Language {

    public static final LatexLanguage INSTANCE = new LatexLanguage();

    private LatexLanguage() {
        super("Latex");
    }

}
