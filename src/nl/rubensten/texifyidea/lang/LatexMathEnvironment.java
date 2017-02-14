package nl.rubensten.texifyidea.lang;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sten Wessel
 */
public enum LatexMathEnvironment {
    /*
        Default LaTeX.
     */
    ARRAY("array", new OptionalArgument("pos"), new RequiredArgument("cols")),
    BMATRIX("bmatrix"),
    CAPITAL_BMATRIX("Bmatrix"),
    MATRIX("matrix"),
    PMATRIX("pmatrix"),
    VMATRIX("vmatrix"),
    CAPITAL_VMATRIX("Vmatrix");

    private static final Map<String, LatexMathEnvironment> lookup = new HashMap<>();

    static {
        for (LatexMathEnvironment environment : LatexMathEnvironment.values()) {
            lookup.put(environment.getName(), environment);
        }
    }

    private final String name;
    private String initialContents = null;
    private Argument[] arguments;

    LatexMathEnvironment(String name, Argument... arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    LatexMathEnvironment(String name, String initialContents, Argument... arguments) {
        this(name, arguments);
        this.initialContents = initialContents;
    }

    public static LatexMathEnvironment get(String name) {
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
