package nl.rubensten.texifyidea.action.insert;

import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.action.InsertEditorAction;

/**
 * @author Ruben Schellekens
 */
public class InsertSectionAction extends InsertEditorAction {

    public InsertSectionAction() {
        super("Section", TexifyIcons.DOT_SECTION, "\\section{", "}");
    }
}
