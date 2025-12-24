package nl.hannahsten.texifyidea.settings.conventions

import com.intellij.application.options.schemes.AbstractSchemeActions
import com.intellij.application.options.schemes.SchemesModel
import com.intellij.application.options.schemes.SimpleSchemesPanel
import javax.naming.OperationNotSupportedException

/**
 *  Controller for managing the convention settings. The panel allows selecting and editing the current scheme and copying settings
 *  between the schemes. The supplied [TexifyConventionsSettings] instance represents the corresponding model,
 *  containing the available schemes.
 *
 *  @see TexifyConventionsScheme
 *  @see TexifyConventionsSettings
 */
internal class TexifyConventionsSchemesPanel(val settings: TexifyConventionsSettings) :
    SimpleSchemesPanel<TexifyConventionsScheme>(),
    SchemesModel<TexifyConventionsScheme> {

    val type: Class<TexifyConventionsScheme>
        get() = TexifyConventionsScheme::class.java

    /**
     * Listeners for changes in the scheme selection.
     */
    private val listeners = mutableListOf<Listener>()

    /**
     * Actions that can be performed with this panel.
     */
    val actions = createSchemeActions()

    /**
     * Registers a listener that will be informed whenever the scheme selection changed.
     *
     * @param listener the listener that listens to scheme-change events
     */
    fun addListener(listener: Listener) = listeners.add(listener)

    /**
     * Forcefully updates the combo box so that its entries and the current selection reflect the `settings` instance.
     *
     * This panel instance will update the combo box by itself. Call this method if the `settings` instance has been
     * changed externally and these changes need to be reflected.
     */
    fun updateComboBoxList() {
        settings.currentScheme.also { currentScheme ->
            resetSchemes(settings.schemes)
            selectScheme(currentScheme)
        }
    }

    /**
     * Returns true if a scheme with the given name is present in this panel.
     *
     * @param name the name to check for
     * @param projectScheme ignored
     * @return true if a scheme with the given name is present in this panel
     */
    override fun containsScheme(name: String, projectScheme: Boolean) = settings.schemes.any { it.name == name }

    /**
     * Returns an object with a number of actions that can be performed on this panel.
     *
     * @return an object with a number of actions that can be performed on this panel
     */
    override fun createSchemeActions() = SchemeActions()

    override fun getModel() = this

    override fun supportsProjectSchemes() = true

    override fun highlightNonDefaultSchemes() = true

    override fun useBoldForNonRemovableSchemes() = true

    override fun isProjectScheme(scheme: TexifyConventionsScheme) = scheme.isProjectScheme

    override fun canDeleteScheme(scheme: TexifyConventionsScheme) = false

    override fun canDuplicateScheme(scheme: TexifyConventionsScheme) = false

    override fun canRenameScheme(scheme: TexifyConventionsScheme) = false

    override fun canResetScheme(scheme: TexifyConventionsScheme) = true

    override fun differsFromDefault(scheme: TexifyConventionsScheme): Boolean {
        // schemes differ if any setting except the name is different
        return scheme.deepCopy() != TexifyConventionsScheme(myName = scheme.myName)
    }

    override fun removeScheme(scheme: TexifyConventionsScheme): Unit = throw OperationNotSupportedException()

    /**
     * The actions that can be performed with this panel.
     */
    inner class SchemeActions : AbstractSchemeActions<TexifyConventionsScheme>(this) {

        /**
         * Called when the user changes the scheme using the combo box.
         *
         * @param scheme the scheme that has become the selected scheme
         */
        override fun onSchemeChanged(scheme: TexifyConventionsScheme?) {
            if (scheme == null)
                return

            listeners.forEach { it.onCurrentSchemeWillChange(settings.currentScheme) }
            settings.currentScheme = scheme
            listeners.forEach { it.onCurrentSchemeHasChanged(scheme) }
        }

        override fun copyToIDE(scheme: TexifyConventionsScheme) {
            settings.copyToDefaultScheme(scheme)
        }

        override fun copyToProject(scheme: TexifyConventionsScheme) {
            settings.copyToProjectScheme(scheme)
        }

        override fun getSchemeType() = type

        override fun resetScheme(scheme: TexifyConventionsScheme) {
            scheme.copyFrom(TexifyConventionsScheme())
            listeners.forEach { it.onCurrentSchemeHasChanged(scheme) }
        }

        /**
         * Duplicates the currently active scheme.
         *
         * This method is useful only if there can be other schemes besides the projet and the global default scheme,
         * which is currently not supported.
         */
        override fun duplicateScheme(scheme: TexifyConventionsScheme, newName: String): Unit = throw OperationNotSupportedException()

        /**
         * Renames the currently active scheme.
         *
         * This method is useful only if there can be other schemes besides the projet and the global default scheme,
         * which is currently not supported.
         */

        override fun renameScheme(scheme: TexifyConventionsScheme, newName: String): Unit = throw OperationNotSupportedException()
    }

    /**
     * A listener that listens to events that occur to this panel.
     */
    interface Listener {

        /**
         * Invoked when the currently-selected scheme is about to change.
         *
         * @param scheme the scheme that is about to be replaced in favor of another scheme
         */
        fun onCurrentSchemeWillChange(scheme: TexifyConventionsScheme)

        /**
         * Invoked when the currently-selected scheme has just been changed.
         *
         * @param scheme the scheme that has become the currently-selected scheme
         */
        fun onCurrentSchemeHasChanged(scheme: TexifyConventionsScheme)
    }
}