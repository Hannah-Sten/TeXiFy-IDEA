package nl.hannahsten.texifyidea.settings

import com.intellij.application.options.schemes.AbstractSchemeActions
import com.intellij.application.options.schemes.SchemesModel
import com.intellij.application.options.schemes.SimpleSchemesPanel
import javax.naming.OperationNotSupportedException

class TexifyConventionsSchemesPanel(val settings: TexifyConventionsSettings) :
    SimpleSchemesPanel<TexifyConventionsScheme>(),
    SchemesModel<TexifyConventionsScheme> {

    val type: Class<TexifyConventionsScheme>
        get() = TexifyConventionsScheme::class.java

    /**
     * Listeners for changes in the scheme selection.
     */
    private val listeners = mutableListOf<Listener<TexifyConventionsScheme>>()

    /**
     * Actions that can be performed with this panel.
     */
    val actions = createSchemeActions()

    /**
     * Registers a listener that will be informed whenever the scheme selection changed.
     *
     * @param listener the listener that listens to scheme-change events
     */
    fun addListener(listener: Listener<TexifyConventionsScheme>) = listeners.add(listener)

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

    /**
     * Returns this panel, because this panel also functions as the model.
     *
     * @return this panel, because this panel also functions as the model
     */
    override fun getModel() = this

    /**
     * Returns false because project-specific schemes are not supported.
     *
     * @return false because project-specific schemes are not supported
     */
    override fun supportsProjectSchemes() = true

    /**
     * Returns true so that non-default schemes are highlighted.
     *
     * @return true so that non-default schemes are highlighted
     */
    override fun highlightNonDefaultSchemes() = true

    /**
     * Returns true so that non-removable schemes are highlighted.
     *
     * Since only the default scheme is non-removable, only the default scheme will be bold.
     *
     * @return true so that non-removable schemes are highlighted
     */
    override fun useBoldForNonRemovableSchemes() = true

    /**
     * Returns false because project-specific schemes are not supported.
     *
     * @param scheme ignored
     * @return false because project-specific schemes are not supported
     */
    override fun isProjectScheme(scheme: TexifyConventionsScheme) = scheme.isProjectScheme()

    /**
     * Returns true if the given scheme can be deleted.
     *
     * Because only the default scheme cannot be deleted, this method is equivalent to [differsFromDefault].
     *
     * @param scheme the scheme to check for deletability
     * @return true if the given scheme can be deleted
     */
    override fun canDeleteScheme(scheme: TexifyConventionsScheme) = false

    /**
     * Returns true because all schemes can be duplicated.
     *
     * @param scheme ignored
     * @return true because all schemes can be duplicated
     */
    override fun canDuplicateScheme(scheme: TexifyConventionsScheme) = false

    /**
     * Returns true if the given scheme can be renamed.
     *
     * Because only the default scheme cannot be renamed, this method is equivalent to [differsFromDefault].
     *
     * @param scheme the scheme to check for renamability
     * @return true if the given scheme can be renamed
     */
    override fun canRenameScheme(scheme: TexifyConventionsScheme) = false

    /**
     * Returns true if the given scheme can be reset.
     *
     * Because only the default scheme cannot be reset, this method is equivalent to [differsFromDefault].
     *
     * @param scheme the scheme to check for resetability
     * @return true if the given scheme can be reset
     */
    override fun canResetScheme(scheme: TexifyConventionsScheme) = false
    override fun differsFromDefault(scheme: TexifyConventionsScheme): Boolean {
        throw OperationNotSupportedException()
    }

    override fun removeScheme(scheme: TexifyConventionsScheme) {
        throw OperationNotSupportedException()
    }

    /**
     * The actions that can be performed with this panel.
     */
    inner class SchemeActions : AbstractSchemeActions<TexifyConventionsScheme>(this) {
        /**
         * Called when the user changes the scheme using the combo box.
         *
         * @param scheme the scheme that has become the selected scheme
         */
        public override fun onSchemeChanged(scheme: TexifyConventionsScheme?) {
            if (scheme == null)
                return

            listeners.forEach { it.onCurrentSchemeWillChange(settings.currentScheme) }
            settings.currentScheme = scheme
            listeners.forEach { it.onCurrentSchemeHasChanged(scheme) }
        }

        override fun copyToIDE(scheme: TexifyConventionsScheme) {
            val defaultScheme =
                settings.schemes.firstOrNull() { it.name == TexifyConventionsScheme.DEFAULT_SCHEME_NAME }
                    ?: throw IllegalStateException("IDE scheme does not exist.")
            defaultScheme.copySettingsFrom(scheme)
        }

        override fun copyToProject(scheme: TexifyConventionsScheme) {
            val projectScheme = settings.schemes.firstOrNull() { it.isProjectScheme() }
                ?: throw IllegalStateException("IDE scheme does not exist.")
            projectScheme.copySettingsFrom(scheme)
        }

        /**
         * Returns the type of scheme actions apply to.
         *
         * @return the type of scheme actions apply to
         */
        override fun getSchemeType() = type
        override fun resetScheme(scheme: TexifyConventionsScheme) {
            throw OperationNotSupportedException()
        }

        override fun duplicateScheme(scheme: TexifyConventionsScheme, newName: String) {
            throw OperationNotSupportedException()
        }

        override fun renameScheme(scheme: TexifyConventionsScheme, newName: String) {
            throw OperationNotSupportedException()
        }
    }

    /**
     * A listener that listens to events that occur to this panel.
     *
     * @param T the type of scheme about which events are generated
     */
    interface Listener<T : TexifyConventionsScheme> {
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