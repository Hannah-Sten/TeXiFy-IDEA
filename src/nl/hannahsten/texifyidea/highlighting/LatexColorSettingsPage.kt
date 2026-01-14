package nl.hannahsten.texifyidea.highlighting

import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import nl.hannahsten.texifyidea.TexifyIcons

/**
 * @author Hannah Schellekens, Sten Wessel
 */
class LatexColorSettingsPage : ColorSettingsPage {

    /*
     * You can group multiple descriptors by prefixing it with the group name and '//'. FYI.
     */
    val descriptors = arrayOf(
        AttributesDescriptor("Braces", LatexSyntaxHighlighter.BRACES),
        AttributesDescriptor("Brackets", LatexSyntaxHighlighter.BRACKETS),
        AttributesDescriptor("Commands//Optional parameters", LatexSyntaxHighlighter.OPTIONAL_PARAM),
        AttributesDescriptor("Commands//Equals sign in key-value pairs", LatexSyntaxHighlighter.SEPARATOR_EQUALS),
        AttributesDescriptor("Commands//Commands", LatexSyntaxHighlighter.COMMAND),
        AttributesDescriptor("Commands//User defined commands", LatexSyntaxHighlighter.USER_DEFINED_COMMAND),
        AttributesDescriptor("Commands//Commands in inline math mode", LatexSyntaxHighlighter.COMMAND_MATH_INLINE),
        AttributesDescriptor("Commands//Commands in display math mode", LatexSyntaxHighlighter.COMMAND_MATH_DISPLAY),
        AttributesDescriptor("Commands//Stars", LatexSyntaxHighlighter.STAR),
        AttributesDescriptor("Comments//Regular comment", LatexSyntaxHighlighter.COMMENT),
        AttributesDescriptor("Comments//Magic comment", LatexSyntaxHighlighter.MAGIC_COMMENT),
        AttributesDescriptor("References//Label definition", LatexSyntaxHighlighter.LABEL_DEFINITION),
        AttributesDescriptor("References//Label reference", LatexSyntaxHighlighter.LABEL_REFERENCE),
        AttributesDescriptor("References//Bibliography item", LatexSyntaxHighlighter.BIBLIOGRAPHY_DEFINITION),
        AttributesDescriptor("References//Citation", LatexSyntaxHighlighter.BIBLIOGRAPHY_REFERENCE),
        AttributesDescriptor("Math//Inline math", LatexSyntaxHighlighter.INLINE_MATH),
        AttributesDescriptor("Math//Display math", LatexSyntaxHighlighter.DISPLAY_MATH),
        AttributesDescriptor("Math//Text in math", LatexSyntaxHighlighter.MATH_NESTED_TEXT),

        // Styles
        AttributesDescriptor("Font style//Bold", LatexSyntaxHighlighter.STYLE_BOLD),
        AttributesDescriptor("Font style//Italics", LatexSyntaxHighlighter.STYLE_ITALIC),
        AttributesDescriptor("Font style//Underline", LatexSyntaxHighlighter.STYLE_UNDERLINE),
        AttributesDescriptor("Font style//Strikethrough", LatexSyntaxHighlighter.STYLE_STRIKETHROUGH),
        AttributesDescriptor("Font style//Small capitals", LatexSyntaxHighlighter.STYLE_SMALL_CAPITALS),
        AttributesDescriptor("Font style//Overline", LatexSyntaxHighlighter.STYLE_OVERLINE),
        AttributesDescriptor("Font style//Typewriter", LatexSyntaxHighlighter.STYLE_TYPEWRITER),
        AttributesDescriptor("Font style//Slanted", LatexSyntaxHighlighter.STYLE_SLANTED)
    )

    private val demoTags = mapOf(
        "displayCommand" to LatexSyntaxHighlighter.COMMAND_MATH_DISPLAY,
        "inlineCommand" to LatexSyntaxHighlighter.COMMAND_MATH_INLINE,
        "userDefinedCommand" to LatexSyntaxHighlighter.USER_DEFINED_COMMAND,
        "displayMath" to LatexSyntaxHighlighter.DISPLAY_MATH,
        "inlineMath" to LatexSyntaxHighlighter.INLINE_MATH,
        "textInMath" to LatexSyntaxHighlighter.MATH_NESTED_TEXT,
        "optionalParam" to LatexSyntaxHighlighter.OPTIONAL_PARAM,
        "comment" to LatexSyntaxHighlighter.COMMENT,
        "magicComment" to LatexSyntaxHighlighter.MAGIC_COMMENT,
        "equalsSeparator" to LatexSyntaxHighlighter.SEPARATOR_EQUALS,
        "labelDefinition" to LatexSyntaxHighlighter.LABEL_DEFINITION,
        "reference" to LatexSyntaxHighlighter.LABEL_REFERENCE,
        "bibliographyDefinition" to LatexSyntaxHighlighter.BIBLIOGRAPHY_DEFINITION,
        "bibliographyReference" to LatexSyntaxHighlighter.BIBLIOGRAPHY_REFERENCE,
        "styleBold" to LatexSyntaxHighlighter.STYLE_BOLD,
        "styleItalic" to LatexSyntaxHighlighter.STYLE_ITALIC,
        "styleUnderline" to LatexSyntaxHighlighter.STYLE_UNDERLINE,
        "styleStrikethrough" to LatexSyntaxHighlighter.STYLE_STRIKETHROUGH,
        "styleSmallCapitals" to LatexSyntaxHighlighter.STYLE_SMALL_CAPITALS,
        "styleOverline" to LatexSyntaxHighlighter.STYLE_OVERLINE,
        "styleTypewriter" to LatexSyntaxHighlighter.STYLE_TYPEWRITER,
        "styleSlanted" to LatexSyntaxHighlighter.STYLE_SLANTED
    )

