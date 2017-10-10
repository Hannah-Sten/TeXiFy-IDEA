package nl.rubensten.texifyidea.highlighting

import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import nl.rubensten.texifyidea.TexifyIcons

/**
 * @author Ruben Schellekens, Sten Wessel
 */
class LatexColorSettingsPage : ColorSettingsPage {

    companion object {

        val DESCRIPTORS = arrayOf(
                AttributesDescriptor("Braces", LatexSyntaxHighlighter.BRACES),
                AttributesDescriptor("Brackets", LatexSyntaxHighlighter.BRACKETS),
                AttributesDescriptor("Optional parameters", LatexSyntaxHighlighter.OPTIONAL_PARAM),
                AttributesDescriptor("Commands", LatexSyntaxHighlighter.COMMAND),
                AttributesDescriptor("Commands in inline math mode", LatexSyntaxHighlighter.COMMAND_MATH_INLINE),
                AttributesDescriptor("Commands in display math mode", LatexSyntaxHighlighter.COMMAND_MATH_DISPLAY),
                AttributesDescriptor("Comments", LatexSyntaxHighlighter.COMMENT),
                AttributesDescriptor("Inline math", LatexSyntaxHighlighter.INLINE_MATH),
                AttributesDescriptor("Display math", LatexSyntaxHighlighter.DISPLAY_MATH),
                AttributesDescriptor("Stars", LatexSyntaxHighlighter.STAR)
        )

        val DEMO_TAGS = mapOf(
            "displayCommand" to LatexSyntaxHighlighter.COMMAND_MATH_DISPLAY,
                "inlineCommand" to LatexSyntaxHighlighter.COMMAND_MATH_INLINE,
                "displayMath" to LatexSyntaxHighlighter.DISPLAY_MATH,
                "inlineMath" to LatexSyntaxHighlighter.INLINE_MATH,
                "optionalParam" to LatexSyntaxHighlighter.OPTIONAL_PARAM,
                "comment" to LatexSyntaxHighlighter.COMMENT
        )
    }

    override fun getIcon() = TexifyIcons.LATEX_FILE!!

    override fun getHighlighter() = LatexSyntaxHighlighter()

    override fun getDemoText() = """
                |%
                |%  An amazing example for LaTeX.
                |%
                |\documentclass[<optionalParam>12pt,a4paper</optionalParam>]{article}
                |
                |% Package imports.
                |\usepackage{amsmath}
                |\usepackage{comment}
                |
                |% Start document.
                |\begin{document}
                |
                |    % Make title.
                |    \title{A Very Simple \LaTeXe{} Template}
                |    \author{
                |            Henk-Jan\\Department of YUROP\\University of Cheese\\
                |            Windmill City, 2198 AL, \underline{Tulipa}
                |    }
                |    \date{\today}
                |    \maketitle
                |
                |    % Start writing amazing stuff now.
                |    \begin{abstract}
                |        This is the paper's abstract \ldots.
                |    \end{abstract}
                |
                |    <comment>\begin{comment}
                |        Yes, even comment environments get highlighted.
                |    \end{comment}</comment>
                |
                |    \section{Introduction}\label{sec:introduction}
                |    This is time for all good men to come to the aid of their party!
                |
                |    \paragraph{Mathematics}
                |    Please take a look at the value of <inlineMath>${'$'}x <inlineCommand>\times</inlineCommand>
                |    <inlineCommand>\frac</inlineCommand>{5}{<inlineCommand>\sqrt</inlineCommand>{3}}${'$'}</inlineMath> in the following equation:
                |    <displayMath>\[
                |       x <displayCommand>\times</displayCommand> <displayCommand>\frac</displayCommand>{5}{<displayCommand>\sqrt</displayCommand>{3}} = y <displayCommand>\cdot</displayCommand> <displayCommand>\max\left</displayCommand>{ 4, <displayCommand>\alpha</displayCommand>, 6 <displayCommand>\right</displayCommand>} +
                |           <displayCommand>\sqrt</displayCommand>[<optionalParam>1234</optionalParam>]{5678}.
                |    \]</displayMath>
                |
                |    \section{More work}\label{sec:moreWork}
                |    A much longer \LaTeXe{} example was written by Henk-Jan~\cite{Gil:02}. But
                |    we can also just do some more epic plugin showoffy stuff like
                |    <displayMath>\begin{align}
                |       <displayCommand>\text</displayCommand>{Stuff here is also highlighted, and also }
                |       <displayCommand>\sum</displayCommand>_{i=0}^n <displayCommand>\left</displayCommand>( i <displayCommand>\right</displayCommand>)
                |    \begin{align}</displayMath>
                |
                |    \section{Results}\label{sec:results}
                |    In this section we describe the results. So basically <inlineMath>${'$'}x${'$'}</inlineMath> but maybe
                |    also <inlineMath>${'$'}<inlineCommand>\hat</inlineCommand>{x}^{2y}${'$'}</inlineMath>.
                |
                |    \section{Conclusions}\label{sec:conclusions}
                |    We worked hard, and achieved very little. Or did we?
                |
                |    % Another extremely descriptive comment.
                |    \bibliographystyle{abbrv}
                |    \bibliography{main}
                |
                |\end{document}
        """.trimMargin()

    override fun getAdditionalHighlightingTagToDescriptorMap() = DEMO_TAGS

    override fun getAttributeDescriptors() = DESCRIPTORS

    override fun getColorDescriptors(): Array<out ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY!!

    override fun getDisplayName() = "LaTeX"
}
