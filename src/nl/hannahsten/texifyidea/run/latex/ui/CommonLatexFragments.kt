package nl.hannahsten.texifyidea.run.latex.ui

import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.ui.*
import com.intellij.ide.macro.MacrosDialog
import com.intellij.openapi.options.SettingsEditor
import com.intellij.ui.RawCommandLineEditor
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import java.util.function.BiConsumer
import java.util.function.Predicate
import javax.swing.JLabel
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1

/**
 * Collection of fragment builders for the run configuration settings UI.
 *
 * @author Sten Wessel
 */
object CommonLatexFragments {

    fun <S : RunConfigurationBase<*>> programArguments(id: String,
                                                       message: String,
                                                       commandLinePosition: Int,
                                                       settingsProperty: (S) -> KMutableProperty0<String?>,
                                                       editorVisible: (S) -> Boolean = { true },
                                                       name: String? = null,
                                                       group: String? = null): SettingsEditorFragment<S, RawCommandLineEditor> {
        val editor = RawCommandLineEditor().apply {
            CommandLinePanel.setMinimumWidth(this, 400)

            editorField.emptyText.text = message
            editorField.accessibleContext.accessibleName = message
            FragmentedSettingsUtil.setupPlaceholderVisibility(editorField)
            MacrosDialog.addMacroSupport(editorField, MacrosDialog.Filters.ALL) { false }

            CommonParameterFragments.setMonospaced(textField)
        }

        val fragment = SettingsEditorFragment(
            id, name, group, editor, commandLinePosition,
            { settings, component -> component.text = settingsProperty(settings).get() },
            { settings, component -> settingsProperty(settings).set(component.text) },
            editorVisible
        )

        fragment.isRemovable = false
        fragment.setEditorGetter { e -> e.editorField }

        return fragment
    }
}