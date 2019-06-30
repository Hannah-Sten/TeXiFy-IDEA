package nl.hannahsten.texifyidea.action.insert;

import nl.hannahsten.texifyidea.TexifyIcons;
import nl.hannahsten.texifyidea.action.InsertEditorAction;

/**
 * @author Hannah Schellekens
 */
public class InsertItalicsAction extends InsertEditorAction {

    public InsertItalicsAction() {
        super("Italics", TexifyIcons.FONT_ITALICS, "\\textit{", "}");
    }
}
