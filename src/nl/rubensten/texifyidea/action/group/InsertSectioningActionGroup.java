package nl.rubensten.texifyidea.action.group;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import nl.rubensten.texifyidea.action.InsertEditorAction;
import nl.rubensten.texifyidea.util.TexifyUtil;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static nl.rubensten.texifyidea.TexifyIcons.*;

/**
 * @author Ruben Schellekens
 */
public class InsertSectioningActionGroup extends DefaultActionGroup {

    private static final String[] SECTIONING = new String[] {
            "part", "chapter", "section", "subsection",
            "subsubsection", "paragraph", "subparagraph"
    };

    private static final Icon[] ICONS = new Icon[] {
            DOT_PART, DOT_CHAPTER, DOT_SECTION, DOT_SUBSECTION,
            DOT_SUBSUBSECTION, DOT_PARAGRAPH, DOT_SUBPARAGRAPH
    };

    private static final List<AnAction> ACTIONS = new ArrayList<AnAction>() {{
        for (int i = 0; i < SECTIONING.length; i++) {
            AnAction action = new InsertEditorAction(
                    TexifyUtil.capitaliseFirst(SECTIONING[i]),
                    ICONS[i],
                    "\\" + SECTIONING[i] + "{",
                    "}"
            );

            add(action);
        }
    }};

    @Override
    public void update(AnActionEvent e) {
        removeAll();
        addAll(ACTIONS);
    }
}
