package nl.rubensten.texifyidea.modules;

import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import nl.rubensten.texifyidea.TexifyIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Sten Wessel
 */
public class LatexModuleType extends ModuleType<LatexModuleBuilder> {

    private static final String ID = "LATEX_MODULE_TYPE";

    public LatexModuleType() {
        super(ID);
    }

    public static LatexModuleType getInstance() {
        return (LatexModuleType)ModuleTypeManager.getInstance().findByID(ID);
    }

    @NotNull
    @Override
    public LatexModuleBuilder createModuleBuilder() {
        return new LatexModuleBuilder();
    }

    @NotNull
    @Override
    public String getName() {
        return "LaTeX";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "LaTeX";
    }

    @Override
    public Icon getBigIcon() {
        return TexifyIcons.LATEX_MODULE;
    }

    @Override
    public Icon getNodeIcon(@Deprecated boolean isOpened) {
        return TexifyIcons.LATEX_MODULE;
    }
}
