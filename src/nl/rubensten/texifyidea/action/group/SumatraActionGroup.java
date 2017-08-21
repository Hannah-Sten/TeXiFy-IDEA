package nl.rubensten.texifyidea.action.group;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.util.SystemInfo;

/**
 * @author Sten Wessel
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
}
