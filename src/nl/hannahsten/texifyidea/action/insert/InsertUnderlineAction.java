package nl.hannahsten.texifyidea.action.insert;

import nl.hannahsten.texifyidea.TexifyIcons;
import nl.hannahsten.texifyidea.action.InsertEditorAction;

/**
 * @author Hannah Schellekens
 */
public class InsertUnderlineAction extends InsertEditorAction {

    public InsertUnderlineAction() {
        super("Underline", TexifyIcons.FONT_UNDERLINE, "\\underline{", "}");
    }
}
