package nl.rubensten.texifyidea.lang;

import nl.rubensten.texifyidea.lang.Argument.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static nl.rubensten.texifyidea.lang.Package.*;

/**
 * @author Ruben Schellekens, Sten Wessel
 */
public enum LatexMathCommand implements LatexCommand {

    /*
     *  Greek alphabet
     */
    ALPHA("alpha", "α", true),
    BETA("beta", "β", true),
    GAMMA("gamma", "γ", true),
    CAPITAL_GAMMA("Gamma", "Γ", true),
    DELTA("delta", "δ", true),
    CAPITAL_DELTA("Delta", "Δ", true),
    EPSILON("epsilon", "ϵ", true),
    EPSILON_SYMBOL("varepsilon", "ε", true),
    ZETA("zeta", "ζ", true),
    ETA("eta", "η", true),
    THETA("theta", "θ", true),
    THETA_SYMBOL("vartheta", "ϑ", true),
    CAPITAL_THETA("Theta", "Θ", true),
    IOTA("iota", "ι", true),
    KAPPA("kappa", "κ", true),
    LAMBDA("lambda", "λ", true),
    CAPITAL_LAMBDA("Lambda", "Λ", true),
    MU("mu", "μ", true),
    NU("nu", "ν", true),
    XI("xi", "ξ", true),
    CAPITAL_XI("Xi", "Ξ", true),
    PI("pi", "π", true),
    PI_SYMBOL("varpi", "ϖ", true),
    CAPITAL_PI("Pi", "Π", true),
    RHO("rho", "ρ", true),
    RHO_SYMBOL("varrho", "ϱ", true),
    SIGMA("sigma", "σ", true),
    FINAL_SIGMA("varsigma", "ς", true),
    CAPTIAL_SIGMA("Sigma", "Σ", true),
    TAU("tau", "τ", true),
    UPSILON("upsilon", "υ", true),
    PHI("phi", "φ", true),
    PHI_SYMBOL("varphi", "ϕ", true),
    CAPITAL_PHI("Phi", "Φ", true),
    CHI("chi", "χ", true),
    PSI("psi", "ψ", true),
    CAPITAL_PSI("Psi", "Ψ", true),
    OMEGA("omega", "ω", true),
    CAPITAL_OMEGA("Omega", "Ω", true),

