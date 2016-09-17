package nl.rubensten.texifyidea.modules;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sten Wessel
 */
public class LatexModuleBuilder extends ModuleBuilder {

    @Override
    public void setupRootModel(ModifiableRootModel rootModel) throws ConfigurationException {
        final CompilerModuleExtension compilerModuleExtension = rootModel.getModuleExtension(CompilerModuleExtension.class);
        compilerModuleExtension.setExcludeOutput(true);

        ContentEntry contentEntry = doAddContentEntry(rootModel);
        if (contentEntry != null) {
            final List<Pair<String, String>> sourcePaths = getSourcePaths();

            for (final Pair<String, String> sourcePath : sourcePaths) {
                String path = sourcePath.first;
                new File(path).mkdirs();
                final VirtualFile sourceRoot = LocalFileSystem.getInstance().refreshAndFindFileByPath(FileUtil.toSystemIndependentName(path));

                if (sourceRoot != null) {
                    contentEntry.addSourceFolder(sourceRoot, false, sourcePath.second);

                    // Add main file
                    String mainFilePath = path + File.separator + "main.tex";
                    try {
                        new File(mainFilePath).createNewFile();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            String path = getContentEntryPath() + File.separator + "out";
            new File(path).mkdirs();
            final VirtualFile outRoot = LocalFileSystem.getInstance().refreshAndFindFileByPath(FileUtil.toSystemIndependentName(path));
            if (outRoot != null) {
                contentEntry.addExcludeFolder(outRoot);
            }

            path = getContentEntryPath() + File.separator + "auxil";
            new File(path).mkdirs();
            final VirtualFile auxRoot = LocalFileSystem.getInstance().refreshAndFindFileByPath(FileUtil.toSystemIndependentName(path));
            if (auxRoot != null) {
                contentEntry.addExcludeFolder(auxRoot);
            }
        }
    }

    @Override
    public ModuleType getModuleType() {
        return LatexModuleType.getInstance();
    }

    private List<Pair<String, String>> getSourcePaths() {
        final List<Pair<String, String>> paths = new ArrayList<>();
        final String path = getContentEntryPath() + File.separator + "src";
        new File(path).mkdirs();
        paths.add(Pair.create(path, ""));

        return paths;
    }
}
