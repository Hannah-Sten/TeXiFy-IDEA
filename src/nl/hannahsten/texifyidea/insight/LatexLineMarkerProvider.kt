package nl.hannahsten.texifyidea.insight

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.impl.LineMarkersPass
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.Magic

/**
 * Provides line markers for the LaTeX language.
 *
 * @author Hannah Schellekens, Sten Wessel
 */
class LatexLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        // Method separators before sectioning commands
        if (!DaemonCodeAnalyzerSettings.getInstance().SHOW_METHOD_SEPARATORS || element !is LatexCommands) return null

        val colourManager = EditorColorsManager.getInstance()
        val commandToken = element.commandToken.text
        return if (commandToken in Magic.Command.sectionMarkers) {
            LineMarkersPass.createMethodSeparatorLineMarker(element.commandToken, colourManager).apply {
                separatorColor = Magic.Command.sectionSeparatorColors[commandToken]
            }
        }
        else null
    }

    override fun collectSlowLineMarkers(
            elements: MutableList<PsiElement>,
            result: MutableCollection<LineMarkerInfo<PsiElement>>
    ) = Unit
}