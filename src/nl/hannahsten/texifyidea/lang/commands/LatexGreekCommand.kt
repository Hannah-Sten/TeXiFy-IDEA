package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.AMSMATH

/**
 * @author Hannah Schellekens
 */
enum class LatexGreekCommand(
    override val command: String,
    override vararg val arguments: Argument = emptyArray(),
    override val dependency: LatexPackage = LatexPackage.DEFAULT,
    override val display: String? = null,
    override val isMathMode: Boolean = true,
    val collapse: Boolean = false
) : LatexCommand {

    ALPHA("alpha", display = "α", collapse = true),
    BETA("beta", display = "β", collapse = true),
    GAMMA("gamma", display = "γ", collapse = true),
    CAPITAL_GAMMA("Gamma", display = "Γ", collapse = true),
    CAPITAL_GAMMA_VARIANT("varGamma", dependency = AMSMATH, collapse = true),
    DELTA("delta", display = "δ", collapse = true),
    CAPITAL_DELTA("Delta", display = "Δ", collapse = true),
    CAPITAL_DELTA_VARIANT("varDelta", dependency = AMSMATH, collapse = true),
    EPSILON("epsilon", display = "ϵ", collapse = true),
    BETTER_LOOKING_EPSILON("varepsilon", display = "ε", collapse = true),
    ZETA("zeta", display = "ζ", collapse = true),
    ETA("eta", display = "η", collapse = true),
    THETA("theta", display = "θ", collapse = true),
    THETA_VARIANT("vartheta", display = "ϑ", collapse = true),
    CAPITAL_THETA("Theta", display = "Θ", collapse = true),
    CAPITAL_THETA_VARIANT("varTheta", dependency = AMSMATH, collapse = true),
    IOTA("iota", display = "ι", collapse = true),
    KAPPA("kappa", display = "κ", collapse = true),
    LAMBDA("lambda", display = "λ", collapse = true),
    CAPITAL_LAMBDA("Lambda", display = "Λ", collapse = true),
    CAPITAL_LAMBDA_VARIANT("varLambda", dependency = AMSMATH, collapse = true),
    MU("mu", display = "μ", collapse = true),
    NU("nu", display = "ν", collapse = true),
    XI("xi", display = "ξ", collapse = true),
    CAPITAL_XI("Xi", display = "Ξ", collapse = true),
    CAPITAL_XI_VARIANT("varXi", dependency = AMSMATH, collapse = true),
    PI("pi", display = "π", collapse = true),
    PI_VARIANT("varpi", display = "ϖ", collapse = true),
    CAPITAL_PI("Pi", display = "Π", collapse = true),
    CAPITAL_PI_VARIANT("varPi", dependency = AMSMATH, collapse = true),
    RHO("rho", display = "ρ", collapse = true),
    RHO_VARIANT("varrho", display = "ϱ", collapse = true),
    SIGMA("sigma", display = "σ", collapse = true),
    FINAL_SIGMA("varsigma", display = "ς", collapse = true),
    CAPITAL_SIGMA("Sigma", display = "Σ", collapse = true),
    CAPITAL_SIGMA_VARIANT("varSigma", dependency = AMSMATH, collapse = true),
    TAU("tau", display = "τ", collapse = true),
    UPSILON("upsilon", display = "υ", collapse = true),
    CAPITAL_UPSILON("Upsilon", display = "ϒ", collapse = true),
    CAPITAL_UPSILON_VARIANT("Upsilon", dependency = AMSMATH, collapse = true),
    PHI_STRAIGHT("phi", display = "ϕ", collapse = true),
    PHI_CURLY("varphi", display = "φ", collapse = true),
    CAPITAL_PHI("Phi", display = "Φ", collapse = true),
    CAPITAL_PHI_VARIANT("varPhi", dependency = AMSMATH, collapse = true),
    CHI("chi", display = "χ", collapse = true),
    PSI("psi", display = "ψ", collapse = true),
    CAPITAL_PSI("Psi", display = "Ψ", collapse = true),
    CAPITAL_PSI_VARIANT("varPsi", dependency = AMSMATH, collapse = true),
    OMEGA("omega", display = "ω", collapse = true),
    CAPITAL_OMEGA("Omega", display = "Ω", collapse = true),
    CAPITAL_OMEGA_VARIANT("varOmega", dependency = AMSMATH, collapse = true),
    ;

    override val identifier: String
        get() = name
}