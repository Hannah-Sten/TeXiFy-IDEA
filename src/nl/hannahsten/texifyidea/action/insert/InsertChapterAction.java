package nl.hannahsten.texifyidea.action.insert;

import nl.hannahsten.texifyidea.TexifyIcons;
import nl.hannahsten.texifyidea.action.InsertEditorAction;

/**
 * @author Hannah Schellekens
 */
public class InsertChapterAction extends InsertEditorAction {

    public InsertChapterAction() {
        super("Chapter", TexifyIcons.DOT_CHAPTER, "\\chapter{", "}");
    }
}
