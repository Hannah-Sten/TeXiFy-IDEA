package nl.hannahsten.texifyidea.action.wizard.graphic

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.components.panels.HorizontalLayout
import nl.hannahsten.texifyidea.lang.graphic.CaptionLocation
import nl.hannahsten.texifyidea.lang.graphic.FigureLocation
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.addTextChangeListener
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * @author Hannah Schellekens
 */
open class InsertGraphicWizardDialogWrapper(val initialFilePath: String = "") : DialogWrapper(true) {

    companion object {

        /**
         * The amount of pixels between the left pane border and the contorls.
         */
        private const val CONTROL_LEFT_PADDING = 16
    }

    /**
     * Stores the path to the graphics file.
     */
    private val txtGraphicFile = TextFieldWithBrowseButton().apply {
        text = initialFilePath
        addBrowseFolderListener(TextBrowseFolderListener(
                FileChooserDescriptor(true, false, false, false, false, false)
                        .withFileFilter { vf -> vf.extension?.toLowerCase() in Magic.File.graphicFileExtensions }
                        .withTitle("Select graphics file...")
        ))
    }

    /**
     * The width option for the graphic. Not necessarily a number. Whatever is in here will get put
     * in the optional parameters.
     */
    private val txtWidth = JBTextField("", 8).apply {
        addTextChangeListener {
            updateGraphicsOptions("width")
        }
    }

    /**
     * The height option for the graphic. Not necessarily a number. Whatever is in here will get put
     * in the optional parameters.
     */
    private val txtHeight = JBTextField("", 8).apply {
        addTextChangeListener {
            updateGraphicsOptions("height")
        }
    }

    /**
     * The angle option for the graphic. When empty, no angle. Not necessarily a number.
     */
    private val txtAngle = JBTextField("").apply {
        toolTipText = "Degrees, anticlockwise"
        addTextChangeListener {
            updateGraphicsOptions("angle")
        }
    }

    /**
     * The custom graphics options. This is basically the optional parameter of \includegraphics.
     * Can get modified when the width/height/angle get modified.
     */
    private val txtCustomOptions = JBTextField("")

    /**
     * Whether the image must be centered horizontally.
     */
    private val checkCenterHorizontally = JBCheckBox("Center horizontally", true)

    /**
     * Whether to place the included graphic inside a figure environment.
     */
    private val checkPlaceInFigure = JBCheckBox("Place in figure environment", true).apply {
        addItemListener {
            updateFigureControlsState()
        }
    }

    /**
     * Where to put the caption in the figure environment.
     */
    private val cboxCaptionLocation = ComboBox(CaptionLocation.values()).apply {
        selectedItem = CaptionLocation.BELOW_GRAPHIC
    }

    /**
     * Stores the short caption for the figure environment.
     */
    private val txtShortCaption = JBTextField()

    /**
     * Stores the long caption for the figure environment.
     */
    private val txtLongCaption = ExpandableTextField()

    /**
     * Contains the label for the figure.
     */
    private val txtLabel = JBTextField("fig:")

    /**
     * Place figure on top position.
     */
    private val checkTop = JBCheckBox("top")

    /**
     * Place figure on bottom position.
     */
    private val checkBottom = JBCheckBox("bottom")

    /**
     * Place figure on page position.
     */
    private val checkPage = JBCheckBox("page")

    /**
     * Place figure here.
     */
    private val checkHere = JBCheckBox("here")

    /**
     * Place figure strictly here.
     */
    private val checkStrictHere = JBCheckBox("strict here")

    init {
        super.init()
        title = "Insert graphic"
    }

    /**
     * Get the data that has been entered into the UI.
     */
    fun extractData() = InsertGraphicData(
            filePath = txtGraphicFile.text.trim(),
            options = txtCustomOptions.text.trim(),
            center = checkCenterHorizontally.isSelected,
            placeInFigure = checkPlaceInFigure.isSelected,
            captionLocation = whenFigure(cboxCaptionLocation.selectedItem as CaptionLocation),
            caption = whenFigure(txtLongCaption.text.trim()),
            shortCaption = whenFigure(txtShortCaption.text.trim()),
            label = whenFigure(txtLabel.text.trim()),
            positions = whenFigure(listOfNotNull(
                    if (checkTop.isSelected) FigureLocation.TOP else null,
                    if (checkBottom.isSelected) FigureLocation.BOTTOM else null,
                    if (checkPage.isSelected) FigureLocation.PAGE else null,
                    if (checkHere.isSelected) FigureLocation.HERE else null,
                    if (checkStrictHere.isSelected) FigureLocation.STRICT_HERE else null
            ))
    )

