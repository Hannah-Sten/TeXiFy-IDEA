package nl.hannahsten.texifyidea.settings.conventions

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.settings.TexifyConventionsScheme
import nl.hannahsten.texifyidea.settings.TexifyConventionsSchemesPanel
import nl.hannahsten.texifyidea.settings.TexifyConventionsSettings
import java.awt.*
import java.text.DecimalFormatSymbols
import java.util.*
import javax.swing.*

abstract class JNumberSpinner<T>(value: T, minValue: T, maxValue: T, stepSize: T, description: String? = null) :
    JSpinner(SpinnerNumberModel(value, minValue, maxValue, stepSize)) where T : Number, T : Comparable<T> {
    /**
     * Transforms a [Number] into a [T].
     */
    abstract val numberToT: (Number) -> T

    /**
     * The description to use in error messages.
     */
    private val description = description ?: DEFAULT_DESCRIPTION

    /**
     * A helper function to return the super class's model as an instance of [SpinnerNumberModel].
     */
    private val numberModel: SpinnerNumberModel
        get() = super.getModel() as SpinnerNumberModel

    /**
     * The minimal allowed value.
     */
    var minValue: T
        get() = numberToT(numberModel.minimum as Number)
        set(value) {
            numberModel.minimum = value
        }

    /**
     * The maximal allowed value.
     */
    var maxValue: T
        get() = numberToT(numberModel.maximum as Number)
        set(value) {
            numberModel.maximum = value
        }


    /**
     * Returns the current value of the spinner.
     *
     * @return the current value of the spinner
     */
    override fun getValue(): T = numberToT(numberModel.number)

    /**
     * Sets the value of the spinner.
     *
     * @param value the new value of the spinner
     */
    override fun setValue(value: Any) {
        numberModel.value = value
    }

    companion object {
        /**
         * The default description to use in error messages.
         */
        const val DEFAULT_DESCRIPTION = "value"

        /**
         * The default width of a number spinner.
         */
        const val DEFAULT_WIDTH = 52

        /**
         * The default number format used to display numbers.
         */
        val DEFAULT_FORMAT = DecimalFormatSymbols(Locale.US)
    }
}

class JLongSpinner(
    value: Long = 0L,
    minValue: Long = Long.MIN_VALUE,
    maxValue: Long = Long.MAX_VALUE,
    stepSize: Long = 1L,
    description: String? = null
) : JNumberSpinner<Long>(value, minValue, maxValue, stepSize, description) {
    override val numberToT: (Number) -> Long
        get() = { it.toLong() }


    init {
        this.editor = NumberEditor(this).also { it.format.decimalFormatSymbols = DEFAULT_FORMAT }
        this.minimumSize = Dimension(DEFAULT_WIDTH, minimumSize.height)
        this.preferredSize = Dimension(DEFAULT_WIDTH, preferredSize.height)
    }
}


class TexifyConventionsConfigurable(project: Project) : SearchableConfigurable, Configurable.VariableProjectAppLevel {
    private val settings: TexifyConventionsSettings = TexifyConventionsSettings.getInstance(project)
    private val unsavedSettings: TexifyConventionsSettings = TexifyConventionsSettings.getInstance(project).deepCopy()
    private lateinit var schemesPanel: TexifyConventionsSchemesPanel
    private lateinit var mainPanel: JPanel
    private lateinit var maxSectionSize: JLongSpinner


    override fun createComponent(): JComponent? {

        schemesPanel = TexifyConventionsSchemesPanel(unsavedSettings)
        schemesPanel.addListener(object : TexifyConventionsSchemesPanel.Listener<TexifyConventionsScheme> {
            override fun onCurrentSchemeWillChange(scheme: TexifyConventionsScheme) {
                saveScheme(scheme)
            }

            override fun onCurrentSchemeHasChanged(scheme: TexifyConventionsScheme) {
                loadScheme(scheme)
            }

        })

        val centerPanel = JPanel(FlowLayout()).apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            maxSectionSize = JLongSpinner(minValue = 0, stepSize = 10)
            maxSectionSize.alignmentX = JSpinner.CENTER_ALIGNMENT
            add(JPanel(GridBagLayout()).apply {
                add(JLabel("Maximum section size (characters)"), GridBagConstraints().apply {
                    gridx = 0;
                    gridy = 0
                    gridwidth = 1;
                    gridheight = 1
                })
                add(
                    maxSectionSize,
                    GridBagConstraints().apply {
                        gridx = 1;
                        gridy = 0;
                        fill = GridBagConstraints.HORIZONTAL;
                        gridwidth = 2;
                        gridheight = 1
                        weightx = 100.0;
                        weighty = 100.0;
                    })
            })
        }

        mainPanel = JPanel(BorderLayout())
        mainPanel.add(schemesPanel, BorderLayout.NORTH)
        mainPanel.add(centerPanel, BorderLayout.CENTER)
        loadSettings(settings)
        return mainPanel
    }

    fun loadScheme(scheme: TexifyConventionsScheme) {
        maxSectionSize.value = scheme.maxSectionSize
    }

    fun saveScheme(scheme: TexifyConventionsScheme) {
        scheme.maxSectionSize = maxSectionSize.value
    }

    fun loadSettings(settings: TexifyConventionsSettings) {
        unsavedSettings.loadState(settings.deepCopy())
        loadScheme(unsavedSettings.currentScheme)
        schemesPanel.updateComboBoxList()
    }

    private fun saveSettings(settings: TexifyConventionsSettings) {
        saveScheme(unsavedSettings.currentScheme)
        settings.loadState(unsavedSettings.deepCopy())
    }

    override fun isModified(): Boolean = settings.deepCopy().also { saveSettings(it) } != settings

    override fun reset() = loadSettings(settings)

    override fun apply() {
        saveSettings(settings)
    }

    override fun getDisplayName() = "Conventions"

    override fun getId() = "TexifyConventionsConfigurable"

    override fun isProjectLevel(): Boolean =
        ::schemesPanel.isInitialized && schemesPanel.model.settings.currentScheme.isProjectScheme()

}