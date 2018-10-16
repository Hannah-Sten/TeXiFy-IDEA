package nl.rubensten.texifyidea.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import nl.rubensten.texifyidea.file.*;
import nl.rubensten.texifyidea.index.BibtexIdIndex;
import nl.rubensten.texifyidea.index.LatexCommandsIndex;
import nl.rubensten.texifyidea.lang.LatexMathCommand;
import nl.rubensten.texifyidea.lang.LatexNoMathCommand;
import nl.rubensten.texifyidea.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ruben Schellekens, Sten Wessel
 */
public class TexifyUtil {

    private TexifyUtil() {
    }

    /**
     * {@link FilesKt#findRelativeFile} but then it scans all content roots.
     *
     * @param original
     *         The file where the relative path starts.
     * @param path
     *         The path relative to {@code original}.
     * @return The found file.
     */
    public static PsiFile scanRoots(@NotNull PsiFile original, @NotNull String path, @Nullable Set<String> extensions) {
        Project project = original.getProject();
        ProjectRootManager rootManager = ProjectRootManager.getInstance(project);
        VirtualFile[] roots = rootManager.getContentSourceRoots();
        VirtualFileManager fileManager = VirtualFileManager.getInstance();

        for (VirtualFile root : roots) {
            Optional<VirtualFile> fileHuh = findFile(root, path, extensions != null ? extensions : Magic.File.includeExtensions);
            if (fileHuh.isPresent()) {
                return FilesKt.psiFile(fileHuh.get(), project);
            }
        }

        return null;
    }

    public static PsiFile getFileRelativeToWithDirectory(@NotNull PsiFile file, @NotNull String path) {
        // Find file
        VirtualFile directory = file.getVirtualFile().getParent();
        Optional<VirtualFile> fileHuh = findFile(directory, path, Magic.File.includeExtensions);
        if (!fileHuh.isPresent()) {
            return null;
        }

        PsiFile psiFile = PsiManager.getInstance(file.getProject()).findFile(fileHuh.get());
        if (psiFile == null || (!LatexFileType.INSTANCE.equals(psiFile.getFileType()) &&
                !StyleFileType.INSTANCE.equals(psiFile.getFileType()))) {
            return null;
        }

        return psiFile;
    }

    /**
     * Looks for a certain file.
     * <p>
     * First looks if the file including extensions exists, when it doesn't it tries to append all
     * possible extensions until it finds a good one.
     *
     * @param directory
     *         The directory where the search is rooted from.
     * @param fileName
     *         The name of the file relative to the directory.
     * @param extensions
     *         Set of all supported extensions to look for.
     * @return The matching file.
     */
    public static Optional<VirtualFile> findFile(VirtualFile directory, String fileName,
                                                 Set<String> extensions) {
        VirtualFile file = directory.findFileByRelativePath(fileName);
        if (file != null) {
            return Optional.of(file);
        }

        for (String extension : extensions) {
            String lookFor = fileName.endsWith("." + extension) ? fileName : fileName + "." + extension;
            file = directory.findFileByRelativePath(lookFor);

            if (file != null) {
                return Optional.of(file);
            }
        }

        return Optional.empty();
    }

    /**
     * Get the FileType instance that corresponds to the given file extension.
     *
     * @param extensionWithoutDot
     *         The file extension to get the corresponding FileType instance of without a dot in
     *         front.
     * @return The corresponding FileType instance.
     */
    public static FileType getFileTypeByExtension(@NotNull String extensionWithoutDot) {

        switch (extensionWithoutDot.toLowerCase()) {
            case "cls":
                return ClassFileType.INSTANCE;
            case "sty":
                return StyleFileType.INSTANCE;
            case "bib":
                return BibtexFileType.INSTANCE;
            case "tikz":
                return TikzFileType.INSTANCE;
            default:
                return LatexFileType.INSTANCE;
        }
    }

    /**
     * Appends an extension to a path only if the given path does not end in that extension.
     *
     * @param path
     *         The path to append the extension to.
     * @param extensionWithoutDot
     *         The extension to append optionally.
     * @return A path ending with the given extension without duplications (e.g. {@code .tex.tex} is
     * impossible}.
     * @throws IllegalArgumentException
     *         When {@code path} or {@code extensionWithoutDot} is {@code null}.
     */
    public static String appendExtension(@NotNull String path, @NotNull String extensionWithoutDot) {

        if (path.toLowerCase().endsWith("." + extensionWithoutDot.toLowerCase())) {
            return path;
        }

        if (path.endsWith(".")) {
            return path + extensionWithoutDot;
        }

        return path + "." + extensionWithoutDot;
    }

