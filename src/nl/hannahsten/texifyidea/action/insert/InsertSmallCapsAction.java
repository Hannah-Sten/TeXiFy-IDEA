package nl.hannahsten.texifyidea.action.insert;

import nl.hannahsten.texifyidea.TexifyIcons;
import nl.hannahsten.texifyidea.action.InsertEditorAction;

/**
 * @author Hannah Schellekens
 */
public class InsertSmallCapsAction extends InsertEditorAction {

    public InsertSmallCapsAction() {
        super("Small capitals", TexifyIcons.FONT_SMALLCAPS, "\\textsc{", "}");
    }
}
