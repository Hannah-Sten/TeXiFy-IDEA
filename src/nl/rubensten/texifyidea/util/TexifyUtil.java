package nl.rubensten.texifyidea.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import nl.rubensten.texifyidea.file.ClassFileType;
import nl.rubensten.texifyidea.file.LatexFileType;
import nl.rubensten.texifyidea.file.StyleFileType;
import nl.rubensten.texifyidea.index.LatexCommandsIndex;
import nl.rubensten.texifyidea.psi.LatexBeginCommand;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexContent;
import nl.rubensten.texifyidea.psi.LatexParameter;
import nl.rubensten.texifyidea.psi.LatexRequiredParam;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author Ruben Schellekens, Sten Wessel
 */
public class TexifyUtil {

    private TexifyUtil() {
    }

    // Roman numerals.
    private static final TreeMap<Integer, String> ROMAN = new TreeMap<>();
    static {
        ROMAN.put(1000, "M");
        ROMAN.put(500, "D");
        ROMAN.put(100, "C");
        ROMAN.put(50, "L");
        ROMAN.put(10, "X");
        ROMAN.put(5, "V");
        ROMAN.put(1, "I");
    }

    // Referenced files.
    private static final List<String> INCLUDE_COMMANDS = Arrays.asList("\\includeonly", "\\include", "\\input");
    private static final Set<String> INCLUDE_EXTENSIONS = new HashSet<>();
    static {
        Collections.addAll(INCLUDE_EXTENSIONS, "tex", "sty", "cls");
    }

    /**
     * Scans the whole document (recursively) for all referenced/included files.
     *
     * @return A collection containing all the PsiFiles that are referenced from {@code psiFile}.
     */
    @NotNull
    public static Collection<PsiFile> getReferencedFiles(@NotNull PsiFile psiFile) {
        Set<PsiFile> result = new HashSet<>();
        getReferencedFiles(psiFile, result);
        return result;
    }

    /**
     * Recursive implementation of {@link TexifyUtil#getReferencedFiles(PsiFile)}.
     */
    private static void getReferencedFiles(@NotNull PsiFile file, @NotNull Collection<PsiFile> files) {
        GlobalSearchScope scope = GlobalSearchScope.fileScope(file);
        Collection<LatexCommands> commands = LatexCommandsIndex.getIndexedCommands(file.getProject(), scope);

        for (LatexCommands command : commands) {
            if (!INCLUDE_COMMANDS.contains(command.getCommandToken().getText())) {
                continue;
            }

            List<String> required = command.getRequiredParameters();
            if (required.isEmpty()) {
                continue;
            }

            String fileName = required.get(0);
            PsiFile included = getFileRelativeTo(file, fileName);

            if (files.contains(included)) {
                continue;
            }

            files.add(included);
            getReferencedFiles(included, files);
        }
    }

    /**
     * Looks up a file relative to the given {@code file}.
     *
     * @param file
     *         The file where the relative path starts.
     * @param path
     *         The path relative to {@code file}.
     * @return The found file.
     */
    @Nullable
    public static PsiFile getFileRelativeTo(@NotNull PsiFile file, @NotNull String path) {
        // Find file
        VirtualFile directory = file.getVirtualFile().getParent();
        Optional<VirtualFile> fileHuh = findFile(directory, path, INCLUDE_EXTENSIONS);
        if (!fileHuh.isPresent()) {
            return null;
        }

        PsiFile psiFile = PsiManager.getInstance(file.getProject()).findFile(fileHuh.get());
        if (!LatexFileType.INSTANCE.equals(psiFile.getFileType()) &&
                !StyleFileType.INSTANCE.equals(psiFile.getFileType())) {
            return null;
        }

        return psiFile;
    }

    /**
     * Looks for the next command relative to the given command.
     *
     * @param commands
     *         The command to start looking from.
     * @return The next command in the file, or {@code null} when there is no such command.
     */
    @Nullable
    public static LatexCommands getNextCommand(@NotNull LatexCommands commands) {
        LatexContent content = (LatexContent)commands.getParent().getParent();
        PsiElement nextPsi = content.getNextSibling();
        if (!(nextPsi instanceof LatexContent)) {
            return null;
        }

        LatexContent siblingContent = (LatexContent)nextPsi;
        LatexCommands childCommand = PsiTreeUtil.findChildOfType(siblingContent, LatexCommands.class);
        if (childCommand == null) {
            return null;
        }

        return childCommand;
    }

