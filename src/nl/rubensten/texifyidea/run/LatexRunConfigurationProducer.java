package nl.rubensten.texifyidea.run;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import nl.rubensten.texifyidea.file.LatexFileType;
import nl.rubensten.texifyidea.util.PsiUtilKt;

/**
 * @author Ruben Schellekens
 */
public class LatexRunConfigurationProducer extends RunConfigurationProducer<LatexRunConfiguration> {

    protected LatexRunConfigurationProducer() {
        super(LatexRunConfigurationType.getInstance());
    }

    @Override
    protected boolean setupConfigurationFromContext(LatexRunConfiguration runConfiguration,
                                                    ConfigurationContext context,
                                                    Ref<PsiElement> sourceElement) {
        Location location = context.getLocation();
        if (location == null) {
            return false;
        }

        PsiFile container = getEntryPointContainer(location);
        if (container == null) {
            return false;
        }

        VirtualFile mainFile = container.getVirtualFile();
        if (mainFile == null) {
            return false;
        }

        // Only activate on .tex files.
        final String extension = mainFile.getExtension();
        final String texTension = LatexFileType.INSTANCE.getDefaultExtension();
        if (extension == null || !extension.equalsIgnoreCase(texTension)) {
            return false;
        }

        // Setup run configuration.
        runConfiguration.setMainFile(mainFile);
        runConfiguration.setDefaultAuxiliaryDirectories();
        runConfiguration.setDefaultCompiler();
        runConfiguration.setDefaultOutputFormat();
        runConfiguration.setSuggestedName();

        if (PsiUtilKt.hasBibliography(container)) {
            runConfiguration.generateBibRunConfig();
        }

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
        PsiFile psiFile = context.getDataContext().getData(PlatformDataKeys.PSI_FILE);
        if (psiFile == null) {
            return false;
        }

        VirtualFile currentFile = psiFile.getVirtualFile();

        return mainFile.getPath().equals(currentFile.getPath());
    }
}
