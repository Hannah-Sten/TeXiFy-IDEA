package nl.rubensten.texifyidea.action.group;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.util.SystemInfo;
import nl.rubensten.texifyidea.TexifyIcons;

/**
 * @author Ruben Schellekens, Sten Wessel
 */
public class SumatraActionGroup extends DefaultActionGroup {

    @Override
    public boolean canBePerformed(DataContext context) {
        return SystemInfo.isWindows;
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
