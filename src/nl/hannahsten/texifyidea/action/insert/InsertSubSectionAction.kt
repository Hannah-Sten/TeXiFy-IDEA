package nl.hannahsten.texifyidea.action.insert;

import nl.hannahsten.texifyidea.TexifyIcons;
import nl.hannahsten.texifyidea.action.InsertEditorAction;

/**
 * @author Hannah Schellekens
 */
public class InsertSubSectionAction extends InsertEditorAction {

    public InsertSubSectionAction() {
        super("Subsection", TexifyIcons.DOT_SUBSECTION, "\\subsection{", "}");
    }
}
