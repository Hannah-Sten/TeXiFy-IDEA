package nl.rubensten.texifyidea.lang;

import nl.rubensten.texifyidea.lang.Argument.Type;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static nl.rubensten.texifyidea.lang.Package.AMSMATH;
import static nl.rubensten.texifyidea.lang.Package.AMSSYMB;
import static nl.rubensten.texifyidea.lang.Package.DEFAULT;

/**
 * @author Sten Wessel
 */
public enum LatexMathCommand {
    /*
      Greek alphabet
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
      Operators
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
    MEASURED_ANGLE("measuredangle", AMSSYMB, "∡", false),
    SPHERICAL_ANGLE("sphericalangle", AMSSYMB, "∢", false),
    MID("mid", "∣", true),
    MID_SHORT("shortmid", AMSSYMB, "∣", true),
    PARALLEL("parallel", "∥", false),
    NOT_PARALLEL("nparallel", AMSSYMB, "∦", false),
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
    NOT_LESS_THAN("nless", AMSSYMB, "≮", false),
    NOT_GREATER_THAN("ngtr", AMSSYMB, "≯", false),
    NOT_LESS_THAN_EQUAL("nleq", AMSSYMB, "≰", false),
    NOT_LESS_THAN_EQUALL("nleqq", AMSSYMB, "≦\u200D\u0338", false),
    NOT_GREATER_THAN_EQUAL("ngeq", AMSSYMB, "≱", false),
    NOT_GREATER_THAN_EQUALL("ngeqq", AMSSYMB, "≧\u200D\u0338", false),
    SUBSET("subset", "⊂", true),
    SUPSET("supset", "⊃", true),
    SUBSET_EQ("subseteq", "⊆", false),
    SUBSET_EQQ("subseteqq", AMSSYMB, "⊆", false),
    SUPSET_EQ("supseteq", "⊇", false),
    SUPSET_EQQ("supseteqq", AMSSYMB, "⊇", false),
    NOT_SUBSET_EQ("nsubseteq", AMSSYMB, "⊈", false),
    NOT_SUBSET_EQQ("nsubseteqq", AMSSYMB, "⊈", false),
    NOT_SUPSET_EQ("nsupseteq", AMSSYMB, "⊉", false),
    NOT_SUPSET_EQQ("nsupseteqq", AMSSYMB, "⊉", false),
    SQUARE_SUBSET("sqsubset", AMSSYMB, "⊏", false),
    SQUARE_SUPSET("sqsupset", AMSSYMB, "⊐", false),
    SQUARE_SUBSET_EQ("sqsubseteq", AMSSYMB, "⊑", true),
    SQUARE_SUPSET_EQ("sqsupseteq", AMSSYMB, "⊒", true),
    SQUARE_CAP("sqcap", "⊓", false),
    SQUARE_CUP("sqcup", "⊔", false),
    CIRCLED_PLUS("oplus", "⊕", false),
    CIRCLED_MINUS("ominus", "⊖", false),
    CIRCLED_TIMES("otimes", "⊗", false),
    CIRCLED_SLASH("oslash", "⊘", false),
    CIRCLED_DOT("odot", "⊙", false),
    BOXED_PLUS("boxplus", AMSSYMB, "⊞", false),
    BOXED_MINUS("boxminus", AMSSYMB, "⊟", false),
    BOXED_TIMES("boxtimes", AMSSYMB, "⊠", false),
    BOXED_DOT("boxdot", AMSSYMB, "⊡", false),

    /*
        Generic commands
     */
    MATHBF("mathbf", required("text")),
    MATHCAL("mathcal", required("text")),
    MATHDS("mathds", required("mathds")),
    MATHELLIPSIS("mathellipsis"),
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
    DOTEQ("doteq"),
    DOT("dot", required("a")),
    DOWNARROW("downarrow"),
    CAPITAL_DOWNARROW("Downarrow"),
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
    TILDE("tilde", required("a")),
    UNDERBRACE("underbrace", required("text")),
    UNDERLINE("underline", required("text")),
    VEC("vec", required("a")),
    WIDEHAT("widehat", required("text")),
    WIDETILDE("widetilde", required("text"));

    private static final Map<String, LatexMathCommand> lookup = new HashMap<>();
    static {
        for (LatexMathCommand command : LatexMathCommand.values()) {
            lookup.put(command.getCommand(), command);
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
    public Package getPackage() {
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
