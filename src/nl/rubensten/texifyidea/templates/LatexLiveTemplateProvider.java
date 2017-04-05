package nl.rubensten.texifyidea.templates;

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider;
import nl.rubensten.texifyidea.util.Constants;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sten Wessel
 */
public class LatexLiveTemplateProvider implements DefaultLiveTemplatesProvider {

    @Override
    public String[] getDefaultLiveTemplateFiles() {
        return Constants.EMPTY_STRING_ARRAY;
    }

    @Nullable
    @Override
    public String[] getHiddenLiveTemplateFiles() {
        return new String[] {
                "liveTemplates/hidden/LaTeX"
        };
    }
}
