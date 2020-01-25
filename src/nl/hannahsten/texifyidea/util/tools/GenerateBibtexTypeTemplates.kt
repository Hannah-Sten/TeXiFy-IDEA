package nl.hannahsten.texifyidea.util.tools

import nl.hannahsten.texifyidea.lang.BibtexDefaultEntry
import nl.hannahsten.texifyidea.lang.BibtexEntryType
import java.io.File

/**
 * Generates xml for the BibTeX.xml template file.
 *
 * @author Hannah Schellekens
 */
fun main() {
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

    // Write result to file.
    File("bibtex-templates.xml").printWriter().use {
        it.println(result.toString())
    }
}

fun tags(entry: BibtexEntryType): String {
    val result = StringBuilder()
    var newline = ""
    var spacing = ""

    for (field in entry.required) {
        result.append("$newline$spacing${field.fieldName} = {${'$'}${field.fieldName.toUpperCase()}${'$'}},")
        newline = "&#10;"
        spacing = "&#32;&#32;&#32;&#32;"
    }

    return result.toString()
}