    /**
     * Turns a given integer into a roman numeral.
     *
     * @param integer
     *         The (positive) integer to convert to roman.
     * @return The roman representation of said integer.
     * @throws IllegalArgumentException
     *         When the integer is smaller or equal to 0.
     */
    public static String toRoman(int integer) throws IllegalArgumentException {
        if (integer <= 0) {
            throw new IllegalArgumentException("Integer must be positive!");
        }

        Integer fromMap = ROMAN.floorKey(integer);
        if (integer == fromMap) {
            return ROMAN.get(integer);
        }

        return ROMAN.get(fromMap) + toRoman(integer - fromMap);
    }

    /**
     * Looks for a certain file.
     * <p>
     * First looks if the file including extensions exists, when it doesn't it tries to append
     * all possible extensions until it finds a good one.
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
            file = directory.findFileByRelativePath(fileName + "." + extension);

            if (file != null) {
                return Optional.of(file);
            }
        }

        return Optional.empty();
    }

    /**
     * Repeats the given string a given amount of times.
     *
     * @param string
     *         The string to repeat.
     * @param count
     *         The amount of times to repeat the string.
     * @return A string where {@code string} has been repeated {@code count} times.
     */
    public static String fill(String string, int count) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < count; i++) {
            sb.append(string);
        }

        return sb.toString();
    }

    /**
     * Get the FileType instance that corresponds to the given file extension.
     *
     * @param extensionWithoutDot
     *         The file extension to get the corresponding FileType instance of without a dot in
     *         front.
     * @return The corresponding FileType instance.
     * @throws IllegalArgumentException
     *         When {@code extensionWithoutDot} is {@code null}.
     */
    public static FileType getFileTypeByExtension(@NotNull String extensionWithoutDot)
            throws IllegalArgumentException {
        if (extensionWithoutDot == null) {
            throw new IllegalArgumentException("extensionWithoutDot cannot be null");
        }

        switch (extensionWithoutDot.toLowerCase()) {
            case "cls":
                return ClassFileType.INSTANCE;
            case "sty":
                return StyleFileType.INSTANCE;
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
    public static String appendExtension(@NotNull String path, @NotNull String extensionWithoutDot)
            throws IllegalArgumentException {
        if (path == null) {
            throw new IllegalArgumentException("path cannot be null");
        }

        if (extensionWithoutDot == null) {
            throw new IllegalArgumentException("extensionWithoutDot cannot be null");
        }

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
        if (requiredParams.size() != 1) {
            return false;
        }

        return requiredParams.get(0).getText().equals("{document}");
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
     * Creates a project directory at {@code path} which will be marked as excluded.
     *
     * @param path
     *         The path to create the directory to.
     */
    public static void createExcludedDir(@NotNull String path, @NotNull Module module) {
        if (new File(path).mkdirs()) {
            VirtualFile root = LocalFileSystem.getInstance().refreshAndFindFileByPath(FileUtil.toSystemIndependentName(path));
            if (root != null) {
                ModuleRootManager.getInstance(module).getModifiableModel().addContentEntry(root)
                        .addExcludeFolder(root);
            }
        }
    }

    /**
     * Retrieves the file path relative to the root path, or {@code null} if the file is not a
     * child of the root.
     *
     * @param rootPath
     *         The path of the root
     * @param filePath
     *         The path of the file
     * @return The relative path of the file to the root, or {@code null} if the file is no child
     * of the root.
     */
    @Nullable
    public static String getPathRelativeTo(@NotNull String rootPath, @NotNull String filePath) {
        if (!filePath.startsWith(rootPath)) {
            return null;
        }
        return filePath.substring(rootPath.length());
    }

    /**
     * Little class to make the log messages look awesome :3
     *
     * @author Ruben Schellekens
     */
    private class Log {

    }

}
