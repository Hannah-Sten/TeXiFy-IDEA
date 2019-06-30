package nl.hannahsten.texifyidea.action.insert;

import nl.hannahsten.texifyidea.TexifyIcons;
import nl.hannahsten.texifyidea.action.InsertEditorAction;

/**
 * @author Hannah Schellekens
 */
public class InsertSlantedAction extends InsertEditorAction {

    public InsertSlantedAction() {
        super("Slanted", TexifyIcons.FONT_SLANTED, "\\textsl{", "}");
    }
}
