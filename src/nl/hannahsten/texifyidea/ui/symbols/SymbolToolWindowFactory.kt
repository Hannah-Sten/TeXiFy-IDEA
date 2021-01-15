package nl.hannahsten.texifyidea.ui.symbols

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.SearchTextField
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.WrapLayout
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.psiFile
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import javax.swing.event.DocumentEvent

/**
 * The Symbol tool window shows an overview of several symbols that can be inserted in the active latex document.
 *
 * @author Hannah Schellekens
 */
open class SymbolToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val toolWindowPanel = SymbolToolWindow(project)
        val content = ContentFactory.SERVICE.getInstance().createContent(toolWindowPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    override fun isApplicable(project: Project) = project.hasLatexModule()

    /**
     * The swing contents of the symbol tool window.
     */
    class SymbolToolWindow(val project: Project) : JPanel(BorderLayout(0, 0)) {

        /**
         * Control to search through the symbols that are in the currently selected category.
         */
        private val txtSearch = SearchTextField(true, "History").apply {
            addDocumentListener(object : DocumentAdapter() {
                override fun textChanged(e: DocumentEvent) {
                    updateFilters()
                }
            })
        }

        /**
         * Selects the current category if icons.
         */
        private val cboxCategory = ComboBox(SymbolCategories.categoryList.toTypedArray()).apply {
            addActionListener {
                updateFilters()
            }
        }

        /**
         * Maps a symbol category to a map mapping a symbol entry to the ui button.
         */
        private val btnSymbols = HashMap<SymbolCategory, MutableMap<SymbolUiEntry, JButton>>()

        /**
         * Panel that contains all the symbols in the current category.
         */
        private val panelSymbols = JPanel(WrapLayout(FlowLayout.LEFT, 4, 4)).apply {
            SymbolCategories.categories.forEach { (category, symbols) ->
                symbols.forEach { symbol ->
                    add(symbol.createButton(category))
                }
            }
        }

        init {
            border = EmptyBorder(8, 8, 8, 8)

            add(filterPanel(), BorderLayout.NORTH)
            add(panelSymbols, BorderLayout.CENTER)
        }

        /**
         * Creates a panel containing the filter controls.
         */
        private fun filterPanel() = JPanel(BorderLayout(8, 8)).apply {
            add(txtSearch, BorderLayout.CENTER)
            add(cboxCategory, BorderLayout.EAST)
        }

        /**
         * Creates the symbol button for this symbol entry.
         */
        private fun SymbolUiEntry.createButton(category: SymbolCategory) = JButton().apply {
            // Make sure the button is exactly 48x48
            minimumSize = Dimension(48, 48)
            preferredSize = Dimension(48, 48)
            maximumSize = Dimension(48, 48)

            // Load the icon with IJ's loader, dark/light mode is managed automatically this way.
            icon = IconLoader.getIcon(imagePath, SymbolToolWindowFactory::class.java)

            // Create a nice tooltip.
            val packageInfo = if (dependency == LatexPackage.DEFAULT) "" else """<b>Package:</b> ${dependency.name}<br>"""
            toolTipText = """<html>
                <p><b>Command:</b> ${generatedLatex.replace("<cursor>", "")}<br>
                $packageInfo
                <p><i>$description</i></p>
            </html>"""

            // Insert symbol when pressed.
            addActionListener { insertSymbol(this@createButton) }

            // Cache the button so it can be filtered later.
            val symbolMap = btnSymbols.getOrDefault(category, HashMap())
            symbolMap[this@createButton] = this
            btnSymbols[category] = symbolMap
        }

        /**
         * Shows or hides the buttons based on the filters.
         */
        private fun updateFilters() {
            val selectedCategory = cboxCategory.item
            val query = txtSearch.text.trim()

            btnSymbols.forEach { (category, symbolMap) ->
                symbolMap.forEach { (symbol, button) ->
                    button.isVisible =
                            // Selected category must match.
                            (selectedCategory == category || selectedCategory == SymbolCategory.ALL) &&
                                    // When a query is typed, must match as well.
                                    (query.isBlank() || symbol.queryString().contains(query))
                }
            }
        }

        /**
         * Inserts the symbol into the currently active document.
         */
        private fun insertSymbol(symbol: SymbolUiEntry) {
            val editor = project.currentTextEditor()?.editor ?: return
            val originalCaret = editor.caretOffset()

            val latex = symbol.generatedLatex
            val textToInsert = latex.replace("<caret>", "")
            editor.insertAtCaretAndMove(textToInsert)

            // Import the required package.
            WriteCommandAction.runWriteCommandAction(project) {
                editor.document.psiFile(project)?.insertUsepackage(symbol.dependency)
            }

            // When <caret> is defined, the cursor ends up at the location of <caret>
            val caretLocationInGeneratedLatex = latex.indexOf("<caret>")
            if (caretLocationInGeneratedLatex < 0) return

            val newCaret = originalCaret + caretLocationInGeneratedLatex
            editor.caretModel.moveToOffset(newCaret)
        }

        /**
         * Generate a lowercase string through which to search.
         */
        private fun SymbolUiEntry.queryString() = buildString {
            command?.let { append(it.commandDisplay) }
            append(generatedLatex)
            append(dependency.name)
            append(description)
        }.toLowerCase()
    }
}