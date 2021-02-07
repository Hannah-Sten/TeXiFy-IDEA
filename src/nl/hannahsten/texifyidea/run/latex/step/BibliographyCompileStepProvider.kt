package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.util.xmlb.annotations.Attribute
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.run.compiler.BibliographyCompiler
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration


object BibliographyCompileStepProvider : LatexCompileStepProvider {

    override val name = "Bibliography"

    override val icon = TexifyIcons.BUILD_BIB

    override fun createStep(configuration: LatexRunConfiguration) = BibliographyCompileStep(this, configuration)
}

class BibliographyCompileStep(
    override val provider: LatexCompileStepProvider, override val configuration: LatexRunConfiguration
) : LatexCompileStep, PersistentStateComponent<BibliographyCompileStep.State> {

    class State : BaseState() {

        @get:Attribute("compiler", converter = BibliographyCompiler.Converter::class)
        var compiler by property<BibliographyCompiler?>(null) { it == null }

        @get:Attribute()
        var mainFile by string()

        @get:Attribute()
        var workingDir by string()
    }

    private var state = State()


    override fun configure(): Boolean {
        TODO("Not yet implemented")
    }

    override fun execute() {
        TODO("Not yet implemented")
    }

    override fun getState() = state

    override fun loadState(state: State) {
        state.resetModificationCount()
        this.state = state
    }
}