    /**
     * Get all commands that are children of the given element.
     */
    public static List<LatexCommands> getAllCommands(PsiElement element) {
        List<LatexCommands> commands = new ArrayList<>();
        getAllCommands(element, commands);
        return commands;
    }

    /**
     * Recursive implementation of {@link TexifyUtil#getAllCommands(PsiElement)}.
     */
    private static void getAllCommands(PsiElement element, List<LatexCommands> commands) {
        for (PsiElement child : element.getChildren()) {
            getAllCommands(child, commands);
        }

        if (element instanceof LatexCommands) {
            commands.add((LatexCommands)element);
        }
    }

    /**
     * Looks up all the required parameters from a given {@link LatexCommands}.
     *
     * @param command
     *         The command to get the required parameters of.
     * @return A list of all required parameters.
     */
    public static List<LatexRequiredParam> getRequiredParameters(LatexCommands command) {
        return command.getParameterList().stream()
                .filter(p -> p.getRequiredParam() != null)
                .map(LatexParameter::getRequiredParam)
                .collect(Collectors.toList());
    }

    /**
     * Looks up all the required parameters from a given {@link LatexCommands}.
     *
     * @param command
     *         The command to get the required parameters of.
     * @return A list of all required parameters.
     */
    public static List<LatexRequiredParam> getRequiredParameters(LatexBeginCommand command) {
        return command.getParameterList().stream()
                .filter(p -> p.getRequiredParam() != null)
                .map(LatexParameter::getRequiredParam)
                .collect(Collectors.toList());
    }

    /**
     * Checks if the given latex command marks a valid entry point for latex compilation.
     * <p>
     * A valid entry point means that a latex compilation can start from the file containing the
     * given command.
     *
     * @param command
     *         The command to check if the file marks a valid entry point.
     * @return {@code true} if the command marks a valid entry point, {@code false} if not.
     */
    public static boolean isEntryPoint(LatexBeginCommand command) {
        // Currently: only allowing '\begin{document}'
        List<LatexRequiredParam> requiredParams = getRequiredParameters(command);
        return requiredParams.size() == 1 && requiredParams.get(0).getText().equals("{document}");
    }

