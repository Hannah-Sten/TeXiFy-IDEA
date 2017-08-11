package nl.rubensten.texifyidea.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.KillableProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import nl.rubensten.texifyidea.util.TexifyUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
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

        // Create out directories for included files' aux
        createOutDirs();

        GeneralCommandLine cmdLine = new GeneralCommandLine(command).withWorkDirectory(mainFile.getParent().getPath());

        ProcessHandler handler = new KillableProcessHandler(cmdLine);
        ProcessTerminatedListener.attach(handler, getEnvironment().getProject());

        return handler;
    }

    private void createOutDirs() throws ExecutionException {
        VirtualFile mainFile = runConfig.getMainFile();
        ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();

        String outPath = fileIndex.getContentRootForFile(mainFile).getPath() + "/out";
        TexifyUtil.createExcludedDir(outPath, fileIndex.getModuleForFile(mainFile));

        Collection<PsiFile> files;
        try {
            files = TexifyUtil.getReferencedFiles(PsiManager.getInstance(project).findFile(mainFile));
        }
        catch (IndexNotReadyException e) {
            throw new ExecutionException("Please wait until the indices are built.");
        }

        VirtualFile root = fileIndex.getSourceRootForFile(mainFile);
        for (PsiFile included : files) {
            String relPath = TexifyUtil.getPathRelativeTo(root.getPath(), included.getContainingDirectory().getVirtualFile().getPath());
            new File(outPath + relPath).mkdirs();
        }
    }
}
