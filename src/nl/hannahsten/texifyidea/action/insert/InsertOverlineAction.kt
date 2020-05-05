package nl.hannahsten.texifyidea.action.insert;

import nl.hannahsten.texifyidea.TexifyIcons;
import nl.hannahsten.texifyidea.action.InsertEditorAction;

/**
 * @author Hannah Schellekens
 */
public class InsertOverlineAction extends InsertEditorAction {

    public InsertOverlineAction() {
        super("Overline", TexifyIcons.FONT_OVERLINE, "\\overline{", "}");
    }
}
