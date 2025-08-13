package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.LSemanticEnv
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.LatexSemanticsEnvLookup

object AllPredefinedEnvironments : LatexSemanticsEnvLookup {

    val allEnvironments: List<LSemanticEnv> =
        listOf(
            PredefinedEnvBasic
        ).flatMap {
            it.allEnvironments
        }

    val packageToEnvironments: Map<LatexLib, List<LSemanticEnv>> = allEnvironments.groupBy { it.dependency }

    val simpleNameLookup = allEnvironments.associateBy { it.name }

    val nameToEnvironments = allEnvironments.groupBy { it.name }

    override fun lookupEnv(name: String): LSemanticEnv? {
        return simpleNameLookup[name]
    }

    fun findAll(name: String): List<LSemanticEnv> {
        return nameToEnvironments[name] ?: emptyList()
    }
}