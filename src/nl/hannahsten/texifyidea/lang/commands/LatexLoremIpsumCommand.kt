package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.BLINDTEXT
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.DEFAULT

/**
 * @author Hannah Schellekens
 */
enum class LatexLoremIpsumCommand(
    override val command: String,
    override vararg val arguments: Argument = emptyArray(),
    override val dependency: LatexPackage = DEFAULT,
    override val display: String? = null,
    override val isMathMode: Boolean = false,
    val collapse: Boolean = false
) : LatexCommand {

    BLIND_DOCUMENT("blinddocument", dependency = BLINDTEXT),
    LONG_BLIND_DOCUMENT("Blinddocument", dependency = BLINDTEXT),
    BLIND_TEXT("blindtext", "repetitions".asOptional(), dependency = BLINDTEXT),
    LONG_BLIND_TEXT("Blindtext", "paragraphs".asOptional(), "repetitions".asOptional(), dependency = BLINDTEXT),
    BLIND_LIST("blindlist", "list".asRequired(Argument.Type.LIST_ENVIRONMENT), dependency = BLINDTEXT),
    BLIND_LIST_OPTIONAL("blindlistoptional", "list".asRequired(Argument.Type.LIST_ENVIRONMENT), dependency = BLINDTEXT),
    BLIND_LIST_LIST("blindlistlist", "level".asOptional(), "list".asRequired(Argument.Type.LIST_ENVIRONMENT), dependency = BLINDTEXT),
    LONG_BLIND_LIST("Blindlist", "list".asRequired(Argument.Type.LIST_ENVIRONMENT), dependency = BLINDTEXT),
    LONG_BLIND_LIST_OPTIONAL("Blindlistoptional", "list".asRequired(Argument.Type.LIST_ENVIRONMENT), dependency = BLINDTEXT),
    BLIND_ITEMIZE("blinditemize", dependency = BLINDTEXT),
    BLIND_ENUMERATE("blindenumerate", dependency = BLINDTEXT),
    BLIND_DESCRIPTION("blinddescription", dependency = BLINDTEXT),
    LIPSUM("lipsum", "paragraph range".asOptional(), "sentence range".asOptional(), dependency = LatexPackage.LIPSUM),
    LIPSUM_AS_SINGLE_PARAGRAPH("lipsum*", "paragraph range".asOptional(), "sentence range".asOptional(), dependency = LatexPackage.LIPSUM),
    ;

    override val identifier: String
        get() = name
}