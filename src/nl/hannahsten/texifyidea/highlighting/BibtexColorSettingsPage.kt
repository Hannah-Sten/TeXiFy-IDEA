package nl.hannahsten.texifyidea.highlighting

import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import nl.hannahsten.texifyidea.TexifyIcons

/**
 * @author Hannah Schellekens
 */
open class BibtexColorSettingsPage : ColorSettingsPage {

    private val descriptors = arrayOf(
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

    private val demoTags = mapOf(
        "key" to BibtexSyntaxHighlighter.KEY,
        "string" to BibtexSyntaxHighlighter.STRING,
        "value" to BibtexSyntaxHighlighter.VALUE
    )

    override fun getIcon() = TexifyIcons.BIBLIOGRAPHY_FILE

    override fun getHighlighter() = BibtexSyntaxHighlighter()

    override fun getDemoText() =
        """
                |%
                |%  Comments are amazing
                |%
                |
                |@Preamble{<string>"Fancy
                |    \newcommand{\peanutbutter}{Jelly Time!}
                |"</string>}
                |
                |@string{ <key>test</key> = <string>"String definition"</string> }
                |@string{ <key>identifier</key> = <string>"Identifier"</string> }
                |
                |@tokentype{
                |    <key>key</key> = <string>"String"</string> # <value>{Braced string}</value> # identifier
                |},
                |
                |@article{small,
                |    <key>author</key> = <value>{Henry}</value> # test,
                |    <key>title</key> = <value>{A small paper}</value>,
                |    <key>journal</key> = <string>"The journal of smiles"</string>,
                |    <key>year</key> = 1997,
                |    <key>volume</key> = -1,
                |    <key>note</key> = <value>{to appear}</value>,
                |}
        """.trimMargin()

    override fun getAdditionalHighlightingTagToDescriptorMap() = demoTags

    override fun getAttributeDescriptors() = descriptors

    override fun getColorDescriptors(): Array<out ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY!!

    override fun getDisplayName() = "BibTeX"
}