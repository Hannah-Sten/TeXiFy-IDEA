package nl.rubensten.texifyidea.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sten Wessel
 */
public abstract class TexifyInspectionBase extends LocalInspectionTool {
    private static final String GROUP_DISPLAY_NAME = "TeXiFy";

    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return GROUP_DISPLAY_NAME;
    }
}
