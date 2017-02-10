package nl.rubensten.texifyidea.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sten Wessel
 */
public class LatexConfigurationFactory extends ConfigurationFactory {

    private static final String FACTORY_NAME = "LaTeX configuration factory";

    public LatexConfigurationFactory(ConfigurationType type) {
        super(type);
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new LatexRunConfiguration(project, this, "LaTeX");
    }

    @Override
    public String getName() {
        return FACTORY_NAME;
    }
}
