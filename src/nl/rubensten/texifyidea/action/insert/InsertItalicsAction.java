package nl.rubensten.texifyidea.action.insert;

import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.action.InsertEditorAction;

/**
 * @author Ruben Schellekens
 */
public class InsertItalicsAction extends InsertEditorAction {

    public InsertItalicsAction() {
        super("Italics", TexifyIcons.FONT_ITALICS, "\\textit{", "}");
    }
}
