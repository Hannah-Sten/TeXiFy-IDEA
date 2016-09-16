package nl.rubensten.texifyidea.modules;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;

/**
 * @author Sten Wessel
 */
public class LatexModuleBuilder extends ModuleBuilder {

    @Override
    public void setupRootModel(ModifiableRootModel modifiableRootModel) throws ConfigurationException {

    }

    @Override
    public ModuleType getModuleType() {
        return LatexModuleType.getInstance();
    }
}
