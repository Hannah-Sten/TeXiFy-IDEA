package nl.rubensten.texifyidea.gutter;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexParameter;
import nl.rubensten.texifyidea.psi.LatexRequiredParam;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Puts a run-configuration icon in the gutter in front of the \begin{document} command.
 *
 * @author Ruben Schellekens
 */
public class LatexCompileGutter extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                            Collection<? super RelatedItemLineMarkerInfo> result) {
        if (!(element instanceof LatexCommands)) {
            return;
        }

        LatexCommands command = (LatexCommands)element;
        PsiElement token = command.getCommandToken();
        List<LatexParameter> params = command.getParameterList();
        if (params.isEmpty()) {
            return;
        }

        // Check for \begin command.
        if (!token.getText().equals("\\begin")) {
            return;
        }

        LatexParameter param = params.get(0);
        LatexRequiredParam requiredParam = param.getRequiredParam();
        if (requiredParam == null) {
            return;
        }

        // Check if the command has 'document' as required argument.
        String paramName = requiredParam.getText();
        if (paramName == null) {
            return;
        }

        if (!paramName.equals("{document}")) {
            return;
        }
        NavigationGutterIconBuilder.create(TexifyIcons.BUILD);

        RelatedItemLineMarkerInfo<PsiElement> info = new RelatedItemLineMarkerInfo<>(
                element,
                TextRange.allOf("Build PDF"),
                TexifyIcons.BUILD,
                0,
                x -> "Build PDF",
                this::buildEvent,
                GutterIconRenderer.Alignment.CENTER,
                Collections.emptyList());
        result.add(info);
    }

    private void buildEvent(MouseEvent event, PsiElement element) {
        if (event.getButton() != MouseEvent.BUTTON1) {
            return;
        }

        Logger.getInstance(getClass()).info("TEXIFY CLICK : " + element + " @ " + element.getClass());
    }

}
