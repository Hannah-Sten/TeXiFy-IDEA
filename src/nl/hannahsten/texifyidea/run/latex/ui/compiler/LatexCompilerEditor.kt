package nl.hannahsten.texifyidea.run.latex.ui.compiler

import com.intellij.ide.util.BrowseFilesListener
import com.intellij.openapi.ui.BrowseFolderRunnable
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.PanelWithAnchor
import com.intellij.ui.SortedComboBoxModel
import com.intellij.util.ui.JBInsets
import java.awt.BorderLayout
import javax.swing.ComboBoxModel
import javax.swing.JList

/**
 * UI element to select a LaTeX compiler.
 *
 * Inspired heavily by [com.intellij.execution.ui.JrePathEditor].
 *
 * @author Sten Wessel
 */
class LatexCompilerEditor : LabeledComponent<ComboBox<LatexCompilerComboBoxItem>>(), PanelWithAnchor {

    private val comboBoxModel: SortedComboBoxModel<LatexCompilerComboBoxItem>

    init {
        comboBoxModel = initComboBoxModel()
        populateModel()
        initComboBox()

        text = "&LaTeX compiler:"
        labelLocation = BorderLayout.WEST

        updateUI()
    }

    private fun initComboBoxModel() = object : SortedComboBoxModel<LatexCompilerComboBoxItem>(
        compareBy<LatexCompilerComboBoxItem> { it.order }.thenBy { it.presentableText.toLowerCase() }
    ) {

        override fun setSelectedItem(anItem: Any?) {
            if (anItem is AddCompilerItem) {
                component.hidePopup()
                buildBrowseRunnable().run()
                return
            }

            super.setSelectedItem(anItem)
        }
    }

    private fun populateModel() {
        comboBoxModel.apply {
            // Manual creation of these items is a temporary solution
            // Replace this by the compilers discovered by the SDK
            add(PdflatexCompilerItem())

            add(AddCompilerItem())
        }
    }

    private fun initComboBox() {
        val comboBox = ComboBox(comboBoxModel).apply {
            isEditable = false
            renderer = object : ColoredListCellRenderer<LatexCompilerComboBoxItem>() {

                init {
                    ipad = JBInsets.create(1, 0)
                    myBorder = null
                }

                override fun customizeCellRenderer(
                    list: JList<out LatexCompilerComboBoxItem>,
                    value: LatexCompilerComboBoxItem?,
                    index: Int,
                    selected: Boolean,
                    hasFocus: Boolean
                ) {
                    value?.render(this, selected)
                }
            }
        }

        component = comboBox
    }

    // TODO: API usage is experimental. Change to alternative?
    private fun buildBrowseRunnable(): Runnable = BrowseFolderRunnable(
        "Select Alternative LaTeX Compiler",
        "Select LaTeX compiler executable to run with",
        null,
        BrowseFilesListener.SINGLE_FILE_DESCRIPTOR,
        component,
        LatexCompilerComboBoxTextComponentAccessor.INSTANCE
    )
}