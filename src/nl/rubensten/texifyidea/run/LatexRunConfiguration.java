package nl.rubensten.texifyidea.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.filters.RegexpFilter;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sten Wessel
 */
public class LatexRunConfiguration extends RunConfigurationBase {

    private static final String TEXIFY_PARENT = "texify";
    private static final String COMPILER = "compiler";
    private static final String MAIN_FILE = "main-file";
    private static final String AUX_DIR = "aux-dir";

    private LatexCompiler compiler;
    private VirtualFile mainFile;
    private boolean auxDir = true;

    protected LatexRunConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new LatexSettingsEditor(getProject());
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
        RegexpFilter filter = new RegexpFilter(environment.getProject(), "^$FILE_PATH$:$LINE$");

        LatexCommandLineState state = new LatexCommandLineState(environment, this);
        state.addConsoleFilters(filter);
        return state;
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);

        Element parent = element.getChild(TEXIFY_PARENT);
        if (parent == null) {
            return;
        }

        // Read compiler.
        String compilerName = parent.getChildText(COMPILER);
        try {
            this.compiler = LatexCompiler.valueOf(compilerName);
        }
        catch (IllegalArgumentException e) {
            this.compiler = null;
        }

        // Read main file.
        LocalFileSystem fileSystem = LocalFileSystem.getInstance();
        String filePath = parent.getChildText(MAIN_FILE);
        this.mainFile = fileSystem.findFileByPath(filePath);

        // Read auxiliary directories.
        String auxDirBoolean = parent.getChildText(AUX_DIR);
        this.auxDir = Boolean.parseBoolean(auxDirBoolean);
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);

        Element parent = element.getChild(TEXIFY_PARENT);

        // Create a new parent when there is no parent present.
        if (parent == null) {
            parent = new Element(TEXIFY_PARENT);
            element.addContent(parent);
        }
        // Otherwise overwrite (remove + write).
        else {
            parent.removeContent();
        }

        // Write compiler.
        final Element compilerElt = new Element(COMPILER);
        compilerElt.setText(compiler == null ? "" : compiler.name());
        parent.addContent(compilerElt);

        // Write main file.
        final Element mainFileElt = new Element(MAIN_FILE);
        mainFileElt.setText((mainFile == null ? "" : mainFile.getPath()));
        parent.addContent(mainFileElt);

        // Write auxiliary directories.
        final Element auxDirElt = new Element(AUX_DIR);
        auxDirElt.setText(Boolean.toString(auxDir));
        parent.addContent(auxDirElt);
    }

    public LatexCompiler getCompiler() {
        return compiler;
    }

    public void setCompiler(LatexCompiler compiler) {
        this.compiler = compiler;
    }

    public VirtualFile getMainFile() {
        return this.mainFile;
    }

    /**
     * Looks up the corresponding {@link VirtualFile} and calls {@link
     * LatexRunConfiguration#getMainFile()}.
     */
    public void setMainFile(String mainFilePath) {
        LocalFileSystem fileSystem = LocalFileSystem.getInstance();
        setMainFile(fileSystem.findFileByPath(mainFilePath));
    }

    public void setMainFile(VirtualFile mainFile) {
        this.mainFile = mainFile;
    }

    public boolean hasAuxDir() {
        return this.auxDir;
    }

    public void setAuxDir(boolean auxDir) {
        this.auxDir = auxDir;
    }

    @Override
    public String toString() {
        return "LatexRunConfiguration{" + "compiler=" + compiler +
                ", mainFile=" + mainFile +
                ", auxDir=" + auxDir +
                '}';
    }

}
