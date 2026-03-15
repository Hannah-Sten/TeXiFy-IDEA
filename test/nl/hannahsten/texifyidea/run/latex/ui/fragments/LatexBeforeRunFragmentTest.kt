package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.components.ActionLink
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latex.ui.LatexSettingsEditor
import java.awt.Component
import java.awt.Container

class LatexBeforeRunFragmentTest : BasePlatformTestCase() {

    fun testEditorShowsAddBeforeLaunchTaskLink() {
        val factory = LatexRunConfigurationProducer().configurationFactory
        val runConfig = LatexRunConfiguration(project, factory, "run config")
        val editor = LatexSettingsEditor(runConfig)
        val settings = RunManagerImpl.getInstanceImpl(project)
            .createConfiguration(runConfig, factory) as RunnerAndConfigurationSettingsImpl

        editor.resetEditorFrom(settings)

        val links = findComponentsOfType(editor.component, ActionLink::class.java)

        assertTrue(links.any { it.text == "Add task" })
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
