package nl.rubensten.texifyidea.structure.latex;

import com.intellij.navigation.ItemPresentation;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.psi.LatexCommands;

/**
 * @author Ruben Schellekens
 */
public class LatexPresentationFactory {

    public static ItemPresentation getPresentation(LatexCommands commands) {
        switch (commands.getCommandToken().getText()) {
            case "\\part":
                return new LatexPartPresentation(commands);
            case "\\chapter":
                return new LatexChapterPresentation(commands);
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
            case "\\DeclareMathOperator":
                return new LatexNewCommandPresentation(commands);
            case "\\label":
                return new LatexLabelPresentation(commands);
            case "\\bibitem":
                return new BibitemPresentation(commands);
            case "\\include":
                return new LatexIncludePresentation(commands);
            case "\\includeonly":
                return new LatexIncludePresentation(commands);
            case "\\input":
                return new LatexIncludePresentation(commands);
            default:
                return new LatexOtherCommandPresentation(commands, TexifyIcons.DOT_COMMAND);
        }
    }

    private LatexPresentationFactory() {
        throw new AssertionError("Nope");
    }

}
