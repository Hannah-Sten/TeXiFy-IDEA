package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.LSemanticEnv
import nl.hannahsten.texifyidea.lang.LatexSemanticsEnvLookup

object AllPredefinedEnvironments : LatexSemanticsEnvLookup {

    val allEnvironments: List<LSemanticEnv> =
        listOf(
            PredefinedEnvBasic
        ).flatMap {
            it.allEnvironments
        }

    val packageToEnvironments: Map<String, List<LSemanticEnv>> = allEnvironments.groupBy { it.dependency }

    val simpleNameLookup = allEnvironments.associateBy { it.name }

    override fun lookupEnv(name: String): LSemanticEnv? {
        return simpleNameLookup[name]
    }
}