package nl.rubensten.texifyidea.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import nl.rubensten.texifyidea.TexifyIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Ruben Schellekens, Sten Wessel
 */
public class LatexRunConfigurationType implements ConfigurationType {

    @NotNull
    public static LatexRunConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(LatexRunConfigurationType.class);
    }

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
        return TexifyIcons.BUILD;
    }

    @NotNull
    @Override
    public String getId() {
        return "LATEX_RUN_CONFIGURATION";
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[] {
                new LatexConfigurationFactory(this)
        };
    }
}
