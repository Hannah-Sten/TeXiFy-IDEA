package nl.rubensten.texifyidea.action.insert;

import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.action.InsertEditorAction;

/**
 * @author Ruben Schellekens
 */
public class InsertSmallCapsAction extends InsertEditorAction {

    public InsertSmallCapsAction() {
        super("Small capitals", TexifyIcons.FONT_SMALLCAPS, "\\textsc{", "}");
    }
}
