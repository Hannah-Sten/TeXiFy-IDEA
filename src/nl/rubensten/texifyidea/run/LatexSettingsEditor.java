package nl.rubensten.texifyidea.run;

import com.intellij.openapi.fileChooser.FileTypeDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
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
    protected void resetEditorFrom(@NotNull LatexRunConfiguration s) {
        compiler.getComponent().setSelectedItem(s.getCompiler());
        ((TextFieldWithBrowseButton)mainFile.getComponent()).setText(s.getMainFile().getPath());
        auxDir.setSelected(s.hasAuxDir());

        project = s.getProject();
    }

    @Override
    protected void applyEditorTo(@NotNull LatexRunConfiguration s) throws ConfigurationException {
        s.setCompiler((LatexCompiler)compiler.getComponent().getSelectedItem());
        s.setMainFile(((TextFieldWithBrowseButton)mainFile.getComponent()).getText());
        s.setAuxDir(auxDir.isSelected());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        createUIComponents();
        return panel;
    }

    private void createUIComponents() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        ComboBox<LatexCompiler> compilerField = new ComboBox<>(LatexCompiler.values());
        compiler = LabeledComponent.create(compilerField, "Compiler");
        panel.add(compiler);

        TextFieldWithBrowseButton mainFileField = new TextFieldWithBrowseButton();
        mainFileField.addBrowseFolderListener(new TextBrowseFolderListener(
                new FileTypeDescriptor("Choose a file to compile", ".tex")
                        .withRoots(ProjectRootManager.getInstance(project).getContentRootsFromAllModules())
        ));
        mainFile = LabeledComponent.create(mainFileField, "Main file to compile");
        panel.add(mainFile);

        panel.add(new TitledSeparator("Options"));

        auxDir = new JCheckBox("Separate auxiliary files from output (MiKTeX only)");
        auxDir.setSelected(true);
        panel.add(auxDir);
    }
}
