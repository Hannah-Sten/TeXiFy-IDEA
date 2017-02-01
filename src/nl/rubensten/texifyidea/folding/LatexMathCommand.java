package nl.rubensten.texifyidea.folding;

import java.util.HashMap;
import java.util.Map;

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
    NEXISTS("nexists", "∄", true),
    EMPTY_SET("emptyset", "∅", true),
    NOTHING("varnothing", "∅", true),
    NABLA("nabla", "∇", true),
    ELEMENT_OF("in", "∈", true),
    NOT_ELEMENT_OF("notin", "∉", true),
    CONTAIN_AS_MEMBER("ni", "∋", true),
    COMPLEMENT("complement", "∁", false),
    BIG_PRODUCT("prod", "∏", true),
    COPRODUCT("coprod", "∐", true),
    SUM("sum", "∑", true),
    MINUS_PLUS("mp", "∓", true),
    SET_MINUS("setminus", "∖", true),
    ASTERISK("ast", "∗", false),
    DOT_PLUS("dotplus", "∔", false),
    CIRCLE("circ", "∘", false),
    BULLET("bullet", "∙", false),
    PROPORTIONAL_TO("propto", "∝", true),
    PROPORTIONAL_TO_SYMBOL("varpropto", "∝", true),
    INFINITY("infty", "∞", true),
    ANGLE("angle", "∠", true),
    MEASURED_ANGLE("measuredangle", "∡", false),
    SPHERICAL_ANGLE("sphericalangle", "∢", false),
    DIVIDES("divides", "∣", true),
    PARALLEL("parallel", "∥", false),
    NOT_PARALLEL("nparallel", "∦", false),
    LOGICAL_AND("land", "∧", true),
    LOGICAL_OR("lor", "∨", true),
    INTERSECTION("cap", "∩", true),
    UNION("cup", "∪", true),
    INTEGRAL("int", "∫", true),
    DOUBLE_INTEGRAL("iint", "∬", true),
    TRIPLE_INTEGRAL("iiint", "∭", true),
    QUADRUPLE_INTEGRAL("iiiint", "⨌", true),
    CONTOUR_INTEGRAL("oint", "∮", true),
    THEREFORE("therefore", "∴", true),
    BECAUSE("because", "∵", true),
    TILDE_OPERATOR("sim", "~", true),
    WREATH_PRODUCT("wr", "≀", true),
    APPROX("approx", "≈", true),
    NOT_EQUAL("neq", "≠", true),
    EQUIVALENT("equiv", "≡", true),
    LESS_THAN_EQUAL("leq", "≤", true),
    LESS_THAN_EQUALL("leqq", "≤", true),
    GREATER_THAN_EQUAL("geq", "≥", true),
    GREATER_THAN_EQUALL("geqq", "≥", true),
    NOT_LESS_THAN("nless", "≮", false),
    NOT_GREATER_THAN("ngtr", "≯", false),
    NOT_LESS_THAN_EQUAL("nleq", "≰", false),
    NOT_LESS_THAN_EQUALL("nleqq", "≰", false),
    NOT_GREATER_THAN_EQUAL("ngeq", "≱", false),
    NOT_GREATER_THAN_EQUALL("ngeqq", "≱", false),
    SUBSET("subset", "⊂", true),
    SUPSET("supset", "⊃", true),
    SUBSET_EQ("subseteq", "⊆", false),
    SUBSET_EQQ("subseteqq", "⊆", false),
    SUPSET_EQ("supseteq", "⊇", false),
    SUPSET_EQQ("supseteqq", "⊇", false),
    NOT_SUBSET_EQ("nsubseteq", "⊈", false),
    NOT_SUBSET_EQQ("nsubseteqq", "⊈", false),
    NOT_SUPSET_EQ("nsupseteq", "⊉", false),
    NOT_SUPSET_EQQ("nsupseteqq", "⊉", false),
    SQUARE_SUBSET("sqsubset", "⊏", false),
    SQUARE_SUPSET("sqsupset", "⊐", false),
    SQUARE_SUBSET_EQ("sqsubseteq", "⊑", true),
    SQUARE_SUPSET_EQ("sqsupseteq", "⊒", true),
    SQUARE_CAP("sqcap", "⊓", false),
    SQUARE_CUP("sqcup", "⊔", false),
    CIRCLED_PLUS("oplus", "⊕", false),
    CIRCLED_MINUS("ominus", "⊖", false),
    CIRCLED_TIMES("otimes", "⊗", false),
    CIRCLED_SLASH("oslash", "⊘", false),
    CIRCLED_DOT("odot", "⊙", false),
    BOXED_PLUS("boxplus", "⊞", false),
    BOXED_MINUS("boxminus", "⊟", false),
    BOXED_TIMES("boxtimes", "⊠", false),
    BOXED_DOT("boxdot", "⊡", false),
    ;

    private static final Map<String, LatexMathCommand> lookup = new HashMap<>();

    static {
        for (LatexMathCommand command : LatexMathCommand.values()) {
            lookup.put(command.getCommand(), command);
        }
    }

    private String command;
    private String display;
    private boolean collapse;

    LatexMathCommand(String command, String display, boolean collapse) {
        this.command = command;
        this.display = display;
        this.collapse = collapse;
    }

    public static LatexMathCommand get(String command) {
        return lookup.get(command);
    }

    public String getCommand() {
        return command;
    }

    public String getDisplay() {
        return display;
    }

    public boolean isCollapse() {
        return collapse;
    }
}