    /*
     *  Operators
     */
    FORALL("forall", "∀", true),
    PARTIAL("partial", "∂", true),
    EXISTS("exists", "∃", true),
    NEXISTS("nexists", AMSSYMB, "∄", true),
    EMPTY_SET("emptyset", "∅", true),
    NOTHING("varnothing", AMSSYMB, "∅", true),
    NABLA("nabla", "∇", true),
    ELEMENT_OF("in", "∈", true),
    NOT_ELEMENT_OF("notin", "∉", true),
    CONTAIN_AS_MEMBER("ni", "∋", true),
    COMPLEMENT("complement", AMSSYMB, "∁", false),
    BIG_PRODUCT("prod", "∏", true),
    COPRODUCT("coprod", "∐", true),
    SUM("sum", "∑", true),
    MINUS_PLUS("mp", "∓", true),
    SET_MINUS("setminus", "∖", true),
    SMALL_SET_MINUS("smallsetminus", AMSSYMB, "∖", true),
    ASTERISK("ast", "∗", false),
    DOT_PLUS("dotplus", AMSSYMB, "∔", false),
    CIRCLE("circ", "∘", false),
    BULLET("bullet", "∙", false),
    PROPORTIONAL_TO("propto", "∝", true),
    PROPORTIONAL_TO_SYMBOL("varpropto", AMSSYMB, "∝", true),
    INFINITY("infty", "∞", true),
    ANGLE("angle", "∠", true),
    MEASURED_ANGLE("measuredangle", AMSSYMB, "∡", true),
    SPHERICAL_ANGLE("sphericalangle", AMSSYMB, "∢", true),
    MID("mid", "∣", true),
    MID_SHORT("shortmid", AMSSYMB, "∣", true),
    PARALLEL("parallel", "∥", true),
    NOT_PARALLEL("nparallel", AMSSYMB, "∦", true),
    LOGICAL_AND("land", "∧", true),
    LOGICAL_OR("lor", "∨", true),
    INTERSECTION("cap", "∩", true),
    UNION("cup", "∪", true),
    INTEGRAL("int", "∫", true),
    DOUBLE_INTEGRAL("iint", AMSMATH, "∬", true),
    TRIPLE_INTEGRAL("iiint", AMSMATH, "∭", true),
    QUADRUPLE_INTEGRAL("iiiint", AMSMATH, "⨌", true),
    CONTOUR_INTEGRAL("oint", "∮", true),
    THEREFORE("therefore", AMSSYMB, "∴", true),
    BECAUSE("because", AMSSYMB, "∵", true),
    TILDE_OPERATOR("sim", "~", true),
    WREATH_PRODUCT("wr", "≀", true),
    APPROX("approx", "≈", true),
    NOT_EQUAL("neq", "≠", true),
    EQUIVALENT("equiv", "≡", true),
    LESS_THAN_EQUAL("leq", "≤", true),
    LESS_THAN_EQUALL("leqq", AMSSYMB, "≦", true),
    GREATER_THAN_EQUAL("geq", "≥", true),
    GREATER_THAN_EQUALL("geqq", AMSSYMB, "≧", true),
    NOT_LESS_THAN("nless", AMSSYMB, "≮", true),
    NOT_GREATER_THAN("ngtr", AMSSYMB, "≯", true),
    NOT_LESS_THAN_EQUAL("nleq", AMSSYMB, "≰", true),
    NOT_LESS_THAN_EQUALL("nleqq", AMSSYMB, "≦\u200D\u0338", false),
    NOT_GREATER_THAN_EQUAL("ngeq", AMSSYMB, "≱", true),
    NOT_GREATER_THAN_EQUALL("ngeqq", AMSSYMB, "≧\u200D\u0338", false),
    LESSER_LESSER("ll", AMSSYMB, "≪", true),
    LESSER_LESSER_LESSER("lll", AMSSYMB, "⋘", true),
    LESSER_NOT_EQUAL("lneqq", AMSSYMB, "≨", true),
    GREATER_NOT_EQUAL("gneqq", AMSSYMB, "≩", true),
    GREATER_GREATER("gg", AMSSYMB, "≫", true),
    GREATER_GREATER_GREATER("ggg", AMSSYMB, "⋙", true),
    SUBSET("subset", "⊂", true),
    SUPSET("supset", "⊃", true),
    SUBSET_EQ("subseteq", "⊆", true),
    SUBSET_EQQ("subseteqq", AMSSYMB, "⊆", true),
    SUPSET_EQ("supseteq", "⊇", true),
    SUPSET_EQQ("supseteqq", AMSSYMB, "⊇", true),
    NOT_SUBSET_EQ("nsubseteq", AMSSYMB, "⊈", true),
    NOT_SUBSET_EQQ("nsubseteqq", AMSSYMB, "⊈", true),
    NOT_SUPSET_EQ("nsupseteq", AMSSYMB, "⊉", true),
    NOT_SUPSET_EQQ("nsupseteqq", AMSSYMB, "⊉", true),
    SQUARE_SUBSET("sqsubset", AMSSYMB, "⊏", true),
    SQUARE_SUPSET("sqsupset", AMSSYMB, "⊐", true),
    SQUARE_SUBSET_EQ("sqsubseteq", AMSSYMB, "⊑", true),
    SQUARE_SUPSET_EQ("sqsupseteq", AMSSYMB, "⊒", true),
    SQUARE_CAP("sqcap", "⊓", true),
    SQUARE_CUP("sqcup", "⊔", true),
    CIRCLED_PLUS("oplus", "⊕", true),
    CIRCLED_MINUS("ominus", "⊖", true),
    CIRCLED_TIMES("otimes", "⊗", true),
    CIRCLED_SLASH("oslash", "⊘", true),
    CIRCLED_DOT("odot", "⊙", true),
    BOXED_PLUS("boxplus", AMSSYMB, "⊞", true),
    BOXED_MINUS("boxminus", AMSSYMB, "⊟", true),
    BOXED_TIMES("boxtimes", AMSSYMB, "⊠", true),
    BOXED_DOT("boxdot", AMSSYMB, "⊡", true),
    BOWTIE("bowtie", "⋈", true),
    JOIN("Join", AMSSYMB, "⨝", true),
    TRIANGLERIGHT("triangleright", AMSSYMB, "▷", true),
    TRIANGLELEFT("triangleleft", AMSSYMB, "◁", true),
    VARTRIANGLERIGHT("vartriangleright", AMSSYMB, "⊳", true),
    VARTRIANGLELEFT("vartriangleleft", AMSSYMB, "⊲", true),
    TRIANGLELEFTEQ("tranglelefteq", AMSSYMB, "⊴", true),
    TRIANGLERIGHTEQ("trianglerighteq", AMSSYMB, "⊵", true),
    LTIMES("ltimes", AMSSYMB, "⋉", true),
    RTIMES("rtimes", AMSSYMB, "⋊", true),
    TIMES("times", "×", true),

