package nl.rubensten.texifyidea.gutter;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.ide.util.gotoByName.GotoFileCellRenderer;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ex.WindowManagerEx;
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
import nl.rubensten.texifyidea.util.PsiKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Ruben Schellekens
 */
public class LatexNavigationGutter extends RelatedItemLineMarkerProvider {

    private static final Set<String> IGNORE_FILE_ARGUMENTS = new HashSet<>(Arrays.asList(
            "\\RequirePackage", "\\usepackage", "\\documentclass", "\\LoadClass",
            "\\LoadClassWithOptions"
    ));

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

        // True when it doesnt have a required _file_ argument, but must be handled.
        boolean ignoreFileArgument = IGNORE_FILE_ARGUMENTS.contains(fullCommand);

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
            argument = new RequiredFileArgument("", "sty", "cls");
        }
        else {
            argument = arguments.get(0);
        }

        List<LatexRequiredParam> requiredParams = PsiCommandsKt.requiredParameters(commands);
        if (requiredParams.isEmpty()) {
            return;
        }

        // Find filenames.
        List<String> fileNames = PsiKt.splitContent(requiredParams.get(0), ",");
        if (fileNames == null) return;

        // Look up target files.
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

        List<VirtualFile> files = fileNames.stream()
            .map(fileName -> {
                for (VirtualFile root : roots) {
                    VirtualFile foundFile = FilesKt.findFile(root, fileName, argument.getSupportedExtensions());
                    if (foundFile != null) {
                        return foundFile;
                    }
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (files.isEmpty()) return;

        PsiManager psiManager = PsiManager.getInstance(element.getProject());

        // Build gutter icon.
        int maxSize = WindowManagerEx.getInstanceEx().getFrame(element.getProject()).getSize().width;

        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                .create(TexifyIcons.getIconFromExtension(argument.getDefaultExtension()))
                .setTargets(files.stream().map(psiManager::findFile).collect(Collectors.toList()))
                .setPopupTitle("Navigate to Referenced File")
                .setTooltipText("Go to referenced file")
                .setCellRenderer(new GotoFileCellRenderer(maxSize));

        result.add(builder.createLineMarkerInfo(element));
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
