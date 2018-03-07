package nl.rubensten.texifyidea.action.group;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.run.SumatraConversationKt;

/**
 * @author Ruben Schellekens, Sten Wessel
 */
public class SumatraActionGroup extends DefaultActionGroup {

    @Override
    public boolean canBePerformed(DataContext context) {
        return SumatraConversationKt.isSumatraAvailable();
    }

    @Override
    public boolean hideIfNoVisibleChildren() {
        return true;
    }

    @Override
    public void update(AnActionEvent event) {
        super.update(event);
        event.getPresentation().setIcon(TexifyIcons.SUMATRA);
    }
}
