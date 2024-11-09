package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.UPGREEK

/**
 * @author Florian Kraft
 */
enum class LatexUpgreekCommand(
    override val command: String,
    override vararg val arguments: Argument = emptyArray(),
    override val dependency: LatexPackage = LatexPackage.DEFAULT,
    override val display: String? = null,
    override val isMathMode: Boolean = true,
    val collapse: Boolean = false
) : LatexCommand {

    UPALPHA("upalpha", display = "α", dependency = UPGREEK),
    UPBETA("upbeta", display = "β", dependency = UPGREEK),
    UPGAMMA("upgamma", display = "γ", dependency = UPGREEK),
    UPGAMMA_CAPITAL("Upgamma", display = "Γ", dependency = UPGREEK),
    UPDELTA("updelta", display = "δ", dependency = UPGREEK),
    UPDELTA_CAPITAL("Updelta", display = "Δ", dependency = UPGREEK),
    UPEPSILON("upepsilon", display = "ϵ", dependency = UPGREEK),
    UPEPSILON_VARIANT("upvarepsilon", display = "ε", dependency = UPGREEK),
    UPZETA("upzeta", display = "ζ", dependency = UPGREEK),
    UPETA("upeta", display = "η", dependency = UPGREEK),
    UPTHETA("uptheta", display = "θ", dependency = UPGREEK),
    UPTHETA_VARIANT("upvartheta", display = "ϑ", dependency = UPGREEK),
    UPTHETA_CAPITAL("Uptheta", display = "Θ", dependency = UPGREEK),
    UPIOTA("upiota", display = "ι", dependency = UPGREEK),
    UPKAPPA("upkappa", display = "κ", dependency = UPGREEK),
    UPLAMBDA("uplambda", display = "λ", dependency = UPGREEK),
    UPLAMBDA_CAPITAL("Uplambda", display = "Λ", dependency = UPGREEK),
    UPMU("upmu", display = "μ", dependency = UPGREEK),
    UPNU("upnu", display = "ν", dependency = UPGREEK),
    UPXI("upxi", display = "ξ", dependency = UPGREEK),
    UPXI_CAPITAL("Upxi", display = "Ξ", dependency = UPGREEK),
    UPPI("uppi", display = "π", dependency = UPGREEK),
    UPPI_VARIANT("upvarpi", display = "ϖ", dependency = UPGREEK),
    UPPI_CAPITAL("Uppi", display = "Π", dependency = UPGREEK),
    UPRHO("uprho", display = "ρ", dependency = UPGREEK),
    UPRHO_VARIANT("upvarrho", display = "ρ", dependency = UPGREEK),
    UPSIGMA("upsigma", display = "σ", dependency = UPGREEK),
    UPSIGMA_VARIANT("upvarsigma", display = "σ", dependency = UPGREEK),
    UPSIGMA_CAPITAL("Upsigma", display = "Σ", dependency = UPGREEK),
    UPTAU("uptau", display = "τ", dependency = UPGREEK),
    UPUPSILON("upupsilon", display = "υ", dependency = UPGREEK),
    UPUPSILON_CAPITAL("Upupsilon", display = "ϒ", dependency = UPGREEK),
    UPPHI("upphi", display = "ϕ", dependency = UPGREEK),
    UPPHI_VARIANT("upvarphi", display = "φ", dependency = UPGREEK),
    UPPHI_CAPITAL("Upphi", display = "Φ", dependency = UPGREEK),
    UPCHI("upchi", display = "χ", dependency = UPGREEK),
    UPPSI("uppsi", display = "ψ", dependency = UPGREEK),
    UPPSI_CAPITAL("Uppsi", display = "Ψ", dependency = UPGREEK),
    UPOMEGA("upomega", display = "ω", dependency = UPGREEK),
    UPOMEGA_CAPITAL("Upomega", display = "Ω", dependency = UPGREEK),
    ;

    override val identifier: String
        get() = name
}