package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.DIFFCOEFF

/**
 * @author Florian Kraft
 */
enum class LatexDiffcoeffCommand(
    override val command: String,
    override vararg val arguments: Argument = emptyArray(),
    override val dependency: LatexPackage = LatexPackage.DEFAULT,
    override val display: String? = null,
    override val isMathMode: Boolean = true,
    val collapse: Boolean = false
) : LatexCommand {

    DIFF("diff", "order-spec".asOptional(), "differentiand".asRequired(), "variable(s)".asRequired(), "pt of eval".asOptional(), dependency = DIFFCOEFF),
    DIFF_STAR("diff*", "order-spec".asOptional(), "differentiand".asRequired(), "variable(s)".asRequired(), "pt of eval".asOptional(), dependency = DIFFCOEFF),
    DIFF_STAR_STAR("diff**", "order-spec".asOptional(), "variable(s)".asRequired(), "differentiand".asRequired(), "pt of eval".asOptional(), dependency = DIFFCOEFF),
    DIFFP("diffp", "order-spec".asOptional(), "differentiand".asRequired(), "variable(s)".asRequired(), "pt of eval".asOptional(), dependency = DIFFCOEFF),
    DIFFP_STAR("diffp*", "order-spec".asOptional(), "differentiand".asRequired(), "variable(s)".asRequired(), "pt of eval".asOptional(), dependency = DIFFCOEFF),
    DIFFP_STAR_STAR("diffp**", "order-spec".asOptional(), "variable(s)".asRequired(), "differentiand".asRequired(), "pt of eval".asOptional(), dependency = DIFFCOEFF),
    DIFS("difs", "order-spec".asOptional(), "differentiand".asRequired(), "variable(s)".asRequired(), "pt of eval".asOptional(), dependency = DIFFCOEFF),
    DIFS_STAR("difs*", "order-spec".asOptional(), "differentiand".asRequired(), "variable(s)".asRequired(), "pt of eval".asOptional(), dependency = DIFFCOEFF),
    DIFS_STAR_STAR("difs**", "order-spec".asOptional(), "variable(s)".asRequired(), "differentiand".asRequired(), "pt of eval".asOptional(), dependency = DIFFCOEFF),
    DIFSP("difsp", "order-spec".asOptional(), "differentiand".asRequired(), "variable(s)".asRequired(), "pt of eval".asOptional(), dependency = DIFFCOEFF),
    DIFSP_STAR("difsp*", "order-spec".asOptional(), "differentiand".asRequired(), "variable(s)".asRequired(), "pt of eval".asOptional(), dependency = DIFFCOEFF),
    DIFSP_STAR_STAR("difsp**", "order-spec".asOptional(), "variable(s)".asRequired(), "differentiand".asRequired(), "pt of eval".asOptional(), dependency = DIFFCOEFF),
    DIFC("difc", "order-spec".asOptional(), "differentiand".asRequired(), "variable(s)".asRequired(), "pt of eval".asOptional(), dependency = DIFFCOEFF),
    DIFC_STAR_STAR("difc**", "order-spec".asOptional(), "variable(s)".asRequired(), "differentiand".asRequired(), "pt of eval".asOptional(), dependency = DIFFCOEFF),
    DIFCP("difcp", "order-spec".asOptional(), "differentiand".asRequired(), "variable(s)".asRequired(), "pt of eval".asOptional(), dependency = DIFFCOEFF),
    DIFCP_STAR_STAR("difcp**", "order-spec".asOptional(), "variable(s)".asRequired(), "differentiand".asRequired(), "pt of eval".asOptional(), dependency = DIFFCOEFF),
    ;

    override val identifier: String
        get() = name
}
