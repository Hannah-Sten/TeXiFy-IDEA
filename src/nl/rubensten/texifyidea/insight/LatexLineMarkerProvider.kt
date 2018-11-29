package nl.rubensten.texifyidea.insight

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.impl.LineMarkersPass
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.psi.PsiElement
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.Magic

/**
 * Provides line markers for the LaTeX language.
 *
 * @author Sten Wessel
 */
class LatexLineMarkerProvider(private val daemonSettings: DaemonCodeAnalyzerSettings,
                              private val colorsManager: EditorColorsManager) : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        // Method separators before sectioning commands
        if (daemonSettings.SHOW_METHOD_SEPARATORS && element is LatexCommands) {
            val commandToken = element.commandToken.text

            if (commandToken in Magic.Command.sectionMarkers) {
                return LineMarkersPass.createMethodSeparatorLineMarker(element, colorsManager)
            }
        }

        return null
    }

    override fun collectSlowLineMarkers(elements: MutableList<PsiElement>,
                                        result: MutableCollection<LineMarkerInfo<PsiElement>>) {

    }
}
