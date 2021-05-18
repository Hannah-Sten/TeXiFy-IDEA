package nl.hannahsten.texifyidea.settings.conventions

import java.awt.Dimension
import java.text.DecimalFormatSymbols
import java.util.*
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

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