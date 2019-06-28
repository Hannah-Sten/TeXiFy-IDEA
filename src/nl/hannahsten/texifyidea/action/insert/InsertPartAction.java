package nl.hannahsten.texifyidea.action.insert;

import nl.hannahsten.texifyidea.TexifyIcons;
import nl.hannahsten.texifyidea.action.InsertEditorAction;

/**
 * @author Hannah Schellekens
 */
public class InsertPartAction extends InsertEditorAction {

    public InsertPartAction() {
        super("Part", TexifyIcons.DOT_PART, "\\part{", "}");
    }
}
