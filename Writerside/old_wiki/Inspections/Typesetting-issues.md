## Nesting of sectioning commands

It is recommended to use proper nesting of `\section`-like commands, for example you should not follow up a `\section` by a `\subsubsection`, but by a `\subsection`.
This inspections includes two quickfixes, to change the sectioning command to the right one (change `\subsubsection` to `\subsection` in this example) and to add the missing sectioning command (`\subsection` in this case).

## Collapse cite commands

`\cite{knuth1990}\cite{goossens1993}` should be replaced with `\cite{knuth1990,goossens1993}`

## En dash in number ranges

Instead of typing `0-9` for a number range, use `0--9` to typeset the right dash.

## Use of `.` instead of `\cdot`

When multiplying numbers, use `\cdot` instead of `.`.

## Use of `x` instead of `\times`

When multiplying numbers, use `2 \times 4` instead of `2x4`.

## Vertically uncentered colon

Instead of `:=`, use `\coloneqq` for better vertical centering of the colon.

## Insert `\qedhere` in trailing displaymath environment

If you end a `proof` environment with a displaymath environment, the qed symbol will appear after the displaymath which is one line too low, so you should use `\qedhere` in the displaymath environment to fix that.

## Dotless versions of i and j must be used with diacritics

When you use diacritics like `\^` on an i or j, you should use the dotless version for better readability.
For example, instead of `\^i` write `\^{\i}`.

## Enclose high commands with `\leftX..\rightX`

Expressions which take up more vertical line space, should also be enclosed with larger parentheses.
For example, instead of `(\frac 1 2)` write `\left(\frac 1 2\right)`.

## Citations must be placed before interpunction

Use `Sentence~\cite{knuth1990}.` and not `Sentence.~\cite{knuth1990}`

# Spacing

## Non-escaped common math operators

Math functions like `sin` and `cos` should be escaped to LaTeX commands in math, so `\sin` and `\cos`, otherwise LaTeX will interpret this as the letter `s` times the letter `i` times the letter `n`, resulting in different spacing (try it).

## Non-breaking spaces before references

Before a `\ref`-like command there should be a non-breaking space `~`, to avoid a line break just before the reference which would make the sentence harder to read.

## Ellipsis with `...` instead of `\ldots` or `\dots`

Use a `\dots`-like command instead of `...` for better spacing.

## Normal space after abbreviation

After an abbreviation like `e.g.` you should use a normal space instead of an end-of-sentence space, so use `e.g.\ this` instead of `e.g. this` (arguably you need to type `e.g., this` anyway). If you don’t do this, LaTeX will interpret `e.g.` as the end of a sentence and thus insert a larger space.

## End-of-sentence space after sentences ending with capitals

If you end a sentence with a captial letter, for example like `Then QED. However, next sentence` then LaTeX will interpret `QED.` as an abbreviation and thus typeset a normal space instead of an end-of-sentence space. Use `QED\@.` instead to override this.

## Use the matching amssymb symbol for extreme inequalities

Instead of writing `xref:`[use `\ll` for better spacing. Same for `]` and more variants.

## Incorrectly typeset quotation marks
_Since b0.7.19_

Different characters are used to open and close a quotation. For example `"quotation"` should be typeset as ```quotation''`, and ’quotation'` should be typeset as ``quotation'`.

## Textidote
_Since b0.7.24_

TeXiFy has support for Textidote (https://github.com/sylvainhalle/textidote) as an external linter.
It can be enabled in <ui-path>File | Settings | Languages & Frameworks | TeXiFy</ui-path>, you can also change command line options there.
The warnings will be shown in the IDE, very similar to inspection warnings.
Note that it is an alternative to the Grazie plugin for grammar checking with LanguageTool, it essentially does the same but it is slower.