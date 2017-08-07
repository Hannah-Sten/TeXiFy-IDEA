package nl.rubensten.texifyidea.action.insert;

import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.action.InsertEditorAction;

/**
 * @author Ruben Schellekens
 */
public class InsertPartAction extends InsertEditorAction {

    public InsertPartAction() {
        super("Part", TexifyIcons.DOT_PART, "\\part{", "}");
    }
}
