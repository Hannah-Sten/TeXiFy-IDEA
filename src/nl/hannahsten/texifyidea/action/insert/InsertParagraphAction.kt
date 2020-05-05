package nl.hannahsten.texifyidea.action.insert;

import nl.hannahsten.texifyidea.TexifyIcons;
import nl.hannahsten.texifyidea.action.InsertEditorAction;

/**
 * @author Hannah Schellekens
 */
public class InsertParagraphAction extends InsertEditorAction {

    public InsertParagraphAction() {
        super("Paragraph", TexifyIcons.DOT_PARAGRAPH, "\\paragraph{", "}");
    }
}
