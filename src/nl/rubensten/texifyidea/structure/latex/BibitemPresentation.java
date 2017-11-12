package nl.rubensten.texifyidea.structure.latex;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.psi.LatexCommands;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * @author Ruben Schellekens
 */
public class BibitemPresentation implements ItemPresentation {

    private final String bibitemName;
    private final String locationString;

    public BibitemPresentation(LatexCommands labelCommand) {
        if (!labelCommand.getCommandToken().getText().equals("\\bibitem")) {
            throw new IllegalArgumentException("command is no \\bibitem-command");
        }

        // Get label name.
        List<String> required = labelCommand.getRequiredParameters();
        if (required.isEmpty()) {
            throw new IllegalArgumentException("\\bibitem has no label name");
        }
        this.bibitemName = required.get(0);

        // Location string.
        FileDocumentManager manager = FileDocumentManager.getInstance();
        Document document = manager.getDocument(labelCommand.getContainingFile().getVirtualFile());
        int line = document.getLineNumber(labelCommand.getTextOffset()) + 1;
        this.locationString = labelCommand.getContainingFile().getName() + ":" + line;
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return bibitemName;
    }

    @Nullable
    @Override
    public String getLocationString() {
        return locationString;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return TexifyIcons.DOT_BIB;
    }
}
