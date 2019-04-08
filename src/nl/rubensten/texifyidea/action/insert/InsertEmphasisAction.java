package nl.rubensten.texifyidea.action.insert;

import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.action.InsertEditorAction;

/**
 * @author Adam Williams
 */
public class InsertEmphasisAction extends InsertEditorAction {

    public InsertEmphasisAction() {
        super("Emphasis", null, "\\emph{", "}");
    }
}
