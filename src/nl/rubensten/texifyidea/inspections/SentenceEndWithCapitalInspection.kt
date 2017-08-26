package nl.rubensten.texifyidea.inspections

import java.util.regex.Pattern

/**
 * @author Ruben Schellekens
 */
open class SentenceEndWithCapitalInspection : TexifyRegexInspection(
        inspectionDisplayName = "End-of-sentence space after sentences ending with capitals",
        inspectionShortName = "SentenceEndWithCapital",
        errorMessage = { "Sentences ending with a capital letter should end with an end-of-sentence space" },
        pattern = Pattern.compile("[A-ZÀ-Ý](\\.)[ \\t]*\\n"),
        replacement = { "\\@." },
        replacementRange = { it.start(1)..it.start(1) + 1 },
        quickFixName = { "Add an end-of-sentence space" }
)