package nl.rubensten.texifyidea.folding;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sten Wessel
 */
public enum LatexMathCommand {
    /**
     * Greek alphabet
     */
    ALPHA("alpha", "α"),
    BETA("beta", "β"),
    GAMMA("gamma", "γ"),
    DELTA("delta", "δ"),
    EPSILON("epsilon", "ε"),
    ZETA("zeta", "ζ"),
    ETA("eta", "η"),
    THETA("theta", "θ"),
    IOTA("iota", "ι"),
    KAPPA("kappa", "κ"),
    LAMBDA("lambda", "λ"),
    MU("mu", "μ"),
    NU("nu", "ν"),
    XI("xi", "ξ"),
    PI("pi", "π"),
    RHO("rho", "ρ"),
    SIGMA("sigma", "σ"),
    FINAL_SIGMA("varsigma", "ς"),
    TAU("tau", "τ"),
    UPSILON("upsilon", "υ"),
    PHI("phi", "φ"),
    CHI("chi", "χ"),
    PSI("psi", "ψ"),
    OMEGA("omega", "ω"),
    ;

    private static final Map<String, LatexMathCommand> lookup = new HashMap<>();

    static {
        for (LatexMathCommand command : LatexMathCommand.values()) {
            lookup.put(command.getCommand(), command);
        }
    }

    private String command;
    private String display;

    LatexMathCommand(String command, String display) {
        this.command = command;
        this.display = display;
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
}
