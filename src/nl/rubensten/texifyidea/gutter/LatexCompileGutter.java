package nl.rubensten.texifyidea.gutter;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.psi.PsiElement;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.util.TexifyUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Puts a run-configuration icon in the gutter in front of the \begin{document} command.
 *
 * @author Ruben Schellekens
 */
public class LatexCompileGutter extends RunLineMarkerContributor {

    @Nullable
    @Override
    public Info getInfo(PsiElement element) {
        if (!(element instanceof LatexCommands)) {
            return null;
        }

        // Break when not a valid command: don't show icon.
        LatexCommands command = (LatexCommands)element;
        if (!TexifyUtil.isEntryPoint(command)) {
            return null;
        }

        // Lookup actions.
        ActionManager actionManager = ActionManager.getInstance();
        AnAction editConfigs = actionManager.getAction("editRunConfigurations");
        AnAction[] actions = ExecutorAction.getActions(0);

        // Create icon.
        return new RunLineMarkerContributor.Info(TexifyIcons.BUILD, e -> "Compile document",
                actions[0], editConfigs);
    }

}
