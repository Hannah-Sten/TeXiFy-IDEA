package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage

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
}