package nl.rubensten.texifyidea.gutter;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.lang.LatexRegularCommand;
import nl.rubensten.texifyidea.lang.RequiredFileArgument;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexRequiredParam;
import nl.rubensten.texifyidea.util.FilesKt;
import nl.rubensten.texifyidea.util.PsiCommandsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Ruben Schellekens
 */
public class LatexNavigationGutter extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                            Collection<? super RelatedItemLineMarkerInfo> result) {
        // Only make markers when dealing with commands.
        if (!(element instanceof LatexCommands)) {
            return;
        }

        LatexCommands commands = (LatexCommands)element;
        PsiElement commandToken = commands.getCommandToken();
        if (commandToken == null) {
            return;
        }

        String fullCommand = commands.getCommandToken().getText();
        if (fullCommand == null) {
            return;
        }

        // True when it doesnt have a required file argument, but must be handled.
        boolean ignoreFileArgument = "\\RequirePackage".equals(fullCommand) ||
                "\\usepackage".equals(fullCommand);

        // Fetch the corresponding LatexRegularCommand object.
        String commandName = fullCommand.substring(1);
        LatexRegularCommand commandHuh = LatexRegularCommand.get(commandName);
        if (commandHuh == null && !ignoreFileArgument) {
            return;
        }

        List<RequiredFileArgument> arguments = commandHuh.getArgumentsOf(RequiredFileArgument.class);
        if (arguments.isEmpty() && !ignoreFileArgument) {
            return;
        }

        // Get the required file arguments.
        RequiredFileArgument argument;
        if (ignoreFileArgument) {
            argument = new RequiredFileArgument("", "sty");
        }
        else {
            argument = arguments.get(0);
        }

        List<LatexRequiredParam> requiredParams = PsiCommandsKt.requiredParameters(commands);
        if (requiredParams.isEmpty()) {
            return;
        }

        // Make filename. Substring is to remove { and }.
        String fileName = requiredParams.get(0).getGroup().getText();
        fileName = fileName.substring(1, fileName.length() - 1);

        // Look up target file.
        PsiFile containingFile = element.getContainingFile();
        if (containingFile == null) {
            return;
        }
        PsiDirectory containingDirectory = containingFile.getContainingDirectory();
        if (containingDirectory == null) {
            return;
        }

        List<VirtualFile> roots = new ArrayList<>();
        PsiFile rootFile = FilesKt.findRootFile(containingFile);
        roots.add(rootFile.getContainingDirectory().getVirtualFile());
        ProjectRootManager rootManager = ProjectRootManager.getInstance(element.getProject());
        Collections.addAll(roots, rootManager.getContentSourceRoots());

        VirtualFile file = null;
        for (VirtualFile root : roots) {
            VirtualFile foundFile = FilesKt.findFile(root, fileName, argument.getSupportedExtensions());
            if (foundFile != null) {
                file = foundFile;
                break;
            }
        }

        if (file == null) {
            return;
        }

        // Build gutter icon.
        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                .create(TexifyIcons.getIconFromExtension(file.getExtension()))
                .setTarget(PsiManager.getInstance(element.getProject()).findFile(file))
                .setTooltipText("Go to referenced file '" + file.getName() + "'");

        result.add(builder.createLineMarkerInfo(element));

        throw new RuntimeException("Test! Remove this exception pls.");
    }

    @Override
    public String getName() {
        return "Navigate to referenced file";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return TexifyIcons.LATEX_FILE;
    }
}
