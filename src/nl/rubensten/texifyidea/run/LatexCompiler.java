package nl.rubensten.texifyidea.run;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sten Wessel
 */
public enum LatexCompiler {

    PDFLATEX("pdfLaTeX", "pdflatex");

    private String displayName;
    private String executableName;

    LatexCompiler(String displayName, String executableName) {
        this.displayName = displayName;
        this.executableName = executableName;
    }

    @Nullable
    public List<String> getCommand(LatexRunConfiguration runConfig, Project project) {
        List<String> command = new ArrayList<>();

        ProjectRootManager rootManager = ProjectRootManager.getInstance(project);
        ProjectFileIndex fileIndex = rootManager.getFileIndex();
        VirtualFile mainFile = runConfig.getMainFile();
        VirtualFile moduleRoot = fileIndex.getContentRootForFile(runConfig.getMainFile());
        VirtualFile[] moduleRoots = rootManager.getContentSourceRoots();
        if (moduleRoot == null || mainFile == null) {
            return null;
        }

        if (this == PDFLATEX) {
            if (runConfig.getCompilerPath() != null) {
                command.add(runConfig.getCompilerPath());
            } else {
                command.add("pdflatex");
            }
            command.add("-file-line-error");
            command.add("-interaction=nonstopmode");
            command.add("-synctex=1");
            command.add("-output-format=" + runConfig.getOutputFormat().name().toLowerCase());
            command.add("-output-directory=" + moduleRoot.getPath() + "/out");

            if (runConfig.hasAuxiliaryDirectories()) {
                command.add("-aux-directory=" + moduleRoot.getPath() + "/auxil");
            }

            for (VirtualFile root : moduleRoots) {
                command.add("-include-directory=" + root.getPath());
            }
        }

        command.add(mainFile.getName());

        return command;
    }

    public String getExecutableName() {
        return executableName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }

    /**
     * @author Ruben Schellekens
     */
    public enum Format {

        PDF,
        DVI;

        @Nullable
        public static Format byNameIgnoreCase(@Nullable String name) {
            for (Format format : values()) {
                if (format.name().equalsIgnoreCase(name)) {
                    return format;
                }
            }

            return null;
        }
    }
}
