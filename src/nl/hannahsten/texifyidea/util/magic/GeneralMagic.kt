package nl.hannahsten.texifyidea.util.magic

import com.intellij.codeInspection.LocalQuickFix
import org.intellij.lang.annotations.Language

object GeneralMagic {

    val noQuickFix: LocalQuickFix? = null

    val latexDemoText =
        """
            |%
            |% An amazing example for LaTeX.
            |%
            |\documentclass[<optionalParam>12pt,a4paper</optionalParam>]{article}
            |
            |% Package imports.
            |\usepackage{amsmath}
            |\usepackage{listings}
            |
            |% Document wide TikZ settings.
            |\tikzset{
            |   mystyle/.style={
            |        draw,
            |        circle,
            |        label={[fill=yellow]0:#1}
            |    }
            |}
            |
            |% Very incomplete Kotlin definition for listings.
            |\lstdefinelanguage{Kotlin}{
            |basicstyle={\ttfamily},
            |    keywords={fun, if, else, this, let},
            |    keywordstyle={\color{orange!80!red}},
            |}
            |
            |% Define title.
            |\title{A Very Simple \LaTeXe{} Template}
            |\author{
            |        Henk-Jan\\Department of YUROP\\University of Cheese\\
            |        Windmill City, 2198 AL, \underline{Tulipa}
            |}
            |\date{\today}
            |
            |% Start document.
            |\begin{document}
            |    \maketitle
            |
            |    % Start writing amazing stuff now.
            |    \begin{abstract}
            |        This is the paper's abstract.
            |        In this paper, we do basically nothing.
            |    \end{abstract}
            |
            |    \section{Introduction}\label{sec:introduction}
            |    This is time for all good women to come to the aid of their party!
            |
            |    \section{Mathematics}\label{sec:mathematics}
            |    To start the party, as ``announced'' in Section~\ref{sec:introduction}, please take a look at the value of <inlineMath>${'$'}x <inlineCommand>\times</inlineCommand>
            |    <inlineCommand>\frac</inlineCommand>{5}{<inlineCommand>\sqrt</inlineCommand>{3}}$</inlineMath> in the following equation:
            |    <displayMath>\[
            |       x <displayCommand>\times</displayCommand> <displayCommand>\frac</displayCommand>{5}{<displayCommand>\sqrt</displayCommand>{3}} = y <displayCommand>\cdot</displayCommand> <displayCommand>\max\left</displayCommand>{ 4, <displayCommand>\alpha</displayCommand>, 6 <displayCommand>\right</displayCommand>} +
            |           <displayCommand>\sqrt</displayCommand>[<optionalParam>1234</optionalParam>]{5678}.
            |    \]</displayMath>
            |    
            |    \paragraph{Programming}
            |    % @formatter:off
            |    \begin{lstlisting}[language=Kotlin]
            |fun Int?.ifPositiveAddTwo(): Int =
            |        this?.let {
            |            if (this >= 0) this + 2
            |            else this
            |        } ?: 0
            |    \end{lstlisting}
            |    % @formatter:on
            |
            |    \subsection{More work}\label{subsec:moreWork}
            |    A much longer \LaTeXe{} example was written by Henk-Jan~\cite{Gil:02}.
            |    But we can also just do some more epic plugin showoffy stuff like
            |    <displayMath>\begin{align}
            |       <displayCommand>\text</displayCommand>{Stuff here is also highlighted, and also }
            |       <displayCommand>\sum</displayCommand>_{i=0}^n <displayCommand>\left</displayCommand>( i <displayCommand>\right</displayCommand>)
            |    \end{align}</displayMath>
            |
            |    \section{Results}\label{sec:results}
            |    In this section we describe the results. 
            |    So basically <inlineMath>${'$'}x$</inlineMath> but maybe also <inlineMath>$<inlineCommand>\hat</inlineCommand>{x}^{2y}$</inlineMath>.
            |
            |    \section{Conclusions}\label{sec:conclusions}
            |    We worked hard, and achieved very little. 
            |    Or did we?
            |
            |    % Another extremely descriptive comment.
            |    \bibliographystyle{abbrv}
            |    \bibliography{main}
            |
            |\end{document}
        """.trimMargin()

    @Language("Bibtex")
    val bibtexDemoText =
        """
            % I am a BibTeX comment.
            @article{greenwade1993,
                author  = "George D. Greenwade",
                title   = "The {C}omprehensive {T}ex {A}rchive {N}etwork ({CTAN})",
                year    = "1993",
                journal = "TUGBoat",
                volume  = "14",
                number  = "3",
                pages   = "342--351"
            }
            
            I am also a BibTeX comment.
            @book{goossens1993,
                author    = "Michel Goossens and Frank Mittelbach and Alexander Samarin",
                title     = "The LaTeX Companion",
                year      = "1993",
                publisher = "Addison-Wesley",
                address   = "Reading, Massachusetts"
            }
        """.trimIndent()
}