    override fun getIcon() = TexifyIcons.LATEX_FILE

    override fun getHighlighter() = LatexSyntaxHighlighter()

    override fun getDemoText() =
        $$"""
                |<magicComment>%! Compiler = pdfLaTeX</magicComment>
                |%  An amazing example for LaTeX.
                |\documentclass[<optionalParam>12pt,a4paper</optionalParam>]{article}
                |
                |% Package imports.
                |\usepackage{amsmath}
                |\usepackage{comment}
                |\usepackage{listings}
                |
                |\lstset{language<equalsSeparator>=</equalsSeparator>Kotlin}
                |
                |\newcommand{\betterphi}{\varphi}
                |\newcommand{\myphi}{<userDefinedCommand>\betterphi</userDefinedCommand>}
                |
                |% Start document.
                |<magicComment>%! Suppress = NonBreakingSpace</magicComment>
                |\begin{document}
                |
                |    % Make title.
                |    \title{A Very Simple \LaTeXe{} Template}
                |    \author{
                |            Henk-Jan\\Department of YUROP\\University of Cheese\\
                |            Windmill City, 2198 AL, \underline{<styleUnderline>Tulipa</styleUnderline>}
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
                |    \section{Introduction}\label{<labelDefinition>sec:introduction</labelDefinition>}
                |    This is time for all good men to come to the aid of
                |    their party!
                |    For the end see~\ref{<reference>sec:conclusions</reference>}.
                |    
                |    \begin{lstlisting}[label<equalsSeparator>=</equalsSeparator>test]
                |       println("Hello world!")
                |    \end{lstlisting}
                |
                |    \paragraph*{Mathematics}
                |    Please take a look at the value of <inlineMath>$x <inlineCommand>\times</inlineCommand>
                |    <inlineCommand>\frac</inlineCommand>{5}{<inlineCommand>\sqrt</inlineCommand>{3}}$</inlineMath> in the following equation:
                |    <displayMath>\[
                |       x <displayCommand>\times</displayCommand> <displayCommand>\frac</displayCommand>{5}{<displayCommand>\sqrt</displayCommand>{3}} = y <displayCommand>\cdot</displayCommand> <displayCommand>\max\left</displayCommand>{ 4, <displayCommand>\alpha</displayCommand>, 6 <displayCommand>\right</displayCommand>} +
                |           <displayCommand>\sqrt</displayCommand>[<optionalParam>1234</optionalParam>]{5678}.
                |    \]</displayMath>
                |
                |    \section{More work}\label{<labelDefinition>sec:moreWork</labelDefinition>}
                |    A much longer \LaTeXe{} example was written by Henk-Jan~\cite{<bibliographyReference>Gil:02</bibliographyReference>}. But
                |    we can also just do some more epic plugin showoffy stuff like
                |    <displayMath>\begin{align}
                |       <displayCommand>\text</displayCommand>{Stuff here is also highlighted, and also }
                |       <displayCommand>\sum</displayCommand>_{i=0}^n <displayCommand>\left</displayCommand>( i <displayCommand>\right</displayCommand>)
                |    \begin{align}</displayMath>
                |
                |    \section{Results}\label{<labelDefinition>sec:results</labelDefinition>}
                |    In this section we describe the results. So basically <inlineMath>$x$</inlineMath> but maybe
                |    also <inlineMath>$<inlineCommand>\hat</inlineCommand>{x}^{2y}<inlineCommand>\text</inlineCommand><textInMath>{if <inlineMath>$x > 0$</inlineMath> }</textInMath>$</inlineMath>.
                |
                |    Also, some text styles:
                |    \textbf{<styleBold>Bold</styleBold>}
                |    \textit{<styleItalic>Italic</styleItalic>}
                |    \overline{<styleOverline>Overline</styleOverline>}
                |    \textsl{<styleSlanted>Slanted</styleSlanted>}
                |    \textsc{<styleSmallCapitals>SMALL CAPITALS</styleSmallCapitals>}
                |    \sout{<styleStrikethrough>Strikethrough</styleStrikethrough>}
                |    \texttt{<styleTypewriter>Typewriter</styleTypewriter>}
                |    \underline{<styleUnderline>Underline</styleUnderline>}
                |
                |    \section{Conclusions}\label{<labelDefinition>sec:conclusions</labelDefinition>}
                |    We worked hard, and achieved very little. Or did we?
                |
                |    % Another extremely descriptive comment.
                |    \bibliographystyle{abbrv}
                |    \bibliography{main}
                |
                |    \begin{thebibliography}{9}
                |        \bibitem{<bibliographyDefinition>latexcompanion</bibliographyDefinition>}
                |        Michel Goossens, Frank Mittelbach, and Alexander Samarin.
                |        \textit{<styleItalic>The \LaTeX\ Companion</styleItalic>}.
                |        Addison-Wesley, Reading, Massachusetts, 1993.
                |    \end{thebibliography}
                |
                |\end{document}
        """.trimMargin()

    override fun getAdditionalHighlightingTagToDescriptorMap() = demoTags

    override fun getAttributeDescriptors() = descriptors

    override fun getColorDescriptors(): Array<out ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY!!

    override fun getDisplayName() = "LaTeX"
}