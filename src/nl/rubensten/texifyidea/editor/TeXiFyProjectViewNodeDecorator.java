package nl.rubensten.texifyidea.editor;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import nl.rubensten.texifyidea.TexifyIcons;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ruben Schellekens
 */
public class TeXiFyProjectViewNodeDecorator implements ProjectViewNodeDecorator {

    private static final Map<String, Icon> FILE_ICONS = new HashMap<>();
    static {
        FILE_ICONS.put("pdf", TexifyIcons.PDF_FILE);
        FILE_ICONS.put("dvi", TexifyIcons.DVI_FILE);
    }

    private void setIcon(ProjectViewNode projectViewNode, PresentationData presentationData) {
        VirtualFile file = projectViewNode.getVirtualFile();
        if (file == null) {
            return;
        }
        if (file.isDirectory()) {
            return;
        }

        String extension = file.getExtension();
        if (extension == null) {
            return;
        }

        Icon icon = FILE_ICONS.get(file.getExtension().toLowerCase());
        if (icon == null) {
            return;
        }

        presentationData.setIcon(icon);
    }

    @Override
    public void decorate(ProjectViewNode projectViewNode, PresentationData presentationData) {
        setIcon(projectViewNode, presentationData);
    }

    @Override
    public void decorate(PackageDependenciesNode packageDependenciesNode, ColoredTreeCellRenderer coloredTreeCellRenderer) {
        // Do nothing.
    }
}
