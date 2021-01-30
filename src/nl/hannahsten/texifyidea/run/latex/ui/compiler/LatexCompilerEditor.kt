package nl.hannahsten.texifyidea.run.latex.ui.compiler

import com.intellij.ide.util.BrowseFilesListener
import com.intellij.openapi.ui.BrowseFolderRunnable
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.PanelWithAnchor
import com.intellij.ui.SortedComboBoxModel
import com.intellij.util.ui.JBInsets
import nl.hannahsten.texifyidea.run.compiler.CustomLatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.SupportedLatexCompiler
import nl.hannahsten.texifyidea.util.magic.CompilerMagic
import java.awt.BorderLayout
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
            addAll(CompilerMagic.compilerByExecutableName.values.map { BuiltinCompilerItem(it) })
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
                    value?.render(this, index == -1)
                }
            }
        }

        component = comboBox
    }

    private fun buildBrowseRunnable(): Runnable = BrowseFolderRunnable(
        "Select Alternative LaTeX Compiler",
        "Select LaTeX compiler executable to run with",
        null,
        BrowseFilesListener.SINGLE_FILE_DESCRIPTOR,
        component,
        LatexCompilerComboBoxTextComponentAccessor.INSTANCE
    )

    fun setSelectedCompiler(compiler: LatexCompiler?) {
        comboBoxModel.selectedItem = when (compiler) {
            is SupportedLatexCompiler -> findSupportedCompiler(compiler)
            is CustomLatexCompiler -> findOrCreateCustomCompiler(compiler)
            null -> null
        }
    }

    fun getSelectedCompiler(): LatexCompiler? {
        return when (val selected = comboBoxModel.selectedItem) {
            is BuiltinCompilerItem -> selected.compiler
            is CustomCompilerItem -> selected.compiler
            else -> null
        }
    }

    private fun findSupportedCompiler(compiler: SupportedLatexCompiler): BuiltinCompilerItem? {
        return comboBoxModel.items.asSequence()
            .mapNotNull { it as? BuiltinCompilerItem }
            .firstOrNull { it.compiler == compiler }
    }

    private fun findOrCreateCustomCompiler(compiler: CustomLatexCompiler): CustomCompilerItem {
        return comboBoxModel.items.asSequence()
            .mapNotNull { it as? CustomCompilerItem }
            .firstOrNull { it.compiler.executablePath == compiler.executablePath }
            ?: CustomCompilerItem(compiler).also { comboBoxModel.add(it) }
    }
}