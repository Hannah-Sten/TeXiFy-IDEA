package nl.rubensten.texifyidea.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import nl.rubensten.texifyidea.TexifyIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Sten Wessel
 */
public class LatexRunConfigurationType implements ConfigurationType {

    @Override
    public String getDisplayName() {
        return "LaTeX";
    }

    @Override
    public String getConfigurationTypeDescription() {
        return "Build a LaTeX file";
    }

    @Override
    public Icon getIcon() {
        return TexifyIcons.LATEX_FILE;
    }

    @NotNull
    @Override
    public String getId() {
        return "LATEX_RUN_CONFIGURATION";
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{new LatexConfigurationFactory(this)};
    }
}
