package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.XPARSE

/**
 * @author Hannah Schellekens
 */
enum class LatexXparseCommand(
        override val command: String,
        override vararg val arguments: Argument = emptyArray(),
        override val dependency: LatexPackage = LatexPackage.DEFAULT,
        override val display: String? = null,
        override val isMathMode: Boolean = false,
        val collapse: Boolean = false
) : LatexCommand {

    NEWDOCUMENTCOMMAND("NewDocumentCommand", "name".asRequired(), "args spec".asRequired(), "code".asRequired(), dependency = XPARSE),
    RENEWDOCUMENTCOMMAND("RenewDocumentCommand", "name".asRequired(), "args spec".asRequired(), "code".asRequired(), dependency = XPARSE),
    PROVIDEDOCUMENTCOMMAND("ProvideDocumentCommand", "name".asRequired(), "args spec".asRequired(), "code".asRequired(), dependency = XPARSE),
    DECLAREDOCUMENTCOMMAND("DeclareDocumentCommand", "name".asRequired(), "args spec".asRequired(), "code".asRequired(), dependency = XPARSE),
    NEWDOCUMENTENVIRONMENT("NewDocumentEnvironment", "name".asRequired(), "args spec".asRequired(), "start code".asRequired(), "end code".asRequired(), dependency = XPARSE),
    RENEWDOCUMENTENVIRONMENT("RenewDocumentEnvironment", "name".asRequired(), "args spec".asRequired(), "start code".asRequired(), "end code".asRequired(), dependency = XPARSE),
    PROVIDEDOCUMENTENVIRONMENT("ProvideDocumentEnvironment", "name".asRequired(), "args spec".asRequired(), "start code".asRequired(), "end code".asRequired(), dependency = XPARSE),
    DECLAREDOCUMENTENVIRONMENT("DeclareDocumentEnvironment", "name".asRequired(), "args spec".asRequired(), "start code".asRequired(), "end code".asRequired(), dependency = XPARSE),
    ;

    override val identifyer: String
        get() = name
}