    /**
     * Only selects the value when [checkPlaceInFigure] is selected.
     */
    private fun <T> whenFigure(value: T): T? = if (checkPlaceInFigure.isSelected) value else null

    override fun createCenterPanel() = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS).apply {
            // Flush left.
            alignmentX = 0.0f
        }

        addFileControls()
        addGraphicOptionControls()
        addLayoutControls()
        addFigureControls()

        add(Box.createRigidArea(Dimension(0, 12)))
    }

    private fun JPanel.addFileControls() {
        addSectionHeader("Graphics file")

        val labelWidth = 64
        addLabeledComponent(txtGraphicFile, "Path:", labelWidth)
    }

    private fun JPanel.addGraphicOptionControls() {
        addSectionHeader("Graphic options")

        val labelWidth = 64
        val optionsPanel = JPanel(HorizontalLayout(0)).apply {
            addLabeledComponent(txtWidth, "Width:", labelWidth)
            addLabeledComponent(txtHeight, "Height:", labelWidth)
            addLabeledComponent(txtAngle, "Angle:", labelWidth)
        }
        add(optionsPanel)
        addLabeledComponent(txtCustomOptions, "Custom:", labelWidth)
    }

    private fun JPanel.addLayoutControls() {
        addSectionHeader("Layout options")

        addLabeledComponent(checkCenterHorizontally, "")
    }

    private fun JPanel.addFigureControls() {
        addSectionHeader("Figure environment")

        val labelWidth = 112
        addLabeledComponent(checkPlaceInFigure, "")
        addLabeledComponent(cboxCaptionLocation, "Caption location:", labelWidth)
        addLabeledComponent(txtLongCaption, "Caption:", labelWidth)
        addLabeledComponent(txtShortCaption, "Short caption:", labelWidth)
        addLabeledComponent(txtLabel, "Label:", labelWidth)

        val positionBoxes = JPanel(HorizontalLayout(8)).apply {
            add(checkTop)
            add(checkBottom)
            add(checkPage)
            add(checkHere)
            add(checkStrictHere)
        }
        addLabeledComponent(positionBoxes, "Position:", labelWidth)
    }

    private fun JPanel.addSectionHeader(title: String, margin: Int = 8) {
        add(Box.createRigidArea(Dimension(0, margin)))
        add(TitledSeparator(title))
        add(Box.createRigidArea(Dimension(0, margin)))
    }

    /**
     * Adds a component to the panel with a label before it.
     *
     * @param component
     *          The component to add to the panel.
     * @param description
     *          The label to put before the component.
     * @param labelWidth
     *          The fixed label width, or `null` to use the label's inherent size.
     */
    private fun JPanel.addLabeledComponent(component: JComponent, description: String, labelWidth: Int? = null): JPanel {
        // Uses a border layout with West for the label and Center for the control itself.
        // East is reserved for suffix elements.
        val pane = JPanel(BorderLayout()).apply {
            val label = JBLabel(description).apply {
                // Left padding.
                border = EmptyBorder(0, CONTROL_LEFT_PADDING, 0, 0)

                // Custom width if specified.
                labelWidth?.let {
                    preferredSize = Dimension(it, height)
                }

                // Align top.
                alignmentY = 0.0f
            }
            add(label, BorderLayout.WEST)
            add(component, BorderLayout.CENTER)
        }
        add(pane)
        return pane
    }

    private fun updateFigureControlsState() {
        val enabled = checkPlaceInFigure.isSelected

        cboxCaptionLocation.isEnabled = enabled
        txtLongCaption.isEnabled = enabled
        txtShortCaption.isEnabled = enabled
        txtLabel.isEnabled = enabled
        checkTop.isEnabled = enabled
        checkBottom.isEnabled = enabled
        checkPage.isEnabled = enabled
        checkHere.isEnabled = enabled
        checkStrictHere.isEnabled = enabled
    }

    private fun JTextField.updateGraphicsOptions(fieldName: String) {
        val text = text.replace(",", "")
        // Update
        if (txtCustomOptions.text.contains("$fieldName=")) {
            txtCustomOptions.text = txtCustomOptions.text
                    .replace(Regex("$fieldName=[^,]*"), Regex.escapeReplacement("$fieldName=$text"))
        }
        // Nothing yet, set width property.
        else if (txtCustomOptions.text.isBlank()) {
            txtCustomOptions.text = "$fieldName=$text"
        }
        // When there is something, append width property.
        else {
            txtCustomOptions.text = txtCustomOptions.text + ",$fieldName=$text"
        }
    }
}