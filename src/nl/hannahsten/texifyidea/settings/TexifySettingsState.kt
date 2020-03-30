package nl.hannahsten.texifyidea.settings

import nl.hannahsten.texifyidea.run.linuxpdfviewer.PdfViewer

data class TexifySettingsState (
        var automaticSecondInlineMathSymbol : Boolean = true,
        var automaticUpDownBracket : Boolean = true,
        var automaticItemInItemize: Boolean = true,
        var automaticDependencyCheck: Boolean = true,
        var autoCompile: Boolean = false,
        var continuousPreview: Boolean = false,
        var dockerizedMiktex: Boolean = false,
        var includeBackslashInSelection: Boolean = false,
        var automaticQuoteReplacement: TexifySettings.QuoteReplacement = TexifySettings.QuoteReplacement.NONE,
        var pdfViewer: PdfViewer = PdfViewer.firstAvailable(),
        var labelCommands: Map<String, String> =
                hashMapOf("\\label" to LabelingCommandInformation("\\label", 1, true).toSerializableString())
)
