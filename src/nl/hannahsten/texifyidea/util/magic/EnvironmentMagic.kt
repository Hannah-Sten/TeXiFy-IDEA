package nl.hannahsten.texifyidea.util.magic
import nl.hannahsten.texifyidea.lang.DefaultEnvironment.*

object EnvironmentMagic {

    val listingEnvironments = hashSetOf(ITEMIZE, ENUMERATE, DESCRIPTION).map { it.environmentName }

    val tableEnvironments = hashSetOf(TABULAR, TABULAR_STAR, TABULARX, ARRAY, LONGTABLE, TABU).map { it.environmentName }


}