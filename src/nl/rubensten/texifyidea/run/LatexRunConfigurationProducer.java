package nl.rubensten.texifyidea.run;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 * @author Ruben Schellekens
 */
public class LatexRunConfigurationProducer extends RunConfigurationProducer<LatexRunConfiguration> {

    static {
        Logger.getInstance(LatexRunConfigurationProducer.class)
                .info("TEXIFY RUNPROD: init_static: SOMETHING PLS");
    }

    protected LatexRunConfigurationProducer(ConfigurationFactory configurationFactory) {
        super(configurationFactory);

        Logger.getInstance(getClass()).info("TEXIFY RUNPROD: init_fac: DN DEES");
    }

    protected LatexRunConfigurationProducer(ConfigurationType configurationType) {
        super(configurationType);

        Logger.getInstance(getClass()).info("TEXIFY RUNPROD: init_prod: DN DIEË");
    }

    @Override
    protected boolean setupConfigurationFromContext(LatexRunConfiguration runConfiguration,
                                                    ConfigurationContext context,
                                                    Ref<PsiElement> sourceElement) {
        Logger.getInstance(getClass()).info("TEXIFY RUNPROD: setup: Start");

        Location location = context.getLocation();
        if (location == null) {
            return false;
        }

        Logger.getInstance(getClass()).info("TEXIFY RUNPROD: setup: Location");

        PsiFile container = getEntryPointContainer(location);
        VirtualFile mainFile = container.getVirtualFile();
        if (mainFile == null) {
            return false;
        }

        Logger.getInstance(getClass()).info("TEXIFY RUNPROD: setup: Container");

        runConfiguration.setMainFile(mainFile);
        runConfiguration.setName(mainFile.getNameWithoutExtension());
        runConfiguration.setAuxDir(true);
        runConfiguration.setCompiler(LatexCompiler.PDFLATEX);

        Logger.getInstance(getClass()).info("TEXIFY RUNPROD: setup: Return true");

        return true;
    }

    private PsiFile getEntryPointContainer(Location location) {
        if (location == null) {
            return null;
        }

        PsiElement locationElement = location.getPsiElement();
        return locationElement.getContainingFile();
    }

    @Override
    public boolean isConfigurationFromContext(LatexRunConfiguration runConfiguration,
                                              ConfigurationContext context) {
        VirtualFile mainFile = runConfiguration.getMainFile();
        VirtualFile currentFile = context.getDataContext().getData(DataKeys.PSI_FILE).getVirtualFile();

        Logger.getInstance(getClass()).info("TEXIFY RUNPROD: isConfig?: Executes");

        return mainFile.getPath().equals(currentFile.getPath());
    }
}
