package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.execution.ui.RunConfigurationEditorFragment
import com.intellij.ide.setToolTipText
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.util.ui.JBUI
import nl.hannahsten.texifyidea.TexifyBundle
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
    TexifyBundle.message("run.step.ui.compile.sequence.title"),
    null,
    wrap(component),
    0,
    { true }
) {

    init {
        component.changeListener = { fireEditorStateChanged() }
        isRemovable = false
        actionHint = TexifyBundle.message("run.step.ui.compile.sequence.action.hint")
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
            val tooltip = TexifyBundle.message("run.step.ui.compile.sequence.tooltip")
            val panel = JPanel(BorderLayout())
            panel.setToolTipText(HtmlChunk.text(tooltip))
            val headerAction = component.headerActionComponent()

            val label = JLabel(TexifyBundle.message("run.step.ui.compile.sequence.title")).apply {
                font = JBUI.Fonts.label().deriveFont(Font.BOLD)
                setToolTipText(HtmlChunk.text(tooltip))
            }
            val header = JPanel(BorderLayout()).apply {
                add(label, BorderLayout.WEST)
                add(
                    JPanel(BorderLayout()).apply {
                        isOpaque = false
                        border = JBUI.Borders.emptyLeft(12)
                        add(headerAction, BorderLayout.EAST)
                    },
                    BorderLayout.EAST,
                )
            }
            component.setToolTipText(HtmlChunk.text(tooltip))
            if (headerAction.toolTipText.isNullOrBlank()) {
                headerAction.setToolTipText(HtmlChunk.text(tooltip))
            }

            panel.add(header, BorderLayout.NORTH)
            panel.add(component, BorderLayout.CENTER)

            return panel
        }
    }
}
