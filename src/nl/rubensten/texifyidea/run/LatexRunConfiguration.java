package nl.rubensten.texifyidea.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationException;
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
        if (parent != null) {
            try {
                this.compiler = LatexCompiler.valueOf(parent.getChildText(COMPILER));
            }
            catch (IllegalArgumentException e) {
                this.compiler = null;
            }

            this.mainFile = LocalFileSystem.getInstance().findFileByPath(parent.getChildText(MAIN_FILE));
            this.auxDir = Boolean.parseBoolean(parent.getChildText(AUX_DIR));
        }
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);

        Element parent = element.getChild(TEXIFY_PARENT);
        if (parent == null) {
            parent = new Element(TEXIFY_PARENT);
            element.addContent(parent);
        }
        else {
            parent.removeContent();
        }

        final Element compilerElt = new Element(COMPILER);
        compilerElt.setText(compiler == null ? "" : compiler.name());
        parent.addContent(compilerElt);

        final Element mainFileElt = new Element(MAIN_FILE);
        compilerElt.setText(mainFile.getPath());
        parent.addContent(mainFileElt);

        final Element auxDirElt = new Element(AUX_DIR);
        compilerElt.setText(Boolean.toString(auxDir));
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

    public void setMainFile(String mainFilePath) {
        setMainFile(LocalFileSystem.getInstance().findFileByPath(mainFilePath));
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

}
