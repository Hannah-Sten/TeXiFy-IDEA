package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage

/**
 * [Source](http://mirrors.ctan.org/info/texbytopic/TeXbyTopic.pdf) chapter Conditionals
 *
 * @author Hannah Schellekens
 */
enum class LatexIfCommand(
    override val command: String,
    override vararg val arguments: Argument = emptyArray(),
    override val dependency: LatexPackage = LatexPackage.DEFAULT,
    override val display: String? = null,
    override val isMathMode: Boolean = false,
    val collapse: Boolean = false
) : LatexCommand {

    IF("if"),
    IFCAT("ifcat"),
    IFX("ifx"),
    IFCASE("ifcase"),
    IFNUM("ifnum"),
    IFODD("ifodd"),
    IFHMODE("ifhmode"),
    IFVMODE("ifvmode"),
    IFMMODE("ifmmode"),
    IFINNER("ifinner"),
    IFDIM("ifdim"),
    IFVOID("ifvoid"),
    IFHBOX("ifhbox"),
    IFVBOX("ifvbox"),
    IFEOF("ifeof"),
    IFTRUE("iftrue"),
    IFFALSE("iffalse"),
    FI("fi"),
    ELSE("else"),
    OR("or"),
    ;

    override val identifier: String
        get() = name
}

fun main() {

    LatexIfCommand.entries.forEach {
        println("+\"${it.command}\"")
    }
}