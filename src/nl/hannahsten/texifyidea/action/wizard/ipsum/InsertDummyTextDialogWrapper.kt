package nl.hannahsten.texifyidea.action.wizard.ipsum

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.JBIntSpinner
import com.intellij.ui.components.*
import nl.hannahsten.texifyidea.util.addLabeledComponent
import nl.hannahsten.texifyidea.util.hbox
import nl.hannahsten.texifyidea.util.text.Ipsum
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*
import javax.swing.border.EmptyBorder
import kotlin.random.Random

/**
 * @author Hannah Schellekens
 */
open class InsertDummyTextDialogWrapper : DialogWrapper(true) {

    /**
     * What type of blind text to generate.
     */
    private val cboxBlindType = ComboBox(DummyTextData.BlindtextType.values()).apply {
        selectedItem = DummyTextData.BlindtextType.PARAGRAPH
        addItemListener { updateUi() }
    }

    /**
     * The amount of repetitions of blind text.
     */
    private val intBlindRepetitions = JBIntSpinner(1, 1, 99999)

    /**
     * The amount of paragraphs of blind text.
     */
    private val intBlindParagraphs = JBIntSpinner(1, 1, 99999)

    /**
     * The level of the blind itemize/enumerate/description.
     */
    private val intBlindLevel = JBIntSpinner(1, 1, 999)

    /**
     * Panel containing options for the blindtext package.
     */
    private val panelBlindtext = JPanel().apply {
        border = EmptyBorder(8, 0, 8, 16)
        layout = BoxLayout(this, BoxLayout.Y_AXIS).apply {
            // Flush left.
            alignmentX = 0.0f
        }

        val labelWidth = 140
        addLabeledComponent(cboxBlindType, "Type of text:", labelWidth)
        addLabeledComponent(hbox(8, intBlindParagraphs, JLabel("Repetitions:"), intBlindRepetitions), "Paragraphs:", labelWidth)
        addLabeledComponent(intBlindLevel, "List level:", labelWidth)
        add(Box.createRigidArea(Dimension(0, 28)))
    }

    /**
     * Contains the lipsum start paragraph number (1-150).
     */
    private val intLipsumParagraphsMin = JBIntSpinner(1, 1, 150)

    /**
     * Contains the lipsum end paragraph number (1-150).
     */
    private val intLipsumParagraphsMax = JBIntSpinner(7, 1, 150)

    /**
     * Contains the lipsum start sentence number.
     */
    private val intLipsumSentencesMin = JBIntSpinner(1, 1, 999)

    /**
     * Contains the lipsum end sentence number.
     */
    private val intLipsumSentencesMax = JBIntSpinner(999, 1, 999)

    /**
     * How to separate the lipsum paragraphs.
     */
    private val cboxLipsumSeparator = ComboBox(DummyTextData.LipsumParagraphSeparation.values()).apply {
        selectedItem = DummyTextData.LipsumParagraphSeparation.PARAGRAPH
    }

    /**
     * Panel containing options for the lipsum package.
     */
    private val panelLipsum = JPanel().apply {
        border = EmptyBorder(8, 0, 8, 16)
        layout = BoxLayout(this, BoxLayout.Y_AXIS).apply {
            // Flush left.
            alignmentX = 0.0f
        }

        val labelWidth = 192
        addLabeledComponent(hbox(8, intLipsumParagraphsMin, JLabel("to"), intLipsumParagraphsMax), "Paragraph numbers:", labelWidth)
        addLabeledComponent(hbox(8, intLipsumSentencesMin, JLabel("to"), intLipsumSentencesMax), "Sentence numbers:", labelWidth)
        addLabeledComponent(cboxLipsumSeparator, "Paragraph separation:", labelWidth)
        add(Box.createRigidArea(Dimension(0, 28)))
    }

    /**
     * Which dummy text teplate to use.
     */
    private val cboxRawTemplate = ComboBox(Ipsum.values()).apply {
        selectedItem = Ipsum.TEXIFY_IDEA_IPSUM
    }

    /**
     * The minimum number of raw paragraphs to generate.
     */
    private val intRawParagraphsMin = JBIntSpinner(3, 1, 99999)

    /**
     * The maximum number of raw paragraphs to generate.
     */
    private val intRawParagraphsMax = JBIntSpinner(7, 1, 99999)

    /**
     * The minimum number of raw sentences in a paragraph.
     */
    private val intRawSentencesMin = JBIntSpinner(2, 1, 99999)

    /**
     * The maximum number of raw sentences in a paragraph.
     */
    private val intRawSentencessMax = JBIntSpinner(14, 1, 99999)

