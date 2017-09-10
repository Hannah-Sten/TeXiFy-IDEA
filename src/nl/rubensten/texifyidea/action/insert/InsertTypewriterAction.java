package nl.rubensten.texifyidea.action.insert;

import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.action.InsertEditorAction;

/**
 * @author Ruben Schellekens
 */
public class InsertTypewriterAction extends InsertEditorAction {

    public InsertTypewriterAction() {
        super("Typewriter (monospace)", TexifyIcons.FONT_TYPEWRITER, "\\texttt{", "}");
    }
}
