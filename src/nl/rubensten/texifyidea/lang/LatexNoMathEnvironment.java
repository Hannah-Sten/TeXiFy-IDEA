package nl.rubensten.texifyidea.lang;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sten Wessel
 */
public enum LatexNoMathEnvironment {
    /*
        Default LaTeX
     */
    ABSTRACT("abstract"),
    ALLTT("alltt"),
    CENTER("center"),
    DESCRIPTION("description", "\\item"),
    DISPLAYMATH("displaymath"),
    DOCUMENT("document"),
    ENUMERATE("enumerate", "\\item "),
    EQUATION("equation"),
    EQNARRAY("eqnarray"),
    FIGURE("figure", new OptionalArgument("placement")),
    FIGURE_STAR("figure*", new OptionalArgument("placement")),
    FILECONTENTS("filecontents"),
    FILECONTENTS_STAR("filecontents*"),
    FLUSHLEFT("flushleft"),
    FLUSHRIGHT("flushright"),
    FOOTNOTESIZE("footnotesize"),
    HUGE("huge"),
    CAPITAL_HUGE("Huge"),
    ITEMIZE("itemize", "\\item "),
    LARGE("large"),
    CAPITAL_LARGE("Large"),
    SCREAMING_LARGE("LARGE"),
    LIST("list", new RequiredArgument("label"), new RequiredArgument("spacing")),
    LRBOX("lrbox"),
    MATH("math"),
    MINIPAGE("minipage", new OptionalArgument("position"), new RequiredArgument("width")),
    NORMALSIZE("normalsize"),
    QUOTATION("quotation"),
    QUOTE("quote"),
    SCRIPTSIZE("scriptsize"),
    SMALL("small"),
    TABBING("tabbing"),
    TABLE("table", new OptionalArgument("placement")),
    TABLE_STAR("table*", new OptionalArgument("placement")),
    TABULAR("tabular", new OptionalArgument("pos"), new RequiredArgument("cols")),
    TABULAR_STAR("tabular*", new RequiredArgument("width"), new OptionalArgument("pos"), new
            RequiredArgument("cols")),
    THEBIBLIOGRAPHY("thebibliography", new RequiredArgument("widestlabel")),
    THEINDEX("theindex"),
    THEOREM("theorem", new OptionalArgument("optional")),
    TINY("tiny"),
    TITLEPAGE("titlepage"),
    TRIVLIST("trivlist"),
    VERBATIM("verbatim"),
    VERBATIM_STAR("verbatim*"),
    VERSE("verse");

    private static final Map<String, LatexNoMathEnvironment> lookup = new HashMap<>();

    static {
        for (LatexNoMathEnvironment environment : LatexNoMathEnvironment.values()) {
            lookup.put(environment.getName(), environment);
        }
    }

    private final String name;
    private String initialContents = null;
    private Argument[] arguments;

    LatexNoMathEnvironment(String name, Argument... arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    LatexNoMathEnvironment(String name, String initialContents, Argument... arguments) {
        this(name, arguments);
        this.initialContents = initialContents;
    }

    public static LatexNoMathEnvironment get(String name) {
        return lookup.get(name);
    }

    public String getName() {
        return name;
    }

    public String getInitialContents() {
        return initialContents;
    }

    public Argument[] getArguments() {
        return arguments;
    }
}
