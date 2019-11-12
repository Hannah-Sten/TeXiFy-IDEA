package nl.hannahsten.texifyidea.settings

import nl.hannahsten.texifyidea.run.linuxpdfviewer.PdfViewer

data class TexifySettingsState (
        var automaticSoftWraps : Boolean = false,
        var automaticSecondInlineMathSymbol : Boolean = true,
        var automaticUpDownBracket : Boolean = true,
        var automaticItemInItemize: Boolean = true,
        var continuousPreview: Boolean = false,
        var automaticQuoteReplacement: TexifySettings.QuoteReplacement = TexifySettings.QuoteReplacement.NONE,
        var pdfViewer: PdfViewer = PdfViewer.firstAvailable(),
        var labelCommands: Map<String, String> =
                hashMapOf("\\label" to LabelingCommandInformation("\\label", 1, true).toSerializableString())
)
