package nl.rubensten.texifyidea.util;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import nl.rubensten.texifyidea.index.LatexCommandsIndex;
import nl.rubensten.texifyidea.lang.Package;
import nl.rubensten.texifyidea.psi.LatexCommands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Ruben Schellekens
 */
public interface PackageUtils {

    /**
     * Inserts a usepackage statement for the given package in a certain file.
     *
     * @param file
     *         The file to add the usepackage statement to.
     * @param packageName
     *         The name of the package to insert.
     * @param parameters
     *         Parameters to add to the statement, {@code null} or empty string for no parameters.
     */
    static void insertUsepackage(@NotNull Document document, @NotNull PsiFile file,
                                 @NotNull String packageName, @Nullable String parameters) {
        Collection<LatexCommands> commands = LatexCommandsIndex.getIndexCommands(file);

        LatexCommands last = null;
        for (LatexCommands cmd : commands) {
            if ("\\usepackage".equals(cmd.getCommandToken().getText())) {
                last = cmd;
            }
        }

        String newlines = null;
        int insertLocation = -1;

        // When there are no usepackage commands: insert below documentclass.
        if (last == null) {
            Optional<LatexCommands> classHuh = commands.stream()
                    .filter(cmd -> "\\documentclass".equals(cmd.getCommandToken().getText()))
                    .findFirst();
            if (!classHuh.isPresent()) {
                return;
            }

            insertLocation = classHuh.get().getTextOffset() + classHuh.get().getTextLength();
            newlines = "\n\n";
        }
        // Otherwise, insert below the lowest usepackage.
        else {
            insertLocation = last.getTextOffset() + last.getTextLength();
            newlines = "\n";
        }

        String command = newlines + "\\usepackage";
        command += (parameters == null || "".equals(parameters) ? "" : "[" + parameters + "]");
        command += "{" + packageName + "}";

        document.insertString(insertLocation, command);
    }

    /**
     * Inserts a usepackage statement for the given package in a certain file.
     *
     * @param file
     *         The file to add the usepackage statement to.
     * @param pack
     *         The package to include.
     */
    static void insertUsepackage(@NotNull PsiFile file, @NotNull Package pack) {
        if (pack.isDefault()) {
            return;
        }

        Document document = PsiUtilKt.document(file);
        if (document == null) {
            return;
        }

        String[] params = pack.getParameters();
        String parameterString = StringUtil.join(params, ",");
        insertUsepackage(document, file, pack.getName(), parameterString);
    }

    /**
     * Analyses the given file to find all the used packages in the included file set.
     *
     * @return All used package names.
     */
    static Collection<String> getIncludedPackages(@NotNull PsiFile baseFile) {
        Collection<LatexCommands> commands = LatexCommandsIndex.getIndexCommandsInFileSet(baseFile);
        return getIncludedPackages(commands);
    }

    /**
     * Analyses all the given commands and reduces it to a set of all included packages.
     */
    static Collection<String> getIncludedPackages(Collection<LatexCommands> commands) {
        Set<String> packages = new HashSet<>();

        for (LatexCommands cmd : commands) {
            if (!"\\usepackage".equals(cmd.getCommandToken().getText())) {
                continue;
            }

            List<String> list = cmd.getRequiredParameters();
            if (list.isEmpty()) {
                continue;
            }

            String packageName = list.get(0);

            // Multiple includes.
            if (packageName.contains(",")) {
                Collections.addAll(packages, packageName.split(","));
            }
            // Single include.
            else {
                packages.add(packageName);
            }
        }

        return packages;
    }
}
