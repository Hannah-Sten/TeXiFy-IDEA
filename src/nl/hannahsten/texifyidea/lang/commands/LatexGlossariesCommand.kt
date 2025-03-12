package nl.hannahsten.texifyidea.lang.commands

import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexParameterText
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.parser.firstChildOfType
import nl.hannahsten.texifyidea.util.parser.requiredParameter
import nl.hannahsten.texifyidea.util.parser.requiredParameters

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
    );

    companion object {

        /**
         * Extract the label text from a glossary entry command
         */
        fun extractGlossaryLabel(command: LatexCommands): String? {
            if (!CommandMagic.glossaryEntry.contains(command.name) &&
                !CommandMagic.glossaryReference.contains(command.name)
            ) return null
            return command.getRequiredParameters()[0]
        }

        /**
         * Extract the label element from a glossary entry command
         */
        fun extractGlossaryLabelElement(command: LatexCommands): PsiElement? {
            if (!CommandMagic.glossaryEntry.contains(command.name)) return null
            return command.requiredParameters()[0].firstChildOfType(LatexParameterText::class)
        }

        /**
         * Find the name, which is the text that will appear in the document, from the given glossary entry definition.
         */
        fun extractGlossaryName(command: LatexCommands): String? {
            if (setOf(NEWGLOSSARYENTRY, LONGNEWGLOSSARYENTRY).map { it.cmd }.contains(command.name)) {
                val keyValueList = command.requiredParameter(1) ?: return null
                return "name=\\{([^}]+)}".toRegex().find(keyValueList)?.groupValues?.get(1)
            }
            else if (setOf(NEWACRONYM, NEWABBREVIATION).map { it.cmd }.contains(command.name)) {
                return command.requiredParameter(1)
            }
            else {
                return null
            }
        }
    }
}