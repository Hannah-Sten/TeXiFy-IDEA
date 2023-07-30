package nl.hannahsten.texifyidea.lang.commands

import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexParameterText
import nl.hannahsten.texifyidea.util.parser.firstChildOfType
import nl.hannahsten.texifyidea.util.magic.CommandMagic
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
    GLS("gls", "label".asRequired(), dependency = LatexPackage.GLOSSARIES),
    GLSUPPER("Gls", "label".asRequired(), dependency = LatexPackage.GLOSSARIES),
    GLSPLURAL("glspl", "label".asRequired(), dependency = LatexPackage.GLOSSARIES),
    GLSPLURALUPPER("Glspl", "label".asRequired(), dependency = LatexPackage.GLOSSARIES),

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
    }
}