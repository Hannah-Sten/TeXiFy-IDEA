package nl.rubensten.texifyidea.run;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import nl.rubensten.texifyidea.util.PlatformType;
import nl.rubensten.texifyidea.util.PlatformUtilKt;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ruben Schellekens, Sten Wessel
 */
public enum LatexCompiler {

    PDFLATEX("pdfLaTeX", "pdflatex"),
    LUALATEX("LuaLaTeX", "lualatex");

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
            command = createPdflatexCommand(runConfig, moduleRoot, moduleRoots);
        }
        else if (this == LUALATEX) {
            command = createLualatexCommand(runConfig, moduleRoot);
        }

        // Custom compiler arguments specified by the user
        if (runConfig.getCompilerArguments() != null) {
            String[] args = runConfig.getCompilerArguments().split("\\s+");
            command.addAll(Arrays.asList(args));
        }

        command.add(mainFile.getName());

        return command;
    }

    /**
     * Create the command to execute lualatex.
     *
     * @param runConfig LaTeX run configuration which initiated the action of creating this command.
     * @param moduleRoot Module root.
     *
     * @return The command to be executed.
     */
    private List<String> createLualatexCommand(LatexRunConfiguration runConfig, VirtualFile moduleRoot) {
        List<String> command = new ArrayList<>();

        if (runConfig.getCompilerPath() != null) {
            command.add(runConfig.getCompilerPath());
        } else {
            command.add("lualatex");
        }

        // Some commands are the same as for pdflatex
        command.add("-file-line-error");
        command.add("-interaction=nonstopmode");
        command.add("-synctex=1");
        command.add("-output-format=" + runConfig.getOutputFormat().name().toLowerCase());

        if (runConfig.hasOutputDirectories() && (PlatformUtilKt.getPlatformType() == PlatformType.WINDOWS)) {
            command.add("-output-directory=" + moduleRoot.getPath() + "/out");
        }

        // Note that lualatex has no -aux-directory
        return command;
    }

    /**
     * Create the command to execute pdflatex.
     *
     * @param runConfig LaTeX run configuration which initiated the action of creating this command.
     * @param moduleRoot Module root.
     * @param moduleRoots List of source roots.
     *
     * @return The command to be executed.
     */
    private List<String> createPdflatexCommand(LatexRunConfiguration runConfig, VirtualFile moduleRoot, VirtualFile[] moduleRoots) {
        List<String> command = new ArrayList<>();

        if (runConfig.getCompilerPath() != null) {
            command.add(runConfig.getCompilerPath());
        } else {
            command.add("pdflatex");
        }
        command.add("-file-line-error");
        command.add("-interaction=nonstopmode");
        command.add("-synctex=1");
        command.add("-output-format=" + runConfig.getOutputFormat().name().toLowerCase());

        if (runConfig.hasOutputDirectories() && (PlatformUtilKt.getPlatformType() == PlatformType.WINDOWS)) {
            command.add("-output-directory=" + moduleRoot.getPath() + "/out");
        }

        if (runConfig.hasAuxiliaryDirectories() &&(PlatformUtilKt.getPlatformType() == PlatformType.WINDOWS)) {
            command.add("-aux-directory=" + moduleRoot.getPath() + "/auxil");
        }

        if ((PlatformUtilKt.getPlatformType() == PlatformType.WINDOWS)) {
            for (VirtualFile root : moduleRoots) {
                command.add("-include-directory=" + root.getPath());
            }
        }

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
