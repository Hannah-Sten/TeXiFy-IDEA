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
