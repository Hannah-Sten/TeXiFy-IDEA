package nl.rubensten.texifyidea.settings

data class TexifySettingsState (
        var labelCommands: Map<String, String> =
                hashMapOf("\\label" to LabelingCommandInformation("\\label", 1, true).toSerializableString()),
        var automaticSoftWraps : Boolean = false,
        var automaticSecondInlineMathSymbol : Boolean = true,
        var automaticUpDownBracket : Boolean = true,
        var automaticItemInItemize: Boolean = true,
        var automaticQuoteReplacement: TexifySettings.QuoteReplacement = TexifySettings.QuoteReplacement.NONE
)
