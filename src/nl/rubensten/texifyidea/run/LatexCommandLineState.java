package nl.rubensten.texifyidea.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

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
        GeneralCommandLine cmdLine = new GeneralCommandLine(runConfig.getCompiler().getCommand(runConfig, project));

        return new OSProcessHandler(cmdLine);
    }
}
