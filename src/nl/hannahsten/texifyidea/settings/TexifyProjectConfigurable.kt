package nl.hannahsten.texifyidea.settings

import com.intellij.openapi.options.SearchableConfigurable
import javax.swing.JComponent

/**
 * Project-level settings UI.
 *
 * The project settings are a property so they will differ per project.
 *
 * Note: We don't currently use this, but keeping it so we don't have to figure it out all over again
 */
class TexifyProjectConfigurable : SearchableConfigurable {

    private val projectSettings: TexifyProjectSettings = TexifyProjectSettings()

    override fun getId() = "TexifyProjectConfigurable"

    override fun getDisplayName() = "Project Settings"

    override fun createComponent(): JComponent? = null

    override fun isModified(): Boolean = false

    override fun apply() {}
}