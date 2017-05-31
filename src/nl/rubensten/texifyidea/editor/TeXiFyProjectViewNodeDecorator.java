package nl.rubensten.texifyidea.editor;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import nl.rubensten.texifyidea.TexifyIcons;

/**
 * @author Ruben Schellekens
 */
public class TeXiFyProjectViewNodeDecorator implements ProjectViewNodeDecorator {

    @Override
    public void decorate(ProjectViewNode projectViewNode, PresentationData presentationData) {
        VirtualFile file = projectViewNode.getVirtualFile();
        if (file.isDirectory()) {
            return;
        }

        if (file.getExtension().equalsIgnoreCase("pdf")) {
            presentationData.setIcon(TexifyIcons.PDF_FILE);
        }
    }

    @Override
    public void decorate(PackageDependenciesNode packageDependenciesNode, ColoredTreeCellRenderer coloredTreeCellRenderer) {
        // Do nothing.
    }
}
