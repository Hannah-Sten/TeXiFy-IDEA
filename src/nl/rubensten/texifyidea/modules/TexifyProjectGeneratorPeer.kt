package nl.rubensten.texifyidea.modules

import com.intellij.platform.ProjectGeneratorPeer
import com.intellij.ui.components.JBLabel
import nl.rubensten.texifyidea.settings.TexifySettings
import org.jetbrains.annotations.NotNull

/**
 * todo ? https://github.com/JuliaEditorSupport/julia-intellij/blob/master/src/org/ice1000/julia/lang/module/ui/JuliaProjectGeneratorPeer.java
 */
abstract class TexifyProjectGeneratorPeer : ProjectGeneratorPeer<TexifySettings> {
    protected abstract val someText: JBLabel
}