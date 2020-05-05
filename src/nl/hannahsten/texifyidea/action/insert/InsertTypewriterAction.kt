package nl.hannahsten.texifyidea.action.insert;

import nl.hannahsten.texifyidea.TexifyIcons;
import nl.hannahsten.texifyidea.action.InsertEditorAction;

/**
 * @author Hannah Schellekens
 */
public class InsertTypewriterAction extends InsertEditorAction {

    public InsertTypewriterAction() {
        super("Typewriter (monospace)", TexifyIcons.FONT_TYPEWRITER, "\\texttt{", "}");
    }
}
