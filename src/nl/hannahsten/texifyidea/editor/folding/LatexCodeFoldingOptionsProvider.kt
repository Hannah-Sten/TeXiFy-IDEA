package nl.hannahsten.texifyidea.editor.folding

import com.intellij.application.options.editor.CodeFoldingOptionsProvider
import com.intellij.openapi.options.BeanConfigurable

/**
 * Settings > Editor > General > Code Folding settings for LaTeX.
 */
class LatexCodeFoldingOptionsProvider : CodeFoldingOptionsProvider, BeanConfigurable<LatexCodeFoldingSettings>(LatexCodeFoldingSettings.getInstance(), "LaTeX") {

    init {
        val settings = instance
        checkBox("Package imports", settings::collapseImports)
        checkBox("Environments", settings::foldEnvironments)
        checkBox("Escaped symbols", settings::foldEscapedSymbols)
        checkBox("Footnotes", settings::foldFootnotes)
        checkBox("Math symbols", settings::foldMathSymbols)
        checkBox("Sections", settings::foldSections)
        checkBox("Symbols", settings::foldSymbols)
    }
}