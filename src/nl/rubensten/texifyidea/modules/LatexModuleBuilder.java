package nl.rubensten.texifyidea.modules;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import nl.rubensten.texifyidea.TeXception;
import nl.rubensten.texifyidea.templates.LatexTemplatesFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ruben Schellekens, Sten Wessel
 */
public class LatexModuleBuilder extends ModuleBuilder {

    @Override
    public void setupRootModel(ModifiableRootModel rootModel) throws ConfigurationException {
        final Project project = rootModel.getProject();
        final LocalFileSystem fileSystem = LocalFileSystem.getInstance();
        final CompilerModuleExtension compilerModuleExtension = rootModel.getModuleExtension(CompilerModuleExtension.class);
        compilerModuleExtension.setExcludeOutput(true);

        ContentEntry contentEntry = doAddContentEntry(rootModel);
        if (contentEntry == null) {
            return;
        }

        final List<Pair<String, String>> sourcePaths = getSourcePaths();
        for (final Pair<String, String> sourcePath : sourcePaths) {
            String path = sourcePath.first;
            new File(path).mkdirs();
            final VirtualFile sourceRoot = LocalFileSystem.getInstance().refreshAndFindFileByPath(FileUtil.toSystemIndependentName(path));

            if (sourceRoot != null) {
                contentEntry.addSourceFolder(sourceRoot, false, sourcePath.second);
                addMainFile(project, path);
            }
        }

        // Create source directory.
        for (Pair<String, String> sourcePath : getSourcePaths()) {
            String path = sourcePath.first;
            new File(path).mkdirs();

            final VirtualFile sourceRoot = fileSystem.refreshAndFindFileByPath(FileUtil.toSystemIndependentName(path));
            if (sourceRoot == null) {
                continue;
            }

            contentEntry.addSourceFolder(sourceRoot, false, sourcePath.second);
            addMainFile(project, path);
        }

        // Create output directory.
        String path = getContentEntryPath() + File.separator + "out";
        new File(path).mkdirs();
        final VirtualFile outRoot = fileSystem.refreshAndFindFileByPath(FileUtil.toSystemIndependentName(path));
        if (outRoot != null) {
            contentEntry.addExcludeFolder(outRoot);
        }

        // Create auxiliary directory.
        path = getContentEntryPath() + File.separator + "auxil";
        new File(path).mkdirs();
        final VirtualFile auxRoot = fileSystem.refreshAndFindFileByPath(FileUtil.toSystemIndependentName(path));
        if (auxRoot != null) {
            contentEntry.addExcludeFolder(auxRoot);
        }
    }

    /**
     * Creates the main.tex file and applies the default file template.
     *
     * @param project
     *         The project to add the file to.
     * @param path
     *         The directory path of the file (no seperator and no file name).
     */
    private void addMainFile(Project project, String path) {
        final String mainFilePath = path + File.separator + "main.tex";
        final File mainFile = new File(mainFilePath);

        // Create main file.
        try {
            mainFile.createNewFile();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new TeXception("Problem with creating main .tex file.", e);
        }

        // Apply template.
        final String template = LatexTemplatesFactory.FILE_TEMPLATE_TEX;
        final String templateText = LatexTemplatesFactory.getTemplateText(project, template);

        try (OutputStream outputStream = new FileOutputStream(mainFile)) {
            outputStream.write(templateText.getBytes());
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new TeXception("Could not apply .tex template to main file.", e);
        }
    }

    private List<Pair<String, String>> getSourcePaths() {
        final List<Pair<String, String>> paths = new ArrayList<>();
        final String path = getContentEntryPath() + File.separator + "src";
        new File(path).mkdirs();
        paths.add(Pair.create(path, ""));

        return paths;
    }

    @Override
    public ModuleType getModuleType() {
        return LatexModuleType.getInstance();
    }
}
