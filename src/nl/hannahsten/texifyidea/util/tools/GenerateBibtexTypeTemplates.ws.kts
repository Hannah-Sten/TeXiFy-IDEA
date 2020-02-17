import nl.hannahsten.texifyidea.lang.BibtexDefaultEntry
import nl.hannahsten.texifyidea.util.print
import nl.hannahsten.texifyidea.util.tools.tags

val result = StringBuilder("<templateSet>\n")

for (entry in BibtexDefaultEntry.values()) {
    if (entry == BibtexDefaultEntry.STRING || entry == BibtexDefaultEntry.PREAMBLE) {
        continue
    }

    val token = entry.token
    val tags = tags(entry)

    // Generate template.
    result.append("""|<!-- @${token.toUpperCase()} -->
            |<template id="BIBTEX.type.$token" name="$token"
            |        value="{${'$'}IDENTIFIER${'$'},&#10;&#32;&#32;&#32;&#32;$tags${'$'}END${'$'}&#10;}"
            |        description="Starts a new @$token entry." toReformat="false" toShortenFQNames="false">
            |    <variable name="IDENTIFIER" expression="" defaultValue="&quot;identifier&quot;" alwaysStopAt="true" />
            |
        """.trimMargin())

    // Add variables.
    for (field in entry.required) {
        val upper = field.fieldName.toUpperCase()
        val lower = upper.toLowerCase()
        result.append("    <variable name=\"$upper\" expression=\"\" defaultValue=\"&quot;$lower&quot;\" alwaysStopAt=\"true\" />\n")
    }

    // Finish up & context.
    result.append("\n    <context><option name=\"BIBTEX\" value=\"true\" /></context>\n")
    result.append("</template>\n\n")
}

result.append("</templateSet>")
// Print to the output, in output open the <templateSet> tag and copy that.
result.print()
