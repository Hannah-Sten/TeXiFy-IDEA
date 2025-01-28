package nl.hannahsten.texifyidea.settings

import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.run.pdfviewer.InternalPdfViewer

data class TexifySettingsState(
    var automaticSecondInlineMathSymbol: Boolean = true,
    var automaticUpDownBracket: Boolean = true,
    var automaticItemInItemize: Boolean = true,
    var automaticDependencyCheck: Boolean = true,
    var automaticBibtexImport: Boolean = true,
    var continuousPreview: Boolean = false,
    var includeBackslashInSelection: Boolean = false,
    var showPackagesInStructureView: Boolean = false,
    var enableExternalIndex: Boolean = true,
    var enableTextidote: Boolean = false,
    var textidoteOptions: String = "--check en --output singleline --no-color",
    var latexIndentOptions: String = "",
    var automaticQuoteReplacement: TexifySettings.QuoteReplacement = TexifySettings.QuoteReplacement.NONE,
    var htmlPasteTranslator: TexifySettings.HtmlPasteTranslator = TexifySettings.HtmlPasteTranslator.BUILTIN,
    var autoCompileOption: TexifySettings.AutoCompile? = null,
    var missingLabelMinimumLevel: LatexCommand = LatexGenericRegularCommand.SUBSECTION,
    // Kept for backwards compatibility
    var autoCompile: Boolean = false,
    var autoCompileOnSaveOnly: Boolean = false,
)
