package nl.rubensten.texifyidea.structure;

import com.intellij.navigation.ItemPresentation;
import nl.rubensten.texifyidea.psi.LatexCommands;

/**
 * @author Ruben Schellekens
 */
public class LatexPresentationFactory {

    public static ItemPresentation getPresentation(LatexCommands commands) {
        switch (commands.getCommandToken().getText()) {
            case "\\section":
                return new LatexSectionPresentation(commands);
            case "\\subsection":
                return new LatexSubSectionPresentation(commands);
            case "\\subsubsection":
                return new LatexSubSubSectionPresentation(commands);
            case "\\paragraph":
                return new LatexParagraphPresentation(commands);
            case "\\subparagraph":
                return new LatexSubParagraphPresentation(commands);
            case "\\newcommand":
                return new LatexNewCommandPresentation(commands);
            case "\\label":
                return new LatexLabelPresentation(commands);
            case "\\include":
                return new LatexIncludePresentation(commands);
            case "\\includeonly":
                return new LatexIncludePresentation(commands);
            case "\\input":
                return new LatexIncludePresentation(commands);
        }

        throw new IllegalArgumentException(
                "Commands " + commands.getCommandToken().getText() + "" + " not supported");
    }

    private LatexPresentationFactory() {
        throw new AssertionError("Nope");
    }

}
