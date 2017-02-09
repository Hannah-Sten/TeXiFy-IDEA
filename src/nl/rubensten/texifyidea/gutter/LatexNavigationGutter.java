package nl.rubensten.texifyidea.gutter;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.TexifyUtil;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexRequiredParam;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * @author Ruben Schellekens
 */
public class LatexNavigationGutter extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                            Collection<? super RelatedItemLineMarkerInfo> result) {
        if (element instanceof LatexCommands) {
            LatexCommands command = (LatexCommands)element;
            if (command.getCommandToken().getText().equals("\\input")) {

                // Try to get the filename parameter
                List<LatexRequiredParam> params = TexifyUtil.getRequiredParameters(command);
                if (params.size() > 0) {
                    String fileName = params.get(0).getGroup().getText();
                    fileName = fileName.substring(1, fileName.length() - 1);

                    VirtualFile file = element.getContainingFile().getContainingDirectory().getVirtualFile().findFileByRelativePath(fileName);

                    // Optional file extension
                    if (file == null) {
                        file = element.getContainingFile().getContainingDirectory().getVirtualFile().findFileByRelativePath(fileName + ".tex");
                    }

                    // Build gutter icon if file was found
                    if (file != null) {
                        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                                .create(TexifyIcons.LATEX_FILE)
                                .setTarget(PsiManager.getInstance(element.getProject()).findFile(file))
                                .setTooltipText("Go to input file");

                        result.add(builder.createLineMarkerInfo(element));
                    }
                }

            }
        }
    }

}
