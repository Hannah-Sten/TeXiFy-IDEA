package nl.hannahsten.texifyidea.run.ui.compiler

import com.intellij.ide.util.BrowseFilesListener
import com.intellij.openapi.ui.BrowseFolderRunnable
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.PanelWithAnchor
import com.intellij.ui.SortedComboBoxModel
import com.intellij.util.ui.JBInsets
import nl.hannahsten.texifyidea.run.compiler.Compiler
import nl.hannahsten.texifyidea.run.compiler.CustomCompiler
import nl.hannahsten.texifyidea.run.compiler.SupportedCompiler
import nl.hannahsten.texifyidea.run.step.CompileStep
import java.awt.BorderLayout
import javax.swing.JList

/**
 * UI element to select a LaTeX compiler.
 *
 * Inspired heavily by [com.intellij.execution.ui.JrePathEditor].
 *
 * @author Sten Wessel
 */
class CompilerEditor<C : CompileStep, in S : SupportedCompiler<C>>(label: String, compilers: Iterable<S>) : LabeledComponent<ComboBox<CompilerComboBoxItem>>(), PanelWithAnchor {

    private val comboBoxModel: SortedComboBoxModel<CompilerComboBoxItem>

    init {
        comboBoxModel = initComboBoxModel()
        populateModel(compilers)
        initComboBox()

        text = label
        labelLocation = BorderLayout.WEST

        updateUI()
    }

    private fun initComboBoxModel() = object : SortedComboBoxModel<CompilerComboBoxItem>(
        compareBy<CompilerComboBoxItem> { it.order }.thenBy { it.presentableText.toLowerCase() }
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

    private fun populateModel(compilers: Iterable<S>) {
        comboBoxModel.apply {
            addAll(compilers.map { BuiltinCompilerItem(it) })
            add(AddCompilerItem())
        }
    }

    private fun initComboBox() {
        val comboBox = ComboBox(comboBoxModel).apply {
            isEditable = false
            renderer = object : ColoredListCellRenderer<CompilerComboBoxItem>() {

                init {
                    ipad = JBInsets.create(1, 0)
                    myBorder = null
                }

                override fun customizeCellRenderer(
                    list: JList<out CompilerComboBoxItem>,
                    value: CompilerComboBoxItem?,
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
        "Select Alternative Compiler",
        "Select compiler executable to run with",
        null,
        BrowseFilesListener.SINGLE_FILE_DESCRIPTOR,
        component,
        LatexCompilerComboBoxTextComponentAccessor.INSTANCE
    )

    fun setSelectedCompiler(compiler: Compiler<C>?) {
        comboBoxModel.selectedItem = when (compiler) {
            is SupportedCompiler<C> -> findSupportedCompiler(compiler)
            is CustomCompiler<C> -> findOrCreateCustomCompiler(compiler)
            else -> null
        }
    }

    fun getSelectedCompiler(): Compiler<C>? {
        return when (val selected = comboBoxModel.selectedItem) {
            is BuiltinCompilerItem<*> -> selected.compiler as Compiler<C>
            is CustomCompilerItem<*> -> selected.compiler as Compiler<C>
            else -> null
        }
    }

    private fun findSupportedCompiler(compiler: SupportedCompiler<C>): BuiltinCompilerItem<C>? {
        return comboBoxModel.items.asSequence()
            .mapNotNull { it as? BuiltinCompilerItem<C> }
            .firstOrNull { it.compiler == compiler }
    }

    private fun findOrCreateCustomCompiler(compiler: CustomCompiler<C>): CustomCompilerItem<C> {
        return comboBoxModel.items.asSequence()
            .mapNotNull { it as? CustomCompilerItem<C> }
            .firstOrNull { it.compiler.executablePath == compiler.executablePath }
            ?: CustomCompilerItem(compiler).also { comboBoxModel.add(it) }
    }
}