    /**
     * Contains the seed to generate random numbers.
     */
    private val txtRawSeed = JBTextField(Random.nextInt().toString())

    /**
     * Panel containing options for raw text.
     */
    private val panelRaw = JPanel().apply {
        border = EmptyBorder(8, 0, 8, 16)
        layout = BoxLayout(this, BoxLayout.Y_AXIS).apply {
            // Flush left.
            alignmentX = 0.0f
        }

        val labelWidth = 192
        addLabeledComponent(cboxRawTemplate, "Dummy text template:", labelWidth)
        addLabeledComponent(hbox(8, intRawParagraphsMin, JLabel("to"), intRawParagraphsMax), "Number of paragraphs:", labelWidth)
        addLabeledComponent(hbox(8, intRawSentencesMin, JLabel("to"), intRawSentencessMax), "Sentences per paragraph:", labelWidth)
        addLabeledComponent(txtRawSeed, "Seed:", labelWidth)
    }

    private val tabPane = JBTabbedPane().apply {
        insertTab(DummyTextData.IpsumType.BLINDTEXT.description, null, panelBlindtext, null, 0)
        insertTab(DummyTextData.IpsumType.LIPSUM.description, null, panelLipsum, null, 1)
        insertTab(DummyTextData.IpsumType.RAW.description, null, panelRaw, null, 2)
    }

    init {
        super.init()
        title = "Insert dummy text"
    }

    /**
     * Get the data that has been entered into the UI.
     */
    fun extractData() = when (tabPane.selectedIndex) {
        0 -> DummyTextData(
            ipsumType = DummyTextData.IpsumType.BLINDTEXT,
            blindtextType = cboxBlindType.selectedItem as DummyTextData.BlindtextType,
            blindtextRepetitions = intBlindRepetitions.number,
            blindtextParagraphs = intBlindParagraphs.number,
            blindtextLevel = intBlindLevel.number
        )
        1 -> DummyTextData(
            ipsumType = DummyTextData.IpsumType.LIPSUM,
            lipsumParagraphs = intLipsumParagraphsMin.number..intLipsumParagraphsMax.number,
            lipsumSentences = intLipsumSentencesMin.number..intLipsumSentencesMax.number,
            lipsumParagraphSeparator = cboxLipsumSeparator.selectedItem as DummyTextData.LipsumParagraphSeparation
        )
        else -> DummyTextData(
            ipsumType = DummyTextData.IpsumType.RAW,
            rawDummyTextType = cboxRawTemplate.selectedItem as Ipsum,
            rawParagraphs = intRawParagraphsMin.number..intRawParagraphsMax.number,
            rawSentencesPerParagraph = intRawSentencesMin.number..intRawSentencessMax.number,
            rawSeed = txtRawSeed.text.toInt()
        )
    }

    private fun updateUi() {
        intBlindLevel.isEnabled = cboxBlindType.item == DummyTextData.BlindtextType.ITEMIZE ||
                cboxBlindType.item == DummyTextData.BlindtextType.DESCRIPTION ||
                cboxBlindType.item == DummyTextData.BlindtextType.ENUMERATE

        intBlindRepetitions.isEnabled = cboxBlindType.item == DummyTextData.BlindtextType.PARAGRAPH
        intBlindParagraphs.isEnabled = cboxBlindType.item == DummyTextData.BlindtextType.PARAGRAPH
    }

    override fun createCenterPanel() = JPanel(BorderLayout()).apply {
        add(tabPane, BorderLayout.CENTER)
        updateUi()
    }

    override fun doValidate() = if (intLipsumParagraphsMax.number < intLipsumParagraphsMin.number) {
        ValidationInfo("Maximum must be greater than or equal to the minimum.", intLipsumParagraphsMax)
    }
    else if (intLipsumSentencesMax.number < intLipsumSentencesMin.number) {
        ValidationInfo("Maximum must be greater than or equal to the minimum.", intLipsumSentencesMax)
    }
    else if (intRawParagraphsMax.number < intRawParagraphsMin.number) {
        ValidationInfo("Maximum must be greater than or equal to the minimum.", intRawParagraphsMax)
    }
    else if (intRawSentencessMax.number < intRawSentencesMin.number) {
        ValidationInfo("Maximum must be greater than or equal to the minimum.", intRawSentencessMax)
    }
    else if (txtRawSeed.text.toIntOrNull() == null) {
        ValidationInfo("Invalid seed: must be an integer in range -2147483648 to 2147483647.", txtRawSeed)
    }
    else null
}