package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.execution.ui.RunConfigurationEditorFragment
import com.intellij.util.ui.JBUI
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

internal class LatexCompileSequenceFragment(
    private val component: LatexCompileSequenceComponent,
) : RunConfigurationEditorFragment<LatexRunConfiguration, JComponent>(
    "compileSequence",
    "Compile sequence",
    null,
    wrap(component),
    0,
    { true }
) {

    init {
        component.changeListener = { fireEditorStateChanged() }
        isRemovable = false
        actionHint = "Add, remove, or reorder compile steps"
    }

    override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
        component.resetEditorFrom()
    }

    override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
        component.applyEditorTo()
    }

    companion object {

        internal fun createWrappedComponent(component: LatexCompileSequenceComponent): JComponent = wrap(component)

        private fun wrap(component: LatexCompileSequenceComponent): JComponent {
            val tooltip = "Drag to reorder. Double-click a step to change its type."
            val panel = JPanel(BorderLayout())
            panel.toolTipText = tooltip

            val label = JLabel("Compile sequence").apply {
                font = JBUI.Fonts.label().deriveFont(Font.BOLD)
                toolTipText = tooltip
            }
            val header = JPanel(BorderLayout()).apply {
                add(label, BorderLayout.WEST)
                add(
                    JPanel(BorderLayout()).apply {
                        isOpaque = false
                        border = JBUI.Borders.emptyLeft(12)
                        add(component.headerActionComponent(), BorderLayout.EAST)
                    },
                    BorderLayout.EAST,
                )
            }
            component.toolTipText = tooltip
            component.headerActionComponent().toolTipText = tooltip

            panel.add(header, BorderLayout.NORTH)
            panel.add(component, BorderLayout.CENTER)

            return panel
        }
    }
}
