package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.LSemanticEnv

object AllPredefinedEnvironments {

    val allEnvironments: List<LSemanticEnv> =
        listOf(
            PredefinedEnvBasic
        ).flatMap {
            it.allEnvironments
        }

    val packageToEnvironments: Map<String, List<LSemanticEnv>> = allEnvironments.groupBy { it.dependency }

    val simpleNameLookup = allEnvironments.associateBy { it.name }
}