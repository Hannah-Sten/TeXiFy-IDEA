package nl.rubensten.texifyidea.action.insert;

import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.action.InsertEditorAction;

/**
 * @author Ruben Schellekens
 */
public class InsertUnderlineAction extends InsertEditorAction {

    public InsertUnderlineAction() {
        super("Underline", TexifyIcons.FONT_UNDERLINE, "\\underline{", "}");
    }
}
