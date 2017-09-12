package nl.rubensten.texifyidea.highlighting

import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import nl.rubensten.texifyidea.TexifyIcons

/**
 * @author Ruben Schellekens
 */
open class BibtexColorSettingsPage : ColorSettingsPage {

    companion object {

        val DESCRIPTORS = arrayOf(
                AttributesDescriptor("Assignment", BibtexSyntaxHighlighter.ASSIGNMENT),
                AttributesDescriptor("Braces", BibtexSyntaxHighlighter.BRACES),
                AttributesDescriptor("Comments", BibtexSyntaxHighlighter.COMMENTS),
                AttributesDescriptor("Concatenation", BibtexSyntaxHighlighter.CONCATENATION),
                AttributesDescriptor("Identifier", BibtexSyntaxHighlighter.IDENTIFIER),
                AttributesDescriptor("Key", BibtexSyntaxHighlighter.KEY),
                AttributesDescriptor("Number", BibtexSyntaxHighlighter.NUMBER),
                AttributesDescriptor("String", BibtexSyntaxHighlighter.STRING),
                AttributesDescriptor("Type token", BibtexSyntaxHighlighter.TYPE_TOKEN),
                AttributesDescriptor("Value", BibtexSyntaxHighlighter.VALUE)
        )
    }

    override fun getIcon() = TexifyIcons.BIBLIOGRAPHY_FILE!!

    override fun getHighlighter() = BibtexSyntaxHighlighter()

    override fun getDemoText() = """
                |%
                |%  Comments are amazing
                |%
                |
                |@Preamble{"Fancy
                |    \newcommand{\peanutbutter}{Jelly Time!}
                |"}
                |
                |@string{ test = "String definition" }
                |@string{ identifier = "Identifier" }
                |
                |@tokentype{
                |    key = "String" # {Braced string} # identifier
                |},
                |
                |@article{small,
                |    author = {Henry} # test,
                |    title = {A small paper},
                |    journal = "The journal of smiles",
                |    year = 1997,
                |    volume = -1,
                |    note = {to appear},
                |}""".trimMargin()

    override fun getAdditionalHighlightingTagToDescriptorMap() = null

    override fun getAttributeDescriptors() = DESCRIPTORS

    override fun getColorDescriptors(): Array<out ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY!!

    override fun getDisplayName() = "BibTeX"
}