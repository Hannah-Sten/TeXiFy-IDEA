package nl.rubensten.texifyidea.action.insert;

import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.action.InsertEditorAction;

/**
 * @author Ruben Schellekens
 */
public class InsertSubSubSectionAction extends InsertEditorAction {

    public InsertSubSubSectionAction() {
        super("Subsubsection", TexifyIcons.DOT_SUBSUBSECTION, "\\subsubsection{", "}");
    }
}
