package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.components.DropDownLink
import nl.hannahsten.texifyidea.run.latex.LatexCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latex.ui.LatexSettingsEditor
import java.awt.Component
import java.awt.Container
import javax.swing.JComponent
import javax.swing.JLabel

class LatexStepFragmentedSettingsBuilderTest : BasePlatformTestCase() {

    fun testStepEditorUsesCustomStepOptionsDropDownWithoutCustomShortcut() {
        val editor = LatexCompileStepFragmentedEditor(project, LatexCompileStepOptions())
        val component = editor.component

        assertNotNull(findComponentByName(component, LatexStepFragmentedSettingsBuilder.STEP_OPTIONS_HEADER_NAME))
        assertTrue(findComponentByName(component, LatexStepFragmentedSettingsBuilder.STEP_OPTIONS_LINK_NAME) is DropDownLink<*>)
        assertEquals(
            1,
            findComponentsOfType(component, DropDownLink::class.java)
                .count { it.name == LatexStepFragmentedSettingsBuilder.STEP_OPTIONS_LINK_NAME }
        )
        assertTrue(findShortcutLikeLabels(component).isEmpty())
        assertFalse(hasCustomStepShortcutBinding(component))
    }

    fun testTopLevelEditorStillUsesPlatformModifyOptionsHeader() {
        val runConfig = LatexRunConfiguration(project, LatexRunConfigurationProducer().configurationFactory, "run config")
        val editor = LatexSettingsEditor(runConfig)

        assertTrue(
            findComponentsOfType(editor.component, DropDownLink::class.java)
                .any { it.text.contains("Modify options") }
        )
    }

    private fun findComponentByName(root: Component, name: String): JComponent? = findComponentsOfType(root, JComponent::class.java)
        .firstOrNull { it.name == name }

    private fun hasCustomStepShortcutBinding(component: JComponent): Boolean {
        val inputMap = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        val mappedKeys = inputMap.allKeys().orEmpty().mapNotNull { inputMap.get(it) as? String }
        val actionKeys = component.actionMap.allKeys().orEmpty().mapNotNull { it as? String }
        return mappedKeys.any { it.startsWith("latex.step.settings") } || actionKeys.any { it.startsWith("latex.step.settings") }
    }

    private fun findShortcutLikeLabels(root: Component): List<JLabel> = findComponentsOfType(root, JLabel::class.java)
        .filter { label ->
            val text = label.text.orEmpty()
            text.contains("Alt+") || text.contains("Ctrl+Alt") || text.contains("⌥") || text.contains("⌃⌥")
        }

    private fun <T : Component> findComponentsOfType(root: Component, type: Class<T>): List<T> {
        val matches = mutableListOf<T>()

        fun visit(component: Component) {
            if (type.isInstance(component)) {
                matches.add(type.cast(component))
            }
            if (component is Container) {
                component.components.forEach(::visit)
            }
        }

        visit(root)
        return matches
    }
}
