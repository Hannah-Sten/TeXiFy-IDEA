package nl.hannahsten.texifyidea.run.latex.ui

import com.intellij.diagnostic.logging.AdditionalTabComponent
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.ui.components.Label
import java.awt.BorderLayout
import javax.swing.JComponent

/**
 * Runner tab component displaying LaTeX log messages in a more readable and navigatable format.
 *
 * @author Sten Wessel
 */
class LatexLogTabComponent : AdditionalTabComponent(BorderLayout()) {

    init {
        add(Label("Hallo!"), BorderLayout.CENTER)
    }

    override fun getTabTitle() = "Log messages"

    override fun dispose() {

    }

    override fun getPreferredFocusableComponent() = component

    override fun getToolbarActions(): ActionGroup? = null

    override fun getToolbarContextComponent(): JComponent? = null

    override fun getToolbarPlace(): String? = null

    override fun getSearchComponent(): JComponent? = null

    override fun isContentBuiltIn() = false

}