    /*
     *  Left/Right
     */
    LEFT_PARENTH("left(", "(", false),
    RIGHT_PARENTH("right)", ")", false),
    LEFT_BRACKET("left[", "[", false),
    RIGHT_BRACKET("right]", "]", false),
    LEFT_BRACE("left\\{", "{", false),
    RIGHT_BRACE("right\\}", "}", false),
    LEFT_ANGULAR("left<", "<", false),
    RIGHT_ANGULAR("right>", ">", false),
    LEFT_PIPE("left|", "|", false),
    RIGHT_PIPE("right|", "|", false),
    LEFT_DOUBLE_PIPE("left\\|", "||", false),
    RIGHT_DOUBLE_PIPE("right\\|", "||", false),

    /*
     *  Arrows
     */
    NRIGHTARROW("nrightarrow", AMSSYMB, "↛", true),LEFTARROW("leftarrow", "←", true),
    L_EFTARROW("Leftarrow", "⇐", true),
    RIGHTARROW("rightarrow", "→", true),
    R_IGHTARROW("Rightarrow", "⇒", true),
    LEFTRIGHTARROW("leftrightarrow", "↔", true),
    L_EFTRIGHTARROW("Leftrightarrow", "⇔", true),
    LONGLEFTARROW("longleftarrow", "⟵", true),
    L_ONGLEFTARROW("Longleftarrow", "⟸", true),
    LONGRIGHTARROW("longrightarrow", "⟶", true),
    L_ONGRIGHTARROW("Longrightarrow", "⟹", true),
    LONGLEFTRIGHTARROW("longleftrightarrow", "⟷", true),
    L_ONGLEFTRIGHTARROW("Longleftrightarrow", "⟺", true),
    UPARROW("uparrow", "↑", true),
    U_PARROW("Uparrow", "⇑", true),
    DOWNARROW("downarrow", "↓", true),
    D_OWNARROW("Downarrow", "⇓", true),
    UPDOWNARROW("updownarrow", "↕", true),
    U_PDOWNARROW("Updownarrow", "⇕", true),
    MAPSTO("mapsto", "↦", true),
    HOOKLEFTARROW("hookleftarrow", "↩", true),
    LEFTHARPOONUP("leftharpoonup", "↼", true),
    LEFTHARPOONDOWN("leftharpoondown", "↽", true),
    RIGHTLEFTHARPOONS("rightleftharpoons", "⇌", true),
    LONGMAPSTO("longmapsto", "⟼", true),
    HOOKRIGHTARROW("hookrightarrow", "↪", true),
    RIGHTHARPOONUP("rightharpoonup", "⇀", true),
    RIGHTHARPOONDOWN("rightharpoondown", "⇁", true),
    LEADSTO("leadsto", LATEXSYMB, "⤳", true),
    NEARROW("nearrow", "↗", true),
    SEARROW("searrow", "↘", true),
    SWARROW("swarrow", "↙", true),
    NWARROW("nwarrow", "↖", true),
    DASHRIGHTARROW("dashrightarrow", AMSSYMB, "⤍", true),
    LEFTRIGHTARROWS("leftrightarrows", AMSSYMB, "⇆", true),
    LEFTARROWTAIL("leftarrowtail", AMSSYMB, "↢", true),
    CURVEARROWLEFT("curvearrowleft", AMSSYMB, "↶", true),
    UPUPARROWS("upuparrows", AMSSYMB, "⇈", true),
    MULTIMAP("multimap", AMSSYMB, "⊸", true),
    RIGHTLEFTARROWS("rightleftarrows", AMSSYMB, "⇄", true),
    TWOHEADRIGHTARROW("twoheadrightarrow", AMSSYMB, "↠", true),
    RSH("Rsh", AMSSYMB, "↱", true),
    DOWNHARPOONRIGHT("downharpoonright", AMSSYMB, "⇂", true),
    DASHLEFTARROW("dashleftarrow", AMSSYMB, "⇠", true),
    L_LEFTARROW("Lleftarrow", AMSSYMB, "⇚", true),
    LOOPARROWLEFT("looparrowleft", AMSSYMB, "↫", true),
    CIRCLEARROWLEFT("circlearrowleft", AMSSYMB, "↺", true),
    UPHARPOONLEFT("upharpoonleft", AMSSYMB, "↿", true),
    LEFTRIGHTSQUIGARROW("leftrightsquigarrow", AMSSYMB, "↭", true),
    RIGHTRIGHTARROWS("rightrightarrows", AMSSYMB, "⇉", true),
    RIGHTARROWTAIL("rightarrowtail", AMSSYMB, "↣", true),
    CURVEARROWRIGHT("curvearrowright", AMSSYMB, "↷", true),
    DOWNDOWNARROWS("downdownarrows", AMSSYMB, "⇊", true),
    RIGHTSQUIGARROW("rightsquigarrow", AMSSYMB, "⇝", true),
    LEFTLEFTARROWS("leftleftarrows", AMSSYMB, "⇇", true),
    TWOHEADLEFTARROW("twoheadleftarrow", AMSSYMB, "↞", true),
    LEFTRIGHTHARPOONS("leftrightharpoons", AMSSYMB, "↰", true),
    LSH("Lsh", AMSSYMB, "↿", true),
    DOWNHARPOONLEFT("downharpoonleft", AMSSYMB, "⇃", true),
    LOOPARROWRIGHT("looparrowright", AMSSYMB, "↬", true),
    CIRCLEARROWRIGHT("circlearrowright", AMSSYMB, "↻", true),
    UPHARPOONRIGHT("rightsquigarrow", AMSSYMB, "↾", true),
    NLEFTARROW("nleftarrow", AMSSYMB, "↚", true),
    NL_EFTARROW("nLeftarrow", AMSSYMB, "⇍", true),
    NR_IGHTARROW("nRightarrow", AMSSYMB, "⇏", true),
    NLEFTRIGHTARROW("nleftrightarrow", AMSSYMB, "↮", true),
    NL_EFTRIGHTARROW("nLeftrightarrow", AMSSYMB, "⇎", true),

