package nl.hannahsten.texifyidea.action.insert;

import nl.hannahsten.texifyidea.TexifyIcons;
import nl.hannahsten.texifyidea.action.InsertEditorAction;

/**
 * @author Hannah Schellekens
 */
public class InsertSubParagraphAction extends InsertEditorAction {

    public InsertSubParagraphAction() {
        super("Subparagraph", TexifyIcons.DOT_SUBPARAGRAPH, "\\subparagraph{", "}");
    }
}
