package nl.rubensten.texifyidea.sdk;

import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import nl.rubensten.texifyidea.TexifyIcons;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Sten Wessel
 */
public class LatexSdkType extends SdkType {

    public LatexSdkType() {
        super("LaTeX SDK");
    }

    @NotNull
    public static LatexSdkType getInstance() {
        return SdkType.findInstance(LatexSdkType.class);
    }

    @Nullable
    @Override
    public String suggestHomePath() {
        List<String> paths = findLatexInPath();
        return paths.size() > 0 ? findLatexInPath().get(0) : null;
    }

    @NotNull
    @Override
    public Collection<String> suggestHomePaths() {
        return findLatexInPath();
    }

    @Override
    public boolean isValidSdkHome(String path) {
        return path.toLowerCase().contains("miktex");
    }

    @Nullable
    @Override
    public String getVersionString(@NotNull Sdk sdk) {
        return LatexSdkVariant.MIKTEX_2_9.getVersion();
    }

    @Nullable
    @Override
    public String getVersionString(String sdkHome) {
        return LatexSdkVariant.MIKTEX_2_9.getVersion();
    }

    @Override
    public String suggestSdkName(String currentSdkName, String sdkHome) {
        return "MiKTeX 2.9";
    }

    @Nullable
    @Override
    public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull SdkModel sdkModel, @NotNull SdkModificator sdkModificator) {
        return null;
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return "LaTeX SDK";
    }

    @Override
    public Icon getIcon() {
        return TexifyIcons.LATEX_MODULE;
    }

    @NotNull
    @Override
    public Icon getIconForAddAction() {
        return TexifyIcons.LATEX_MODULE;
    }

    private List<String> findLatexInPath() {
        List<String> result = new ArrayList<>();

        final String path = System.getenv("PATH");
        for (String root : path.split(File.pathSeparator)) {
            if (root.contains("\\miktex\\")) {
                // Get the root path for the SDK
                final File file = new File(root.split("\\\\miktex\\\\")[0]);
                try {
                    result.add(file.getCanonicalPath());
                }
                catch (IOException ignored) {
                }
            }
        }
        return result;
    }

    @Override
    public void saveAdditionalData(@NotNull SdkAdditionalData additionalData, @NotNull Element additional) {

    }
}
