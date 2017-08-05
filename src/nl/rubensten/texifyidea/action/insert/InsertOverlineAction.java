package nl.rubensten.texifyidea.action.insert;

import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.action.InsertEditorAction;

/**
 * @author Ruben Schellekens
 */
public class InsertOverlineAction extends InsertEditorAction {

    public InsertOverlineAction() {
        super("Overline", TexifyIcons.FONT_OVERLINE, "\\overline{", "}");
    }
}
