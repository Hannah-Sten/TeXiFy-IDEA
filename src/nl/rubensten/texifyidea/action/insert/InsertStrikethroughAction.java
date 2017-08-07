package nl.rubensten.texifyidea.action.insert;

import nl.rubensten.texifyidea.action.InsertEditorAction;

/**
 * @author Ruben Schellekens
 */
public class InsertStrikethroughAction extends InsertEditorAction {

    public InsertStrikethroughAction() {
        super("Strikethrough (ulem package)", null, "\\sout{", "}");
    }
}
