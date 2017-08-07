package nl.rubensten.texifyidea.action.insert;

import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.action.InsertEditorAction;

/**
 * @author Ruben Schellekens
 */
public class InsertSubSectionAction extends InsertEditorAction {

    public InsertSubSectionAction() {
        super("Subsection", TexifyIcons.DOT_SUBSECTION, "\\subsection{", "}");
    }
}
