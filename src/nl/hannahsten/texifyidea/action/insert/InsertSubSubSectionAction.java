package nl.hannahsten.texifyidea.action.insert;

import nl.hannahsten.texifyidea.TexifyIcons;
import nl.hannahsten.texifyidea.action.InsertEditorAction;

/**
 * @author Hannah Schellekens
 */
public class InsertSubSubSectionAction extends InsertEditorAction {

    public InsertSubSubSectionAction() {
        super("Subsubsection", TexifyIcons.DOT_SUBSUBSECTION, "\\subsubsection{", "}");
    }
}
