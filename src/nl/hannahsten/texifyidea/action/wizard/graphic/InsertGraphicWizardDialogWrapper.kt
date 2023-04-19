package nl.hannahsten.texifyidea.action.wizard.graphic

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.components.panels.HorizontalLayout
import nl.hannahsten.texifyidea.lang.graphic.CaptionLocation
import nl.hannahsten.texifyidea.lang.graphic.FigureLocation
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.magic.FileMagic
import java.awt.Dimension
import java.io.File
import java.util.*
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * @author Hannah Schellekens
 */
open class InsertGraphicWizardDialogWrapper(val initialFilePath: String = "") : DialogWrapper(true) {

    /**
     * Stores the path to the graphics file.
     */
    private val txtGraphicFile = TextFieldWithBrowseButton().apply {
        text = initialFilePath
        addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(true, false, false, false, false, false)
                    .withFileFilter { vf -> vf.extension?.lowercase(Locale.getDefault()) in FileMagic.graphicFileExtensions }
                    .withTitle("Select Graphics File...")
            )
        )
    }

    /**
     * Insert a relative file path if checked, absolute if unchecked.
     */
    private val checkRelativePath = JBCheckBox("Convert to relative path", true)

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
    private val txtLongCaption = ExpandableTextField(
        { it.split("\n") },
        { it.joinToString("\n") }
    )

    /**
     * Contains the label for the figure.
     */
    private val txtLabel = JBTextField(("fig:" + File(initialFilePath).nameWithoutExtension).formatAsLabel())

    /**
     * Contains the positioning symbols.
     */
    private val txtPosition = JBTextField("").apply {
        setInputFilter(FigureLocation.ALL_SYMBOLS.toSet())
        addKeyReleasedListener { updatePositionCheckmarks() }
    }

    /**
     * The check boxes for the figure locations.
     */
    private val checkPosition = FigureLocation.values().asSequence()
        .map { location ->
            location.symbol to JBCheckBox(location.description).apply {
                addActionListener { event ->
                    val source = event.source as? JBCheckBox ?: error("Not a JBCheckBox!")
                    // Add symbol if selected.
                    if (source.isSelected && txtPosition.text.contains(location.symbol).not()) {
                        txtPosition.text = txtPosition.text + location.symbol
                    }
                    // Remove if deselected.
                    else {
                        txtPosition.text = txtPosition.text.replace(location.symbol, "")
                    }
                }
            }
        }
        // Use linked hash map to preserve order.
        .toMap(LinkedHashMap())

    init {
        super.init()
        title = "Insert Graphic"
    }

    /**
     * Get the data that has been entered into the UI.
     */
    fun extractData() = InsertGraphicData(
        filePath = txtGraphicFile.text.trim(),
        relativePath = checkRelativePath.isSelected,
        options = txtCustomOptions.text.trim(),
        center = checkCenterHorizontally.isSelected,
        placeInFigure = checkPlaceInFigure.isSelected,
        captionLocation = whenFigure { cboxCaptionLocation.selectedItem as CaptionLocation },
        caption = whenFigure { txtLongCaption.text.trim() },
        shortCaption = whenFigure { txtShortCaption.text.trim() },
        label = whenFigure { txtLabel.text.trim() },
        positions = whenFigure {
            txtPosition.text.trim().mapNotNull { FigureLocation.bySymbol(it.toString()) }
        }
    )

    /**
     * Only selects the value when [checkPlaceInFigure] is selected.
     */
    private fun <T> whenFigure(value: () -> T): T? = if (checkPlaceInFigure.isSelected) value() else null

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
        addLabeledComponent(checkRelativePath, "", labelWidth)
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
        addLabeledComponent(txtPosition, "Position:", labelWidth)

        val positionBoxes = JPanel(HorizontalLayout(8)).apply {
            checkPosition.values.forEach {
                add(it)
            }
        }
        addLabeledComponent(positionBoxes, "", labelWidth)
    }

    private fun JPanel.addSectionHeader(title: String, margin: Int = 8) {
        add(Box.createRigidArea(Dimension(0, margin)))
        add(TitledSeparator(title))
        add(Box.createRigidArea(Dimension(0, margin)))
    }

    private fun updateFigureControlsState() {
        val enabled = checkPlaceInFigure.isSelected

        cboxCaptionLocation.isEnabled = enabled
        txtLongCaption.isEnabled = enabled
        txtShortCaption.isEnabled = enabled
        txtLabel.isEnabled = enabled
        checkPosition.values.forEach {
            it.isEnabled = enabled
        }
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

    private fun updatePositionCheckmarks() {
        val positionText = txtPosition.text
        checkPosition.forEach { (symbol, checkbox) ->
            checkbox.isSelected = symbol in positionText
        }
    }
}