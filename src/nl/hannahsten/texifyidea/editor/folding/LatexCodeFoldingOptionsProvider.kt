package nl.hannahsten.texifyidea.editor.folding

import com.intellij.application.options.editor.CodeFoldingOptionsProvider
import com.intellij.openapi.options.BeanConfigurable

/**
 * Settings > Editor > General > Code Folding settings for LaTeX.
 */
class LatexCodeFoldingOptionsProvider : CodeFoldingOptionsProvider, BeanConfigurable<LatexCodeFoldingSettings>(LatexCodeFoldingSettings.getInstance(), "LaTeX") {

    init {
        val settings = instance
        if (settings != null) {
            checkBox("Package imports", settings::collapseImports)
        }
    }
}