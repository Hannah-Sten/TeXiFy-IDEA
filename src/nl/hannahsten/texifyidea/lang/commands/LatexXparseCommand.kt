package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage

/**
 * The xparse package is included in the kernel since the 2020/10/1 release.
 * 
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

    NEWDOCUMENTCOMMAND("NewDocumentCommand", "name".asRequired(), "args spec".asRequired(), "code".asRequired()),
    RENEWDOCUMENTCOMMAND("RenewDocumentCommand", "name".asRequired(), "args spec".asRequired(), "code".asRequired()),
    PROVIDEDOCUMENTCOMMAND("ProvideDocumentCommand", "name".asRequired(), "args spec".asRequired(), "code".asRequired()),
    DECLAREDOCUMENTCOMMAND("DeclareDocumentCommand", "name".asRequired(), "args spec".asRequired(), "code".asRequired()),
    NEWDOCUMENTENVIRONMENT("NewDocumentEnvironment", "name".asRequired(), "args spec".asRequired(), "start code".asRequired(), "end code".asRequired()),
    RENEWDOCUMENTENVIRONMENT("RenewDocumentEnvironment", "name".asRequired(), "args spec".asRequired(), "start code".asRequired(), "end code".asRequired()),
    PROVIDEDOCUMENTENVIRONMENT("ProvideDocumentEnvironment", "name".asRequired(), "args spec".asRequired(), "start code".asRequired(), "end code".asRequired()),
    DECLAREDOCUMENTENVIRONMENT("DeclareDocumentEnvironment", "name".asRequired(), "args spec".asRequired(), "start code".asRequired(), "end code".asRequired()),
    ;

    override val identifyer: String
        get() = name
}