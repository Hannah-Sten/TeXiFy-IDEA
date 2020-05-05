package nl.hannahsten.texifyidea.action.insert;

import nl.hannahsten.texifyidea.TexifyIcons;
import nl.hannahsten.texifyidea.action.InsertEditorAction;

/**
 * @author Hannah Schellekens
 */
public class InsertSectionAction extends InsertEditorAction {

    public InsertSectionAction() {
        super("Section", TexifyIcons.DOT_SECTION, "\\section{", "}");
    }
}
