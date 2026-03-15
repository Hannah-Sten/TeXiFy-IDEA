package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.FragmentedSettings
import com.intellij.execution.ui.FragmentedSettingsBuilder
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.components.DropDownLink
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Step cards are nested fragmented editors, so the platform's default header would introduce a second top-level
 * Modify options affordance. The nested entry follows Maven/Gradle-style group behavior instead: a focusable dropdown
 * link with no extra mnemonic shortcut or field mnemonic of its own, which avoids macOS conflicts and keeps keyboard
 * access on the platform-standard Tab/Enter/Space/Down path.
 */
internal class LatexStepFragmentedSettingsBuilder<Settings : FragmentedSettings>(
    fragments: Collection<SettingsEditorFragment<Settings, *>>,
    disposable: Disposable,
) : FragmentedSettingsBuilder<Settings>(fragments, null, disposable) {

    companion object {

        internal const val STEP_OPTIONS_HEADER_NAME = "latex.step.settings.header"
        internal const val STEP_OPTIONS_LINK_NAME = "latex.step.settings.optionsLink"

        private const val STEP_OPTIONS_TITLE = "Step options"
    }

    private val stepFragments = fragments.toList()
    private val optionFragments: List<SettingsEditorFragment<Settings, *>>
        get() = stepFragments.filter { it.isRemovable && !it.isHeader }

    private var pendingFocusTarget: JComponent? = null

    override fun addHeader(fragment: SettingsEditorFragment<Settings, *>?) {
        val hasStepOptions = optionFragments.isNotEmpty()
        val headerPanel = JPanel(BorderLayout()).apply {
            name = STEP_OPTIONS_HEADER_NAME
            border = JBUI.Borders.empty(5, 0)
        }

        fragment?.component?.let { headerPanel.add(it, BorderLayout.WEST) }
        createHeaderSeparator()?.let { headerPanel.add(it, BorderLayout.CENTER) }

        val stepOptionsLink = DropDownLink(STEP_OPTIONS_TITLE) { link ->
            createStepOptionsPopup(link)
        }.apply {
            name = STEP_OPTIONS_LINK_NAME
            isEnabled = hasStepOptions
        }

        val actionsPanel = JPanel(BorderLayout()).apply {
            isOpaque = false
            add(stepOptionsLink, BorderLayout.CENTER)
        }
        headerPanel.add(actionsPanel, BorderLayout.EAST)

        addLine(headerPanel, 6, 0, 0)
    }

    private fun createStepOptionsPopup(anchor: JComponent): JBPopup {
        if (optionFragments.isEmpty()) {
            return JBPopupFactory.getInstance().createMessage("No additional step options.")
        }

        pendingFocusTarget = null
        val group = DefaultActionGroup().apply {
            optionFragments.forEach { add(StepOptionToggleAction(it)) }
        }

        return JBPopupFactory.getInstance().createActionGroupPopup(
            STEP_OPTIONS_TITLE,
            group,
            com.intellij.ide.DataManager.getInstance().getDataContext(anchor),
            JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
            false,
            { focusPendingTarget() },
            -1
        )
    }

    private fun focusPendingTarget() {
        val target = pendingFocusTarget ?: return
        pendingFocusTarget = null
        IdeFocusManager.findInstanceByComponent(target).requestFocus(target, true)
    }

    private inner class StepOptionToggleAction(
        private val fragment: SettingsEditorFragment<Settings, *>,
    ) : ToggleAction(fragment.name, fragment.actionDescription ?: fragment.actionHint, null) {

        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

        override fun isSelected(event: AnActionEvent): Boolean = fragment.isSelected

        override fun setSelected(event: AnActionEvent, state: Boolean) {
            fragment.toggle(state, event)
            pendingFocusTarget = fragment.editorComponent.takeIf { state }
        }

        override fun update(event: AnActionEvent) {
            super.update(event)
            event.presentation.description = fragment.actionDescription ?: fragment.actionHint
        }
    }
}
