package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.DIFFCOEFF

/**
 * @author Florian Kraft
 */
enum class LatexDiffcoeffCommand(
    override val command: String,
    override vararg val arguments: Argument = emptyArray(),
    override val dependency: LatexPackage = LatexPackage.DIFFCOEFF,
    override val display: String? = null,
    override val isMathMode: Boolean = true,
    val collapse: Boolean = false
) : LatexCommand {

    DIFF("diff", "order-spec".asOptional(), "differentiand".asRequired(), "variable(s)".asRequired(), "pt of eval".asOptional()),
    DIFF_STAR("diff*", "order-spec".asOptional(), "differentiand".asRequired(), "variable(s)".asRequired(), "pt of eval".asOptional()),
    DIFF_STAR_STAR("diff**", "order-spec".asOptional(), "variable(s)".asRequired(), "differentiand".asRequired(), "pt of eval".asOptional()),
    DIFFP("diffp", "order-spec".asOptional(), "differentiand".asRequired(), "variable(s)".asRequired(), "pt of eval".asOptional()),
    DIFFP_STAR("diffp*", "order-spec".asOptional(), "differentiand".asRequired(), "variable(s)".asRequired(), "pt of eval".asOptional()),
    DIFFP_STAR_STAR("diffp**", "order-spec".asOptional(), "variable(s)".asRequired(), "differentiand".asRequired(), "pt of eval".asOptional()),
    DIFS("difs", "order-spec".asOptional(), "differentiand".asRequired(), "variable(s)".asRequired(), "pt of eval".asOptional()),
    DIFS_STAR("difs*", "order-spec".asOptional(), "differentiand".asRequired(), "variable(s)".asRequired(), "pt of eval".asOptional()),
    DIFS_STAR_STAR("difs**", "order-spec".asOptional(), "variable(s)".asRequired(), "differentiand".asRequired(), "pt of eval".asOptional()),
    DIFSP("difsp", "order-spec".asOptional(), "differentiand".asRequired(), "variable(s)".asRequired(), "pt of eval".asOptional()),
    DIFSP_STAR("difsp*", "order-spec".asOptional(), "differentiand".asRequired(), "variable(s)".asRequired(), "pt of eval".asOptional()),
    DIFSP_STAR_STAR("difsp**", "order-spec".asOptional(), "variable(s)".asRequired(), "differentiand".asRequired(), "pt of eval".asOptional()),
    DIFC("difc", "order-spec".asOptional(), "differentiand".asRequired(), "variable(s)".asRequired(), "pt of eval".asOptional()),
    DIFC_STAR_STAR("difc**", "order-spec".asOptional(), "variable(s)".asRequired(), "differentiand".asRequired(), "pt of eval".asOptional()),
    DIFCP("difcp", "order-spec".asOptional(), "differentiand".asRequired(), "variable(s)".asRequired(), "pt of eval".asOptional()),
    DIFCP_STAR_STAR("difcp**", "order-spec".asOptional(), "variable(s)".asRequired(), "differentiand".asRequired(), "pt of eval".asOptional()),
    ;

    override val identifier: String
        get() = name
}
