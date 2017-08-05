package nl.rubensten.texifyidea.action.insert;

import nl.rubensten.texifyidea.action.InsertEditorAction;

/**
 * @author Ruben Schellekens
 */
public class InsertSlantedAction extends InsertEditorAction {

    public InsertSlantedAction() {
        super("Slanted", null, "\\textsl{", "}");
    }
}
