package nl.rubensten.texifyidea.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Sten Wessel
 */
public class LatexCommandLineState extends CommandLineState {

    private final Project project;
    private LatexRunConfiguration runConfig;

    protected LatexCommandLineState(ExecutionEnvironment environment, LatexRunConfiguration runConfig) {
        super(environment);
        this.runConfig = runConfig;
        this.project = environment.getProject();
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        LatexCompiler compiler = runConfig.getCompiler();
        if (compiler == null) {
            throw new ExecutionException("Run configuration is invalid.");
        }

        List<String> command = compiler.getCommand(runConfig, project);
        VirtualFile mainFile = runConfig.getMainFile();

        if (mainFile == null || command == null) {
            throw new ExecutionException("Run configuration is invalid.");
        }

        GeneralCommandLine cmdLine = new GeneralCommandLine(command).withWorkDirectory(mainFile.getParent().getPath());

        return new OSProcessHandler(cmdLine);
    }
}
