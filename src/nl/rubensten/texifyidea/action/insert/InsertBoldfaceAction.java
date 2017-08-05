package nl.rubensten.texifyidea.action.insert;

import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.action.InsertEditorAction;

/**
 * @author Ruben Schellekens
 */
public class InsertBoldfaceAction extends InsertEditorAction {

    public InsertBoldfaceAction() {
        super("Bold face", TexifyIcons.FONT_BOLD, "\\textbf{", "}");
    }
}
