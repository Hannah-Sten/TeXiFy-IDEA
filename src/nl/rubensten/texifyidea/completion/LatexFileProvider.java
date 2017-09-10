package nl.rubensten.texifyidea.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ProcessingContext;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.completion.handlers.CompositeHandler;
import nl.rubensten.texifyidea.completion.handlers.FileNameInsertionHandler;
import nl.rubensten.texifyidea.completion.handlers.LatexReferenceInsertHandler;
import nl.rubensten.texifyidea.util.Kindness;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Ruben Schellekens
 */
public class LatexFileProvider extends CompletionProvider<CompletionParameters> {

    private static final Pattern TRIM_SLASH = Pattern.compile("/[^/]*$");
    private static final Pattern TRIM_BACK = Pattern.compile("\\.\\./");

    LatexFileProvider() {
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  ProcessingContext context, @NotNull CompletionResultSet result) {
        // Get base data.
        VirtualFile baseFile = parameters.getOriginalFile().getVirtualFile();
        VirtualFile baseDirectory = baseFile.getParent();
        String autocompleteText = processAutocompleteText(parameters.getOriginalPosition().getText());

        String directoryPath = baseDirectory.getPath() + "/" + autocompleteText;
        VirtualFile searchDirectory = getByPath(directoryPath);

        if (searchDirectory == null) {
            autocompleteText = trimAutocompleteText(autocompleteText);
            if (autocompleteText.length() == 0) {
                searchDirectory = baseDirectory;
            }
            else {
                searchDirectory = getByPath(baseDirectory.getPath() + "/" + autocompleteText);
            }
        }

        if (autocompleteText.length() > 0 && !autocompleteText.endsWith("/")) {
            return;
        }

        // Find stuff.
        List<VirtualFile> directories = getContents(searchDirectory, true);
        List<VirtualFile> files = getContents(searchDirectory, false);

        // Add directories.
        for (VirtualFile directory : directories) {
            String directoryName = directory.getPresentableName();
            result.addElement(
                    LookupElementBuilder.create(noBack(autocompleteText) + directory.getName())
                            .withPresentableText(directoryName)
                            .withIcon(PlatformIcons.PACKAGE_ICON)
            );
        }

        // Add return directory.
        result.addElement(
                LookupElementBuilder.create("..")
                        .withIcon(PlatformIcons.PACKAGE_ICON)
        );

        // Add files.
        for (VirtualFile file : files) {
            String fileName = file.getPresentableName();
            Icon icon = TexifyIcons.getIconFromExtension(file.getExtension());
            result.addElement(
                    LookupElementBuilder.create(noBack(autocompleteText) + file.getName())
                            .withPresentableText(fileName)
                            .withInsertHandler(new CompositeHandler<>(
                                    new LatexReferenceInsertHandler(),
                                    new FileNameInsertionHandler()
                            ))
                            .withIcon(icon)
            );
        }

        result.addLookupAdvertisement(Kindness.getKindWords());
    }

    private String noBack(String stuff) {
        return TRIM_BACK.matcher(stuff).replaceAll("");
    }

    private String trimAutocompleteText(String autoCompleteText) {
        if (!autoCompleteText.contains("/")) {
            return "";
        }

        return TRIM_SLASH.matcher(autoCompleteText).replaceAll("/");
    }

    private String processAutocompleteText(String autocompleteText) {
        String result = autocompleteText.endsWith("}") ?
                autocompleteText.substring(0, autocompleteText.length() - 1) :
                autocompleteText;

        if (result.endsWith(".")) {
            result = result.substring(0, result.length() - 1) + "/";
        }

        return result;
    }

    private VirtualFile getByPath(String path) {
        LocalFileSystem fileSystem = LocalFileSystem.getInstance();
        return fileSystem.findFileByPath(path);
    }

    private List<VirtualFile> getContents(VirtualFile base, boolean directory) {
        List<VirtualFile> contents = new ArrayList<>();

        if (base == null) {
            return contents;
        }

        for (VirtualFile file : base.getChildren()) {
            if (file.isDirectory() == directory) {
                contents.add(file);
            }
        }

        return contents;
    }
}
