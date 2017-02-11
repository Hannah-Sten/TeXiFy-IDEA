package nl.rubensten.texifyidea.gutter;

import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.psi.PsiElement;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexParameter;
import nl.rubensten.texifyidea.psi.LatexRequiredParam;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

        LatexCommands command = (LatexCommands)element;
        PsiElement token = command.getCommandToken();
        List<LatexParameter> params = command.getParameterList();
        if (params.isEmpty()) {
            return null;
        }

        // Check for \begin command.
        if (!token.getText().equals("\\begin")) {
            return null;
        }

        LatexParameter param = params.get(0);
        LatexRequiredParam requiredParam = param.getRequiredParam();
        if (requiredParam == null) {
            return null;
        }

        // Check if the command has 'document' as required argument.
        String paramName = requiredParam.getText();
        if (paramName == null) {
            return null;
        }

        if (!paramName.equals("{document}")) {
            return null;
        }

        // Lookup actions.
        ActionManager actionManager = ActionManager.getInstance();

        AnAction runnerActions = actionManager.getAction("RunnerActions");
        AnAction chooseRun = actionManager.getAction("ChooseRunConfiguration");
        AnAction editConfigs = actionManager.getAction("editRunConfigurations");

        // Create icon.
        return new RunLineMarkerContributor.Info(TexifyIcons.BUILD, e -> "Compile document",
                runnerActions, chooseRun, editConfigs);
    }

    //    @Override
//    protected void collectNavigationMarkers(@NotNull PsiElement element,
//                                            Collection<? super RelatedItemLineMarkerInfo> result) {
//        if (!(element instanceof LatexCommands)) {
//            return;
//        }
//
//        LatexCommands command = (LatexCommands)element;
//        PsiElement token = command.getCommandToken();
//        List<LatexParameter> params = command.getParameterList();
//        if (params.isEmpty()) {
//            return;
//        }
//
//        // Check for \begin command.
//        if (!token.getText().equals("\\begin")) {
//            return;
//        }
//
//        LatexParameter param = params.get(0);
//        LatexRequiredParam requiredParam = param.getRequiredParam();
//        if (requiredParam == null) {
//            return;
//        }
//
//        // Check if the command has 'document' as required argument.
//        String paramName = requiredParam.getText();
//        if (paramName == null) {
//            return;
//        }
//
//        if (!paramName.equals("{document}")) {
//            return;
//        }
//        NavigationGutterIconBuilder.create(TexifyIcons.BUILD);
//
//        RelatedItemLineMarkerInfo<PsiElement> info = new RelatedItemLineMarkerInfo<>(
//                element,
//                TextRange.allOf("Build PDF"),
//                TexifyIcons.BUILD,
//                0,
//                x -> "Build PDF",
//                this::buildEvent,
//                GutterIconRenderer.Alignment.CENTER,
//                Collections.emptyList());
//        result.add(info);
//    }
//
//    private void buildEvent(MouseEvent event, PsiElement element) {
//        if (event.getButton() != MouseEvent.BUTTON1) {
//            return;
//        }
//
//        Logger.getInstance(getClass()).info("TEXIFY CLICK : " + element + " @ " + element.getClass());
//    }

}