    /*
     *  Generic commands
     */
    MATHBB("mathbb", AMSSYMB, requiredText("text")),
    MATHBF("mathbf", required("text")),
    MATHCAL("mathcal", required("text")),
    MATHDS("mathds", required("mathds")),
    MATHELLIPSIS("mathellipsis"),
    MATHFRAK("mathfrak", AMSSYMB, requiredText("text")),
    MATHGROUP("mathgroup"),
    MATHIT("mathit", required("text")),
    MATHNORMAL("mathnormal", required("text")),
    MATHRM("mathrm", required("text")),
    MATHSCR("mathscr"),
    MATHSF("mathsf", required("text")),
    MATHSTERLING("mathsterling"),
    MATHTT("mathtt", required("text")),
    MATHUNDERSCORE("mathunderscore"),
    SQRT("sqrt", optional("root"), required("arg")),
    ACUTE("acute", required("a")),
    ALEPH("aleph"),
    AMALG("amalg"),
    ARCCOS("arccos"),
    ARCSIN("arcsin"),
    ARCTAN("arctan"),
    ARG("arg"),
    ARROWVERT("arrowvert", "|", true),
    CAPITAL_ARROWVERT("Arrowvert"),
    ASYMP("asymp", "≍", true),
    BACKSLASH("backslash", "\\", true),
    BAR("bar", required("a")),
    BIGCAP("bigcap", "⋂", true),
    BIGCIRC("bigcirc", "○", true),
    BIGCUP("bigcup", "⋃", true),
    BIGODOT("bigodot", "⨀", true),
    BIGOPLUS("bigoplus", "⨁", true),
    BIGOTIMES("bigotimes", "⨂", true),
    BIGSQCUP("bigsqcup", "⨆", true),
    BIGTRIANGLEDOWN("bigtriangledown", "▽", true),
    BIGTRIANGLEUP("bigtriangleup", "∆", true),
    BIGUPLUS("biguplus", "⨄", true),
    BIGVEE("bigvee", "⋁", true),
    BIGWEDGE("bigwedge", "⋀", true),
    BOT("bot", "⟂", true),
    BRACEVERT("bracevert"),
    BREVE("breve", required("a")),
    CDOT("cdot", "·", true),
    CDOTS("cdots", "⋯", true),
    CHECK("check", required("a")),
    CLUBSUIT("clubsuit", "♣", true),
    COLON("colon", ":", true),
    CONG("cong"),
    COS("cos"),
    COSH("cosh"),
    COT("cot"),
    COTH("coth"),
    CSC("csc"),
    DAGGER("dagger", "†", true),
    DASHV("dashv", "⊣", true),
    DDAGGER("ddagger", "‡", true),
    DEG("deg"),
    DET("det"),
    DFRAC("dfrac", AMSMATH, required("num"), required("den")),
    DIAMOND("diamond", "◇", true),
    DIAMONDSUIT("diamondsuit", "♢", true),
    DIM("dim"),
    DIV("div", "÷", true),
    DIVIDEONTIMES("divideontimes", AMSSYMB, "⋇", true),
    DOTEQ("doteq"),
    DOT("dot", required("a")),
    DOTS("dots", AMSMATH, "⋯", true),
    ELL("ell"),
    EXP("exp"),
    FLAT("flat", "♭", true),
    SHARP("sharp", "♯", true),
    FRAC("frac", required("num"), required("den")),
    GRAVE("grave", required("a")),
    HAT("hat", required("a")),
    MATHRING("mathring", required("a")),
    OVERBRACE("overbrace", required("text")),
    OVERLINE("overline", required("text")),
    TEXT("text", AMSMATH, required("text")),
    TILDE("tilde", required("a")),
    UNDERBRACE("underbrace", required("text")),
    UNDERLINE("underline", required("text")),
    VEC("vec", required("a")),
    WIDEHAT("widehat", required("text")),
    WIDETILDE("widetilde", required("text"));

