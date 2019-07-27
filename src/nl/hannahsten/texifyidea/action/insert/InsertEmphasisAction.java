package nl.hannahsten.texifyidea.action.insert;

import nl.hannahsten.texifyidea.action.InsertEditorAction;

/**
 * @author Adam Williams
 */
public class InsertEmphasisAction extends InsertEditorAction {

    public InsertEmphasisAction() {
        super("Emphasis", null, "\\emph{", "}");
    }
}
