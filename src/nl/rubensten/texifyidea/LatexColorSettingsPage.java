package nl.rubensten.texifyidea;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

/**
 * @author Sten Wessel
 */
public class LatexColorSettingsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Braces", LatexSyntaxHighlighter.BRACES),
            new AttributesDescriptor("Brackets", LatexSyntaxHighlighter.BRACKETS),
            new AttributesDescriptor("Command", LatexSyntaxHighlighter.COMMAND),
            new AttributesDescriptor("Comment", LatexSyntaxHighlighter.COMMENT),
            new AttributesDescriptor("Inline Math", LatexSyntaxHighlighter.INLINE_MATH),
            new AttributesDescriptor("Display Math", LatexSyntaxHighlighter.DISPLAY_MATH)
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return TexifyIcons.LATEX_FILE;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new LatexSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return "% This is a sample LaTeX input file.  (Version of 12 August 2004.)\n" +
                "%\n" +
                "% A '%' character causes TeX to ignore all remaining text on the line,\n" +
                "% and is used for comments like this one.\n" +
                "\n" +
                "\\documentclass{article}      % Specifies the document class\n" +
                "\n" +
                "                             % The preamble begins here.\n" +
                "\\title{An Example Document}  % Declares the document's title.\n" +
                "\\author{Leslie Lamport}      % Declares the author's name.\n" +
                "\\date{January 21, 1994}      % Deleting this command produces today's date.\n" +
                "\n" +
                "\\newcommand{\\ip}[2]{(#1, #2)}\n" +
                "                             % Defines \\ip{arg1}{arg2} to mean\n" +
                "                             % (arg1, arg2).\n" +
                "\n" +
                "%\\newcommand{\\ip}[2]{\\langle #1 | #2\\rangle}\n" +
                "                             % This is an alternative definition of\n" +
                "                             % \\ip that is commented out.\n" +
                "\n" +
                "\\begin{document}             % End of preamble and beginning of text.\n" +
                "\n" +
                "\\maketitle                   % Produces the title.\n" +
                "\n" +
                "This is an example input file.  Comparing it with\n" +
                "the output it generates can show you how to\n" +
                "produce a simple document of your own.\n" +
                "\n" +
                "\\section{Ordinary Text}      % Produces section heading.  Lower-level\n" +
                "                             % sections are begun with similar \n" +
                "                             % \\subsection and \\subsubsection commands.\n" +
                "\n" +
                "The ends  of words and sentences are marked \n" +
                "  by   spaces. It  doesn't matter how many \n" +
                "spaces    you type; one is as good as 100.  The\n" +
                "end of   a line counts as a space.\n" +
                "\n" +
                "One   or more   blank lines denote the  end \n" +
                "of  a paragraph.  \n" +
                "\n" +
                "Since any number of consecutive spaces are treated\n" +
                "like a single one, the formatting of the input\n" +
                "file makes no difference to\n" +
                "      \\LaTeX,                % The \\LaTeX command generates the LaTeX logo.\n" +
                "but it makes a difference to you.  When you use\n" +
                "\\LaTeX, making your input file as easy to read \n" +
                "as possible will be a great help as you write \n" +
                "your document and when you change it.  This sample \n" +
                "file shows how you can add comments to your own input \n" +
                "file.\n" +
                "\n" +
                "Because printing is different from typewriting,\n" +
                "there are a number of things that you have to do\n" +
                "differently when preparing an input file than if\n" +
                "you were just typing the document directly.\n" +
                "Quotation marks like\n" +
                "       ``this'' \n" +
                "have to be handled specially, as do quotes within\n" +
                "quotes:\n" +
                "       ``\\,`this'            % \\, separates the double and single quote.\n" +
                "        is what I just \n" +
                "        wrote, not  `that'\\,''.  \n" +
                "\n" +
                "Dashes come in three sizes: an \n" +
                "       intra-word \n" +
                "dash, a medium dash for number ranges like \n" +
                "       1--2, \n" +
                "and a punctuation \n" +
                "       dash---like \n" +
                "this.\n" +
                "\n" +
                "A sentence-ending space should be larger than the\n" +
                "space between words within a sentence.  You\n" +
                "sometimes have to type special commands in\n" +
                "conjunction with punctuation characters to get\n" +
                "this right, as in the following sentence.\n" +
                "       Gnats, gnus, etc.\\ all  % `\\ ' makes an inter-word space.\n" +
                "       begin with G\\@.         % \\@ marks end-of-sentence punctuation.\n" +
                "You should check the spaces after periods when\n" +
                "reading your output to make sure you haven't\n" +
                "forgotten any special cases.  Generating an\n" +
                "ellipsis\n" +
                "       \\ldots\\               % `\\ ' is needed after `\\ldots' because TeX \n" +
                "                             % ignores spaces after command names like \\ldots " +
                "\n" +
                "                             % made from \\ + letters.\n" +
                "                             %\n" +
                "                             % Note how a `%' character causes TeX to ignore \n" +
                "                             % the end of the input line, so these blank lines " +
                "\n" +
                "                             % do not start a new paragraph.\n" +
                "                             %\n" +
                "with the right spacing around the periods requires\n" +
                "a special command.\n" +
                "\n" +
                "\\LaTeX\\ interprets some common characters as\n" +
                "commands, so you must type special commands to\n" +
                "generate them.  These characters include the\n" +
                "following:\n" +
                "       \\$ \\& \\% \\# \\{ and \\}.\n" +
                "\n" +
                "In printing, text is usually emphasized with an\n" +
                "       \\emph{italic}  \n" +
                "type style.  \n" +
                "\n" +
                "\\begin{em}\n" +
                "   A long segment of text can also be emphasized \n" +
                "   in this way.  Text within such a segment can be \n" +
                "   given \\emph{additional} emphasis.\n" +
                "\\end{em}\n" +
                "\n" +
                "It is sometimes necessary to prevent \\LaTeX\\ from\n" +
                "breaking a line where it might otherwise do so.\n" +
                "This may be at a space, as between the ``Mr.''\\ and\n" +
                "``Jones'' in\n" +
                "       ``Mr.~Jones'',        % ~ produces an unbreakable interword space.\n" +
                "or within a word---especially when the word is a\n" +
                "symbol like\n" +
                "       \\mbox{\\emph{itemnum}} \n" +
                "that makes little sense when hyphenated across\n" +
                "lines.\n" +
                "\n" +
                "Footnotes\\footnote{This is an example of a footnote.}\n" +
                "pose no problem.\n" +
                "\n" +
                "\\LaTeX\\ is good at typesetting mathematical formulas\n" +
                "like\n" +
                "       \\( x-3y + z = 7 \\) \n" +
                "or\n" +
                "       \\( a_{1} > x^{2n} + y^{2n} > x' \\)\n" +
                "or  \n" +
                "       \\( \\ip{A}{B} = \\sum_{i} a_{i} b_{i} \\).\n" +
                "or  \n" +
                "       $34 + 2y^2 - \\sqrt{x} = -3$\n" +
                "The spaces you type in a formula are \n" +
                "ignored.  Remember that a letter like\n" +
                "       $x$                   % $ ... $  and  \\( ... \\)  are equivalent\n" +
                "is a formula when it denotes a mathematical\n" +
                "symbol, and it should be typed as one.\n" +
                "\n" +
                "\\section{Displayed Text}\n" +
                "\n" +
                "Text is displayed by indenting it from the left\n" +
                "margin.  Quotations are commonly displayed.  There\n" +
                "are short quotations\n" +
                "\\begin{quote}\n" +
                "   This is a short quotation.  It consists of a \n" +
                "   single paragraph of text.  See how it is formatted.\n" +
                "\\end{quote}\n" +
                "and longer ones.\n" +
                "\\begin{quotation}\n" +
                "   This is a longer quotation.  It consists of two\n" +
                "   paragraphs of text, neither of which are\n" +
                "   particularly interesting.\n" +
                "\n" +
                "   This is the second paragraph of the quotation.  It\n" +
                "   is just as dull as the first paragraph.\n" +
                "\\end{quotation}\n" +
                "Another frequently-displayed structure is a list.\n" +
                "The following is an example of an \\emph{itemized}\n" +
                "list.\n" +
                "\\begin{itemize}\n" +
                "   \\item This is the first item of an itemized list.\n" +
                "         Each item in the list is marked with a ``tick''.\n" +
                "         You don't have to worry about what kind of tick\n" +
                "         mark is used.\n" +
                "\n" +
                "   \\item This is the second item of the list.  It\n" +
                "         contains another list nested inside it.  The inner\n" +
                "         list is an \\emph{enumerated} list.\n" +
                "         \\begin{enumerate}\n" +
                "            \\item This is the first item of an enumerated \n" +
                "                  list that is nested within the itemized list.\n" +
                "\n" +
                "            \\item This is the second item of the inner list.  \n" +
                "                  \\LaTeX\\ allows you to nest lists deeper than \n" +
                "                  you really should.\n" +
                "         \\end{enumerate}\n" +
                "         This is the rest of the second item of the outer\n" +
                "         list.  It is no more interesting than any other\n" +
                "         part of the item.\n" +
                "   \\item This is the third item of the list.\n" +
                "\\end{itemize}\n" +
                "You can even display poetry.\n" +
                "\\begin{verse}\n" +
                "   There is an environment \n" +
                "    for verse \\\\             % The \\\\ command separates lines\n" +
                "   Whose features some poets % within a stanza.\n" +
                "   will curse.   \n" +
                "\n" +
                "                             % One or more blank lines separate stanzas.\n" +
                "\n" +
                "   For instead of making\\\\\n" +
                "   Them do \\emph{all} line breaking, \\\\\n" +
                "   It allows them to put too many words on a line when they'd rather be \n" +
                "   forced to be terse.\n" +
                "\\end{verse}\n" +
                "\n" +
                "Mathematical formulas may also be displayed.  A\n" +
                "displayed formula \n" +
                "is \n" +
                "one-line long; multiline\n" +
                "formulas require special formatting instructions.\n" +
                "   \\[  \\ip{\\Gamma}{\\psi'} = x'' + y^{2} + z_{i}^{n}\\]\n" +
                "Don't start a paragraph with a displayed equation,\n" +
                "nor make one a paragraph by itself.\n" +
                "\n" +
                "\\end{document}               % End of document.";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @NotNull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @NotNull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "LaTeX";
    }
}
