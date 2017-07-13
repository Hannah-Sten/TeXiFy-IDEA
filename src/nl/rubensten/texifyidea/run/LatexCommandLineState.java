package nl.rubensten.texifyidea.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import nl.rubensten.texifyidea.util.TexifyUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
        List<String> command = compiler.getCommand(runConfig, project);

        // Create out directories for included files' aux
        createOutDirs();

        GeneralCommandLine cmdLine = new GeneralCommandLine(command).withWorkDirectory(runConfig.getMainFile().getParent().getPath());
        return new OSProcessHandler(cmdLine);
    }

    private void createOutDirs() throws ExecutionException {
        Module module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(runConfig.getMainFile());
        if (module == null) {
            throw new ExecutionException("TeX file is not within a LaTeX module.");
        }
        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        if (moduleRootManager.getContentRoots().length == 0) {
            throw new ExecutionException("Module is improperly configured (no content roots available).");
        }

        String outPath = moduleRootManager.getContentRoots()[0].getPath() + "/out";
        TexifyUtil.createExcludedDir(outPath, module);

        Collection<PsiFile> files = TexifyUtil.getReferencedFiles(PsiManager.getInstance(project).findFile(runConfig.getMainFile()));
        VirtualFile[] roots = moduleRootManager.getSourceRoots();
        for (PsiFile included : files) {
            String relPath = Arrays.stream(roots)
                    .map(r -> TexifyUtil.getPathRelativeTo(r.getPath(), included.getContainingDirectory().getVirtualFile().getPath()))
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingInt(String::length))
                    .findFirst().orElse("");

            new File(outPath + relPath).mkdirs();
        }
    }
}
