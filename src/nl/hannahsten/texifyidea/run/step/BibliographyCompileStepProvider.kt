package nl.hannahsten.texifyidea.run.step

import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.legacy.bibtex.BibtexRunConfiguration

object BibliographyCompileStepProvider : CompileStepProvider {

    override val name = "Bibliography"

    override val icon = TexifyIcons.BUILD_BIB

    override val id = "bibliography"

    override fun createStep(configuration: LatexRunConfiguration) = BibliographyCompileStep(this, configuration)

    // todo step creation
    fun createStepIfNeeded() {
//        if (!runConfig.getConfigOptions().hasBeenRun) {
//            // Only at this moment we know the user really wants to run the run configuration, so only now we do the expensive check of
//            // checking for bibliography commands
//            if (runConfig.bibRunConfigs.isEmpty() && !compiler.includesBibtex) {
//                runConfig.generateBibRunConfig()
//
//                runConfig.bibRunConfigs.forEach {
//                    val bibSettings = it
//
//                    // Pass necessary latex run configurations settings to the bibtex run configuration.
//                    (bibSettings.configuration as? BibtexRunConfiguration)?.apply {
//                        // Check if the aux, out, or src folder should be used as bib working dir.
//                        this.bibWorkingDir = runConfig.getAuxilDirectory()
//                    }
//                }
//            }
//        }
    }
}