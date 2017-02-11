package nl.rubensten.texifyidea.documentation;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import nl.rubensten.texifyidea.TexifyUtil;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexNoMathContent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs {@code texdoc} to obtain package/class documentation.
 *
 * @author Sten Wessel
 */
public class LatexPackageDocumentationProvider extends AbstractDocumentationProvider {

    @Override
    public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
        if (!(originalElement instanceof LatexNoMathContent)) {
            return null;
        }

        LatexCommands command = (LatexCommands)PsiTreeUtil.findFirstParent(originalElement, (elt) -> elt instanceof LatexCommands);

        if (command == null) {
            return null;
        }

        if (!command.getCommandToken().getText().equals("\\usepackage")) {
            return null;
        }

        if (!TexifyUtil.getRequiredParameters(command).get(0).getGroup().getContentList().get(0).isEquivalentTo(originalElement)) {
            return null;
        }

        List<String> lookupCommand = new ArrayList<>();
        lookupCommand.add("texdoc");
        lookupCommand.add("-l");
        lookupCommand.add(originalElement.getText());

        GeneralCommandLine commandLine = new GeneralCommandLine(lookupCommand);
        Process process;

        List<String> result = new ArrayList<>();
        try {
            process = commandLine.createProcess();
            process.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // Get the first line and see if documentation could be found.
            String line = br.readLine();
            if (line != null && line.endsWith("could not be found.")) {
                return null;
            }

            while ((line = br.readLine()) != null) {
                result.add(line);
            }
        }
        catch (ExecutionException | InterruptedException | IOException e) {
            return null;
        }

        return result;
    }
}
