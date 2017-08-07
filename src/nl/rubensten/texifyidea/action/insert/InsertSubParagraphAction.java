package nl.rubensten.texifyidea.action.insert;

import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.action.InsertEditorAction;

/**
 * @author Ruben Schellekens
 */
public class InsertSubParagraphAction extends InsertEditorAction {

    public InsertSubParagraphAction() {
        super("Subparagraph", TexifyIcons.DOT_SUBPARAGRAPH, "\\subparagraph{", "}");
    }
}
