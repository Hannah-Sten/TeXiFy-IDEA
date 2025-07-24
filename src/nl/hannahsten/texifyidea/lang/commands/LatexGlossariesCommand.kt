package nl.hannahsten.texifyidea.lang.commands

import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexParameterText
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.parser.findFirstChildOfType

/**
 * See CommandMagic#glossaryReference
 */
enum class LatexGlossariesCommand(
    override val command: String,
    override vararg val arguments: Argument = emptyArray(),
    override val dependency: LatexPackage = LatexPackage.GLOSSARIES,
    override val display: String? = null,
    override val isMathMode: Boolean = false,
    val collapse: Boolean = false
) : LatexCommand {

    LONGNEWGLOSSARYENTRY(
        "longnewglossaryentry",
        "name".asRequired(),
        "options".asRequired(),
        "description".asRequired(),
        dependency = LatexPackage.GLOSSARIES
    ),
    NEWGLOSSARYENTRY(
        "newglossaryentry",
        "name".asRequired(),
        "options".asRequired(),
        dependency = LatexPackage.GLOSSARIES
    ),
    NEWACRONYM(
        "newacronym",
        "options".asOptional(),
        "name".asRequired(),
        "short".asRequired(),
        "long".asRequired(),
        dependency = LatexPackage.GLOSSARIES
    ),
    NEWABBREVIATION(
        "newabbreviation",
        "options".asOptional(),
        "name".asRequired(),
        "short".asRequired(),
        "long".asRequired(),
        dependency = LatexPackage.GLOSSARIES
    ),
    GLS("gls", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSUPPER("Gls", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSCAPS("GLS", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSPL("glspl", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSPLUPPER("Glspl", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSPLCAPS("Glspl", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSPDISP("glsdisp", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "text".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSPDISPUPPER("Glsdisp", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "text".asOptional(), dependency = LatexPackage.GLOSSARIES),

    // \glstext-like commands, see scripts/generate_commands.py
    GLSLINK("glslink", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "text".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSLINKUPPER("Glslink", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "text".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSTEXT("glstext", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSTEXTUPPER("Glstext", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSTEXTCAPS("GLStext", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSFIRST("glsfirst", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSFIRSTUPPER("Glsfirst", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSFIRSTCAPS("GLSfirst", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSPLURAL("glsplural", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSPLURALUPPER("Glsplural", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSPLURALCAPS("GLSplural", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSFIRSTPLURAL("glsfirstplural", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSFIRSTPLURALUPPER("Glsfirstplural", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSFIRSTPLURALCAPS("GLSfirstplural", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSNAME("glsname", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSNAMEUPPER("Glsname", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSNAMECAPS("GLSname", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSSYMBOL("glssymbol", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSSYMBOLUPPER("Glssymbol", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSSYMBOLCAPS("GLSsymbol", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSDESC("glsdesc", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSDESCUPPER("Glsdesc", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSDESCCAPS("GLSdesc", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSUSERI("glsuseri", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSUSERIUPPER("Glsuseri", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSUSERICAPS("GLSuseri", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSUSERII("glsuserii", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSUSERIIUPPER("Glsuserii", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSUSERIICAPS("GLSuserii", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSUSERIII("glsuseriii", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSUSERIIIUPPER("Glsuseriii", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSUSERIIICAPS("GLSuseriii", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSUSERIV("glsuseriv", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSUSERIVUPPER("Glsuseriv", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSUSERIVCAPS("GLSuseriv", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSUSERV("glsuserv", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSUSERVUPPER("Glsuserv", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSUSERVCAPS("GLSuserv", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSUSERVI("glsuservi", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSUSERVIUPPER("Glsuservi", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),
    GLSUSERVICAPS("GLSuservi", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),

    LOADGLSENTRIES(
        "loadglsentries",
        RequiredFileArgument(
            "glossariesfile",
            isAbsolutePathSupported = true,
            commaSeparatesArguments = false
        ),
        dependency = LatexPackage.GLOSSARIES
    ),

    AC("ac", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    AC_UPPER("Ac", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    AC_STAR("ac*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    AC_UPPER_STAR("Ac*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACF("acf", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACF_UPPER("Acf", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACF_STAR("acf*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACF_UPPER_STAR("Acf*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACS("acs", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACS_STAR("acs*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACL("acl", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACL_UPPER("Acl", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACL_STAR("acl*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACL_UPPER_STAR("Acl*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACP("acp", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACP_UPPER("Acp", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACP_STAR("acp*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACP_UPPER_STAR("Acp*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACFP("acfp", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACFP_UPPER("Acfp", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACFP_STAR("acfp*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACFP_UPPER_STAR("Acfp*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACSP("acsp", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACSP_STAR("acsp*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACLP("aclp", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACLP_UPPER("Aclp", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACLP_STAR("aclp*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACLP_UPPER_STAR("Aclp*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACFI("acfi", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACFI_UPPER("Acfi", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACFI_STAR("acfi*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACFI_UPPER_STAR("Acfi*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACFIP("acfip", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACFIP_UPPER("Acfip", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACFIP_STAR("acfip*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACFIP_UPPER_STAR("Acfip*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACSU("acsu", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACSU_STAR("acsu*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACLU("aclu", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACLU_UPPER("Aclu", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACLU_STAR("aclu*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    ACLU_UPPER_STAR("Aclu*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    IAC("iac", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    IAC_UPPER("Iac", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    IAC_STAR("iac*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),
    IAC_UPPER_STAR("Iac*", "linebreak penalty".asOptional(), "acronym".asRequired(Argument.Type.TEXT), dependency = LatexPackage.ACRONYM),

    ACRO("acro", "acronym".asRequired(), "short name".asOptional(), "full name".asRequired(), dependency = LatexPackage.ACRONYM),
    NEWACRO("newacro", "acronym".asRequired(), "short name".asOptional(), "full name".asRequired(), dependency = LatexPackage.ACRONYM),
    ACRODEF("acrodef", "acronym".asRequired(), "short name".asOptional(), "full name".asRequired(), dependency = LatexPackage.ACRONYM),
    ;

    companion object {

        /**
         * Extract the label text from a glossary entry command
         */
        fun extractGlossaryLabel(command: LatexCommands): String? {
            if (!CommandMagic.glossaryEntry.contains(command.name) &&
                !CommandMagic.glossaryReference.contains(command.name)
            ) return null
            return command.requiredParametersText()[0]
        }

        /**
         * Extract the label element from a glossary entry command
         */
        fun extractGlossaryLabelElement(command: LatexCommands): PsiElement? {
            if (!CommandMagic.glossaryEntry.contains(command.name)) return null
            return command.requiredParameters()[0].findFirstChildOfType(LatexParameterText::class)
        }

        private val glossaryEntryCommands: Set<String> = setOf(NEWGLOSSARYENTRY, LONGNEWGLOSSARYENTRY).map { it.cmd }.toSet()
        private val acronymEntryCommands: Set<String> = setOf(NEWACRONYM, NEWABBREVIATION).map { it.cmd }.toSet()
        private val acroEntryCommands: Set<String> = setOf(NEWACRO, ACRO, ACRODEF).map { it.cmd }.toSet()

        /**
         * Find the name, which is the text that will appear in the document, from the given glossary entry definition.
         */
        fun extractGlossaryName(command: LatexCommands): String? {
            when(command.name) {
                in glossaryEntryCommands -> {
                    val keyValueList = command.requiredParameterText(1) ?: return null
                    return "name=\\{([^}]+)}".toRegex().find(keyValueList)?.groupValues?.get(1)
                }
                in acronymEntryCommands -> {
                    return command.requiredParameterText(1)
                }
                in acroEntryCommands -> {
                    // For acro commands, the name is the first parameter
                    return command.requiredParameterText(0)
                }
            }
            return null
        }
    }
}