    private static final Map<String, LatexMathCommand> lookup = new HashMap<>();
    private static final Map<String, LatexMathCommand> lookupDisplay = new HashMap<>();

    static {
        for (LatexMathCommand command : LatexMathCommand.values()) {
            lookup.put(command.getCommand(), command);
            if (command.display != null) {
                lookupDisplay.putIfAbsent(command.display, command);
            }
        }
    }

    private String command;
    private Package thePackage;
    private Argument[] arguments;
    private String display;
    private boolean collapse;

    LatexMathCommand(String command, Package thePackage, String display,
                     boolean collapse, Argument... arguments) {
        this.command = command;
        this.thePackage = thePackage;
        this.arguments = arguments != null ? arguments : Argument.EMPTY_ARRAY;
        this.display = display;
        this.collapse = collapse;
    }

    LatexMathCommand(String command, Package thePackage, Argument... arguments) {
        this(command, thePackage, null, false, arguments);
    }

    LatexMathCommand(String command, Argument... arguments) {
        this(command, DEFAULT, arguments);
    }

    LatexMathCommand(String command, Package thePackage) {
        this(command, thePackage, Argument.EMPTY_ARRAY);
    }

    LatexMathCommand(String command) {
        this(command, Argument.EMPTY_ARRAY);
    }

    LatexMathCommand(String command, Package thePackage, String display, boolean collapse) {
        this(command, thePackage, display, collapse, Argument.EMPTY_ARRAY);
    }

    LatexMathCommand(String command, String display, boolean collapse) {
        this(command, DEFAULT, display, collapse);
    }

    public static LatexMathCommand get(String command) {
        return lookup.get(command);
    }

    @Nullable
    public static LatexMathCommand findByDisplay(String display) {
        return lookupDisplay.get(display);
    }

    private static RequiredArgument required(String name) {
        return new RequiredArgument(name);
    }

    private static RequiredArgument requiredText(String name) {
        return new RequiredArgument(name, Type.TEXT);
    }

    private static OptionalArgument optional(String name) {
        return new OptionalArgument(name);
    }

    private static OptionalArgument optionalText(String name) {
        return new OptionalArgument(name, Type.TEXT);
    }

    public String getCommand() {
        return command;
    }

    public String getCommandDisplay() {
        return "\\" + command;
    }

    public Argument[] getArguments() {
        return arguments != null ? arguments : Argument.EMPTY_ARRAY;
    }

    public String getArgumentsDisplay() {
        StringBuilder sb = new StringBuilder();
        for (Argument arg : getArguments()) {
            sb.append(arg.toString());
        }

        return sb.toString();
    }

    public String getDisplay() {
        return display;
    }

    public boolean isCollapse() {
        return collapse;
    }

    @NotNull
    public Package getDependency() {
        return thePackage;
    }

    /**
     * Checks whether {@code {}} must be automatically inserted in the auto complete.
     *
     * @return {@code true} to insert automatically, {@code false} not to insert.
     */
    public boolean autoInsertRequired() {
        return arguments != null &&
                Stream.of(arguments)
                        .filter(arg -> arg instanceof RequiredArgument)
                        .count() >= 1;
    }
}