    /**
     * Checks if the given elements contain a valid entry point for latex compilation.
     * <p>
     * A valid entry point means that a latex compilation can start from the file containing the
     * given command.
     *
     * @param elements
     *         The elements to check for a valid entry point.
     * @return {@code true} if a valid entry point is found, {@code false} otherwise.
     */
    public static boolean containsEntryPoint(PsiElement[] elements) {
        for (PsiElement element : elements) {
            if (element instanceof LatexBeginCommand) {
                LatexBeginCommand command = (LatexBeginCommand)element;
                if (TexifyUtil.isEntryPoint(command)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Sends a formatted info message to the IntelliJ logger.
     * <p>
     * All messages start with the prefix "{@code TEXIFY-IDEA - }".
     *
     * @param format
     *         How the log should be formatted, see also {@link String#format(Locale, String,
     *         Object...)}.
     * @param objects
     *         The objects to bind to the format.
     */
    public static void logf(String format, Object... objects) {
        Logger logger = Logger.getInstance(Log.class);
        logger.info("TEXIFY-IDEA - " + String.format(format, objects));
    }

    /**
     * Finds all the defined labels in the fileset of the given file.
     *
     * @param file
     *         The file to get all the labels from.
     * @return A set containing all labels that are defined in the fileset of the given file.
     */
    public static Set<String> findLabelsInFileSet(@NotNull PsiFile file) {
        // LaTeX
        Set<String> labels = LatexCommandsIndex.Companion.getItemsInFileSet(file).stream()
                .filter(cmd -> "\\label".equals(cmd.getName()) || "\\bibitem".equals(cmd.getName()))
                .map(LatexCommands::getRequiredParameters)
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0))
                .collect(Collectors.toSet());

        // BibTeX
        BibtexIdIndex.getIndexedIdsInFileSet(file).stream()
                .map(elt -> StringsKt.substringEnd(elt.getText(), 1))
                .forEach(labels::add);

        return labels;
    }

    /**
     * Finds all defined labels within the project.
     *
     * @param project
     *         Project scope.
     * @return A list of label commands.
     */
    public static Collection<PsiElement> findLabels(@NotNull Project project) {
        Collection<LatexCommands> cmds = LatexCommandsIndex.Companion.getItems(project);
        Collection<BibtexId> bibIds = BibtexIdIndex.getIndexedIds(project);
        List<PsiElement> result = new ArrayList<>(cmds);
        result.addAll(bibIds);
        return findLabels(result);
    }

    /**
     * Finds all defined labels within the fileset of a given file.
     *
     * @param file
     *         The file to analyse the file set of.
     * @return A list of label commands.
     */
    public static Collection<PsiElement> findLabels(@NotNull PsiFile file) {
        Collection<LatexCommands> cmds = LatexCommandsIndex.Companion.getItems(file);
        Collection<BibtexId> bibIds = BibtexIdIndex.getIndexedIds(file);
        List<PsiElement> result = new ArrayList<>(cmds);
        result.addAll(bibIds);
        return findLabels(result);
    }

    /**
     * Finds all the label within the collection of commands.
     *
     * @param cmds
     *         The commands to select all labels from.
     * @return A collection of all label commands.
     */
    public static Collection<PsiElement> findLabels(@NotNull Collection<PsiElement> cmds) {
        cmds.removeIf(cmd -> {
            if (cmd instanceof LatexCommands) {
                String name = ((LatexCommands)cmd).getName();
                return !("\\bibitem".equals(name) || "\\label".equals(name));
            }
            return false;
        });

        return cmds;
    }

    /**
     * Finds all defined labels within the project matching the key.
     *
     * @param project
     *         Project scope.
     * @param key
     *         Key to match the label with.
     * @return A list of matched label commands.
     */
    public static Collection<PsiElement> findLabels(Project project, String key) {
        return findLabels(project).parallelStream()
                .filter(c -> {
                    if (c instanceof LatexCommands) {
                        LatexCommands cmd = (LatexCommands)c;
                        List<String> p = ApplicationManager.getApplication().runReadAction(
                                (Computable<List<String>>)cmd::getRequiredParameters
                        );
                        return p.size() > 0 && key != null && key.equals(p.get(0));
                    }
                    else if (c instanceof BibtexId) {
                        return key != null && key.equals(((BibtexId)c).getName());
                    }

                    return false;
                })
                .collect(Collectors.toList());
    }

    /**
     * Creates a project directory at {@code path} which will be marked as excluded.
     *
     * @param path
     *         The path to create the directory to.
     */
    public static void createExcludedDir(@NotNull String path, @NotNull Module module) {
        new File(path).mkdirs();
        // TODO: actually mark as excluded
    }

    /**
     * Retrieves the file path relative to the root path, or {@code null} if the file is not a child
     * of the root.
     *
     * @param rootPath
     *         The path of the root
     * @param filePath
     *         The path of the file
     * @return The relative path of the file to the root, or {@code null} if the file is no child of
     * the root.
     */
    @Nullable
    public static String getPathRelativeTo(@NotNull String rootPath, @NotNull String filePath) {
        if (!filePath.startsWith(rootPath)) {
            return null;
        }
        return filePath.substring(rootPath.length());
    }

    /**
     * Returns the forced first required parameter of a command as a command.
     * <p>
     * This allows both example constructs {@code \\usepackage{\\foo}} and {@code
     * \\usepackage\\foo}, which are equivalent. Note that when the command does not take parameters
     * this method might return untrue results.
     *
     * @param command
     *         The command to get the parameter for.
     * @return The forced first required parameter of the command.
     */
    public static LatexCommands getForcedFirstRequiredParameterAsCommand(LatexCommands command) {
        List<LatexRequiredParam> params = getRequiredParameters(command);
        if (params.size() > 0) {
            LatexRequiredParam param = params.get(0);
            Collection<LatexCommands> found = PsiTreeUtil.findChildrenOfType(param, LatexCommands.class);
            if (found.size() == 1) {
                return (LatexCommands)(found.toArray()[0]);
            }
            else {
                return null;
            }
        }

        LatexContent sibling = PsiTreeUtil.getNextSiblingOfType(PsiTreeUtil.getParentOfType(command, LatexContent.class), LatexContent.class);
        return PsiTreeUtil.findChildOfType(sibling, LatexCommands.class);
    }

    /**
     * Checks whether the command is known by TeXiFy.
     *
     * @param command
     *         The command to check.
     * @return Whether the command is known.
     */
    public static boolean isCommandKnown(LatexCommands command) {
        String commandName = Optional.ofNullable(command.getName()).map(cmd -> cmd.substring(1)).orElse("");
        return LatexNoMathCommand.get(commandName) != null || LatexMathCommand.get(commandName) != null;
    }

    /**
     * Little class to make the log messages look awesome :3
     *
     * @author Ruben Schellekens
     */
    private class Log {

    }

}
