package nl.rubensten.texifyidea.action.insert;

import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.action.InsertEditorAction;

/**
 * @author Ruben Schellekens
 */
public class InsertChapterAction extends InsertEditorAction {

    public InsertChapterAction() {
        super("Chapter", TexifyIcons.DOT_CHAPTER, "\\chapter{", "}");
    }
}
