package nl.rubensten.texifyidea.action.insert;

import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.action.InsertEditorAction;

/**
 * @author Ruben Schellekens
 */
public class InsertParagraphAction extends InsertEditorAction {

    public InsertParagraphAction() {
        super("Paragraph", TexifyIcons.DOT_PARAGRAPH, "\\paragraph{", "}");
    }
}
