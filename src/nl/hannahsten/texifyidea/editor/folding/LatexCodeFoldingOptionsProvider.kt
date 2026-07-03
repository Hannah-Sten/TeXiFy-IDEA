package nl.hannahsten.texifyidea.editor.folding

import com.intellij.application.options.editor.CodeFoldingOptionsProvider
import com.intellij.openapi.options.BeanConfigurable
import nl.hannahsten.texifyidea.TexifyBundle

/**
 * Settings > Editor > General > Code Folding settings for LaTeX.
 */
class LatexCodeFoldingOptionsProvider : CodeFoldingOptionsProvider, BeanConfigurable<LatexCodeFoldingSettings>(
    LatexCodeFoldingSettings.getInstance(),
    TexifyBundle.message("settings.code.folding.title")
) {

    init {
        val settings = instance
        checkBox(TexifyBundle.message("settings.code.folding.package.imports"), settings::collapseImports)
        checkBox(TexifyBundle.message("settings.code.folding.environments"), settings::foldEnvironments)
        checkBox(TexifyBundle.message("settings.code.folding.escaped.symbols"), settings::foldEscapedSymbols)
        checkBox(TexifyBundle.message("settings.code.folding.footnotes"), settings::foldFootnotes)
        checkBox(TexifyBundle.message("settings.code.folding.math.symbols"), settings::foldMathSymbols)
        checkBox(TexifyBundle.message("settings.code.folding.math.style"), settings::foldMathStyle)
        checkBox(TexifyBundle.message("settings.code.folding.sections"), settings::foldSections)
        checkBox(TexifyBundle.message("settings.code.folding.symbols"), settings::foldSymbols)
        checkBox(TexifyBundle.message("settings.code.folding.left.right.expressions"), settings::foldLeftRightExpression)
        checkBox(TexifyBundle.message("settings.code.folding.left.right.commands"), settings::foldLeftRightCommands)
    }
}
