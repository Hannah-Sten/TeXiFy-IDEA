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

    ALPHA("alpha", display = "α"),
    BETA("beta", display = "β"),
    GAMMA("gamma", display = "γ"),
    CAPITAL_GAMMA("Gamma", display = "Γ"),
    CAPITAL_GAMMA_VARIANT("varGamma", dependency = AMSMATH),
    DELTA("delta", display = "δ"),
    CAPITAL_DELTA("Delta", display = "Δ"),
    CAPITAL_DELTA_VARIANT("varDelta", dependency = AMSMATH),
    EPSILON("epsilon", display = "ϵ"),
    BETTER_LOOKING_EPSILON("varepsilon", display = "ε"),
    ZETA("zeta", display = "ζ"),
    ETA("eta", display = "η"),
    THETA("theta", display = "θ"),
    THETA_VARIANT("vartheta", display = "ϑ"),
    CAPITAL_THETA("Theta", display = "Θ"),
    CAPITAL_THETA_VARIANT("varTheta", dependency = AMSMATH),
    IOTA("iota", display = "ι"),
    KAPPA("kappa", display = "κ"),
    LAMBDA("lambda", display = "λ"),
    CAPITAL_LAMBDA("Lambda", display = "Λ"),
    CAPITAL_LAMBDA_VARIANT("varLambda", dependency = AMSMATH),
    MU("mu", display = "μ"),
    NU("nu", display = "ν"),
    XI("xi", display = "ξ"),
    CAPITAL_XI("Xi", display = "Ξ"),
    CAPITAL_XI_VARIANT("varXi", dependency = AMSMATH),
    PI("pi", display = "π"),
    PI_VARIANT("varpi", display = "ϖ"),
    CAPITAL_PI("Pi", display = "Π"),
    CAPITAL_PI_VARIANT("varPi", dependency = AMSMATH),
    RHO("rho", display = "ρ"),
    RHO_VARIANT("varrho", display = "ϱ"),
    SIGMA("sigma", display = "σ"),
    FINAL_SIGMA("varsigma", display = "ς"),
    CAPITAL_SIGMA("Sigma", display = "Σ"),
    CAPITAL_SIGMA_VARIANT("varSigma", dependency = AMSMATH),
    TAU("tau", display = "τ"),
    UPSILON("upsilon", display = "υ"),
    CAPITAL_UPSILON("Upsilon", display = "ϒ"),
    CAPITAL_UPSILON_VARIANT("Upsilon", dependency = AMSMATH),
    PHI_STRAIGHT("phi", display = "ϕ"),
    PHI_CURLY("varphi", display = "φ"),
    CAPITAL_PHI("Phi", display = "Φ"),
    CAPITAL_PHI_VARIANT("varPhi", dependency = AMSMATH),
    CHI("chi", display = "χ"),
    PSI("psi", display = "ψ"),
    CAPITAL_PSI("Psi", display = "Ψ"),
    CAPITAL_PSI_VARIANT("varPsi", dependency = AMSMATH),
    OMEGA("omega", display = "ω"),
    CAPITAL_OMEGA("Omega", display = "Ω"),
    CAPITAL_OMEGA_VARIANT("varOmega", dependency = AMSMATH),
    ;

    override val identifier: String
        get() = name
}