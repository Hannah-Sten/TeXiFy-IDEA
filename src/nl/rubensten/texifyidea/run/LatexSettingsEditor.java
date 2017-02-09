package nl.rubensten.texifyidea.run;

import com.intellij.openapi.fileChooser.FileTypeDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.TitledSeparator;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Sten Wessel
 */
public class LatexSettingsEditor extends SettingsEditor<LatexRunConfiguration> {

    private JPanel panel;
    private LabeledComponent<ComboBox> compiler;
    private LabeledComponent<ComponentWithBrowseButton> mainFile;
    private JCheckBox auxDir;

    private Project project;

    public LatexSettingsEditor(Project project) {
        this.project = project;
    }

    @Override
    protected void resetEditorFrom(@NotNull LatexRunConfiguration runConfiguration) {
        // Reset the selected compiler.
        compiler.getComponent().setSelectedItem(runConfiguration.getCompiler());

        // Reset the main file to compile.
        TextFieldWithBrowseButton txtFile = (TextFieldWithBrowseButton)mainFile.getComponent();
        VirtualFile virtualFile = runConfiguration.getMainFile();
        String path = (virtualFile == null ? "" : virtualFile.getPath());
        txtFile.setText(path);

        // Reset seperate auxiliary files.
        auxDir.setSelected(runConfiguration.hasAuxDir());

        // Reset project.
        project = runConfiguration.getProject();
    }

    @Override
    protected void applyEditorTo(@NotNull LatexRunConfiguration runConfiguration) throws
            ConfigurationException {
        // Apply chosen compiler.
        LatexCompiler chosenCompiler = (LatexCompiler)compiler.getComponent().getSelectedItem();
        runConfiguration.setCompiler(chosenCompiler);

        // Apply main file.
        TextFieldWithBrowseButton txtFile = (TextFieldWithBrowseButton)mainFile.getComponent();
        String filePath = txtFile.getText();
        runConfiguration.setMainFile(filePath);

        // Apply auxiliary files.
        boolean auxDirectories = auxDir.isSelected();
        runConfiguration.setAuxDir(auxDirectories);
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        createUIComponents();
        return panel;
    }

    private void createUIComponents() {
        // Layout
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Compiler
        ComboBox<LatexCompiler> compilerField = new ComboBox<>(LatexCompiler.values());
        compiler = LabeledComponent.create(compilerField, "Compiler");
        panel.add(compiler);

        // Main file selection
        TextFieldWithBrowseButton mainFileField = new TextFieldWithBrowseButton();
        mainFileField.addBrowseFolderListener(new TextBrowseFolderListener(
                new FileTypeDescriptor("Choose a file to compile", ".tex")
                        .withRoots(ProjectRootManager.getInstance(project)
                                .getContentRootsFromAllModules())
        ));
        mainFile = LabeledComponent.create(mainFileField, "Main file to compile");
        panel.add(mainFile);

        panel.add(new TitledSeparator("Options"));

        // Auxiliary files
        auxDir = new JCheckBox("Separate auxiliary files from output (MiKTeX only)");
        auxDir.setSelected(true);
        panel.add(auxDir);
    }
}
