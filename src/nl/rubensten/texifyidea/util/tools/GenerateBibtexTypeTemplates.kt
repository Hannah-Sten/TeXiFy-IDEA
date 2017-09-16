package nl.rubensten.texifyidea.util.tools

import nl.rubensten.texifyidea.lang.BibtexDefaultEntry
import nl.rubensten.texifyidea.lang.BibtexEntryType
import java.io.File

/**
 * Generates xml for the BibTeX.xml template file.
 *
 * @author Ruben Schellekens
 */
fun main(args: Array<String>) {
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
            |    <variable name="IDENTIFIER" expression="" defaultValue="" alwaysStopAt="true" />
            |
        """.trimMargin())

        // Add variables.
        for (field in entry.required) {
            val upper = field.fieldName.toUpperCase()
            result.append("    <variable name=\"$upper\" expression=\"\" defaultValue=\"\" alwaysStopAt=\"true\" />\n")
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