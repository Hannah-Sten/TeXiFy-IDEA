package nl.hannahsten.texifyidea.action.insert;

import nl.hannahsten.texifyidea.TexifyIcons;
import nl.hannahsten.texifyidea.action.InsertEditorAction;

/**
 * @author Hannah Schellekens
 */
public class InsertBoldfaceAction extends InsertEditorAction {

    public InsertBoldfaceAction() {
        super("Bold face", TexifyIcons.FONT_BOLD, "\\textbf{", "}");
    }
}
