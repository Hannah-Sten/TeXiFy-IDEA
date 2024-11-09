package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.UPGREEK

/**
 * @author Hannah Schellekens
 */
enum class LatexUpgreekCommand(
    override val command: String,
    override vararg val arguments: Argument = emptyArray(),
    override val dependency: LatexPackage = LatexPackage.DEFAULT,
    override val display: String? = null,
    override val isMathMode: Boolean = true,
    val collapse: Boolean = false
) : LatexCommand {

    UPALPHA("upalpha", display = "α", collapse = true, dependency = UPGREEK),
    UPBETA("upbeta", display = "β", collapse = true, dependency = UPGREEK),
    UPGAMMA("upgamma", display = "γ", collapse = true, dependency = UPGREEK),
    UPGAMMA_CAPITAL("Upgamma", display = "Γ", collapse = true, dependency = UPGREEK),
    UPDELTA("updelta", display = "δ", collapse = true, dependency = UPGREEK),
    UPDELTA_CAPITAL("Updelta", display = "Δ", collapse = true, dependency = UPGREEK),
    UPEPSILON("upepsilon", display = "ϵ", collapse = true, dependency = UPGREEK),
    UPEPSILON_VARIANT("upvarepsilon", display = "ε", collapse = true, dependency = UPGREEK),
    UPZETA("upzeta", display = "ζ", collapse = true, dependency = UPGREEK),
    UPETA("upeta", display = "η", collapse = true, dependency = UPGREEK),
    UPTHETA("uptheta", display = "θ", collapse = true, dependency = UPGREEK),
    UPTHETA_VARIANT("upvartheta", display = "ϑ", collapse = true, dependency = UPGREEK),
    UPTHETA_CAPITAL("Uptheta", display = "Θ", collapse = true, dependency = UPGREEK),
    UPIOTA("upiota", display = "ι", collapse = true, dependency = UPGREEK),
    UPKAPPA("upkappa", display = "κ", collapse = true, dependency = UPGREEK),
    UPLAMBDA("uplambda", display = "λ", collapse = true, dependency = UPGREEK),
    UPLAMBDA_CAPITAL("Uplambda", display = "Λ", collapse = true, dependency = UPGREEK),
    UPMU("upmu", display = "μ", collapse = true, dependency = UPGREEK),
    UPNU("upnu", display = "ν", collapse = true, dependency = UPGREEK),
    UPXI("upxi", display = "ξ", collapse = true, dependency = UPGREEK),
    UPXI_CAPITAL("Upxi", display = "Ξ", collapse = true, dependency = UPGREEK),
    UPPI("uppi", display = "π", collapse = true, dependency = UPGREEK),
    UPPI_VARIANT("upvarpi", display = "ϖ", collapse = true, dependency = UPGREEK),
    UPPI_CAPITAL("Uppi", display = "Π", collapse = true, dependency = UPGREEK),
    UPRHO("uprho", display = "ρ", collapse = true, dependency = UPGREEK),
    UPRHO_VARIANT("upvarrho", display = "ρ", collapse = true, dependency = UPGREEK),
    UPSIGMA("upsigma", display = "σ", collapse = true, dependency = UPGREEK),
    UPSIGMA_VARIANT("upvarsigma", display = "σ", collapse = true, dependency = UPGREEK),
    UPSIGMA_CAPITAL("Upsigma", display = "Σ", collapse = true, dependency = UPGREEK),
    UPTAU("uptau", display = "τ", collapse = true, dependency = UPGREEK),
    UPUPSILON("upupsilon", display = "υ", collapse = true, dependency = UPGREEK),
    UPUPSILON_CAPITAL("Upupsilon", display = "ϒ", collapse = true, dependency = UPGREEK),
    UPPHI("upphi", display = "ϕ", collapse = true, dependency = UPGREEK),
    UPPHI_VARIANT("upvarphi", display = "φ", collapse = true, dependency = UPGREEK),
    UPPHI_CAPITAL("Upphi", display = "Φ", collapse = true, dependency = UPGREEK),
    UPCHI("upchi", display = "χ", collapse = true, dependency = UPGREEK),
    UPPSI("uppsi", display = "ψ", collapse = true, dependency = UPGREEK),
    UPPSI_CAPITAL("Uppsi", display = "Ψ", collapse = true, dependency = UPGREEK),
    UPOMEGA("upomega", display = "ω", collapse = true, dependency = UPGREEK),
    UPOMEGA_CAPITAL("Upomega", display = "Ω", collapse = true, dependency = UPGREEK),
    ;

    override val identifier: String
        get() = name
}