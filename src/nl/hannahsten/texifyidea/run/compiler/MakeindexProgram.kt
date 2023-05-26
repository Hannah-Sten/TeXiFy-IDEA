package nl.hannahsten.texifyidea.run.compiler

/**
 * Makeindex programs (to create a .ind file).
 *
 * @author Thomas Schouten
 */
enum class MakeindexProgram(val executableName: String) {

    XINDY("texindy"),
    TRUEXINDY("xindy"), // requires perl
    MAKEINDEX("makeindex"),

    // Wrapper which calls makeindex or xindy, from the glossaries package (so technically it doesn't create an index but a glossary)
    MAKEGLOSSARIES("makeglossaries"), // requires perl
    MAKEGLOSSARIESLITE("makeglossaries-lite"), // makeglossaries without using perl
    BIB2GLS("bib2gls")
}