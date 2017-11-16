package nl.rubensten.texifyidea.run;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
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
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.SeparatorComponent;
import com.intellij.ui.TitledSeparator;
import nl.rubensten.texifyidea.run.LatexCompiler.Format;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ItemEvent;

/**
 * @author Sten Wessel
 */
public class LatexSettingsEditor extends SettingsEditor<LatexRunConfiguration> {

    private JPanel panel;
    private LabeledComponent<ComboBox> compiler;
    private JCheckBox enableCompilerPath;
    private TextFieldWithBrowseButton compilerPath;
    private LabeledComponent<RawCommandLineEditor> compilerArguments;
    private LabeledComponent<ComponentWithBrowseButton> mainFile;
    private JCheckBox auxDir;
    private LabeledComponent<ComboBox> outputFormat;
    private BibliographyPanel bibliographyPanel;

    private Project project;

    public LatexSettingsEditor(Project project) {
        this.project = project;
    }

    @Override
    protected void resetEditorFrom(@NotNull LatexRunConfiguration runConfiguration) {
        // Reset the selected compiler.
        compiler.getComponent().setSelectedItem(runConfiguration.getCompiler());

        // Reset the custom compiler path
        compilerPath.setText(runConfiguration.getCompilerPath());
        enableCompilerPath.setSelected(runConfiguration.getCompilerPath() != null);

        // Reset compiler arguments
        compilerArguments.getComponent().setText(runConfiguration.getCompilerArguments());

        // Reset the main file to compile.
        TextFieldWithBrowseButton txtFile = (TextFieldWithBrowseButton)mainFile.getComponent();
        VirtualFile virtualFile = runConfiguration.getMainFile();
        String path = (virtualFile == null ? "" : virtualFile.getPath());
        txtFile.setText(path);

        // Reset seperate auxiliary files.
        auxDir.setSelected(runConfiguration.hasAuxiliaryDirectories());

        // Reset output format.
        outputFormat.getComponent().setSelectedItem(runConfiguration.getOutputFormat());

        // Reset project.
        project = runConfiguration.getProject();

        // Reset bibliography
        bibliographyPanel.setConfiguration(runConfiguration.getBibRunConfig());
    }

    @Override
    protected void applyEditorTo(@NotNull LatexRunConfiguration runConfiguration) throws
            ConfigurationException {
        // Apply chosen compiler.
        LatexCompiler chosenCompiler = (LatexCompiler)compiler.getComponent().getSelectedItem();
        runConfiguration.setCompiler(chosenCompiler);

        // Apply custom compiler path if applicable
        runConfiguration.setCompilerPath(enableCompilerPath.isSelected() ? compilerPath.getText() : null);

        // Apply custom compiler arguments
        runConfiguration.setCompilerArguments(compilerArguments.getComponent().getText());

        // Apply main file.
        TextFieldWithBrowseButton txtFile = (TextFieldWithBrowseButton)mainFile.getComponent();
        String filePath = txtFile.getText();
        runConfiguration.setMainFile(filePath);

        // Apply auxiliary files.
        boolean auxDirectories = auxDir.isSelected();
        runConfiguration.setAuxiliaryDirectories(auxDirectories);

        // Apply output format.
        Format format = (Format)outputFormat.getComponent().getSelectedItem();
        runConfiguration.setOutputFormat(format);

        // Apply bibliography
        runConfiguration.setBibRunConfig(bibliographyPanel.getConfiguration());
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
        panel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP));

        // Compiler
        ComboBox<LatexCompiler> compilerField = new ComboBox<>(LatexCompiler.values());
        compiler = LabeledComponent.create(compilerField, "Compiler");
        panel.add(compiler);

        // Optional custom path for compiler executable
        enableCompilerPath = new JCheckBox("Select custom compiler executable path (required on Mac OS X)");
        panel.add(enableCompilerPath);

        compilerPath = new TextFieldWithBrowseButton();
        compilerPath.addBrowseFolderListener(
                new TextBrowseFolderListener(
                        new FileChooserDescriptor(true, false, false, false, false, false)
                                .withFileFilter(virtualFile -> virtualFile.getNameWithoutExtension().equals(((LatexCompiler)compilerField.getSelectedItem()).getExecutableName()))
                                .withTitle("Choose " + compilerField.getSelectedItem() + " executable")
                )
        );
        compilerPath.setEnabled(false);
        compilerPath.addPropertyChangeListener("enabled", e -> {
            if (!((Boolean)e.getNewValue())) {
                compilerPath.setText(null);
            }
        });
        enableCompilerPath.addItemListener(e -> compilerPath.setEnabled(e.getStateChange() == ItemEvent.SELECTED));

        panel.add(compilerPath);

        // Optional custom compiler arguments
        final String argumentsTitle = "Custom compiler arguments";
        RawCommandLineEditor argumentsField = new RawCommandLineEditor();
        argumentsField.setDialogCaption(argumentsTitle);

        compilerArguments = LabeledComponent.create(argumentsField, argumentsTitle);
        panel.add(compilerArguments);

        panel.add(new SeparatorComponent());

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

        // Output format.
        ComboBox<Format> cboxFormat = new ComboBox<>(Format.values());
        outputFormat = LabeledComponent.create(cboxFormat, "Output format");
        outputFormat.setSize(128, outputFormat.getHeight());
        panel.add(outputFormat);

        panel.add(new TitledSeparator("Extensions"));

        // Extension panels
        bibliographyPanel = new BibliographyPanel(project);
        panel.add(bibliographyPanel);
    }
}
