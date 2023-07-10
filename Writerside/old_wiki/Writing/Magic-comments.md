_Since b0.6.10_

## Compilers
See [Using magic comments to specify the compiler for new run configurations](Compilers#using-magic-comments-to-specify-the-compiler-for-new-run-configurations).

## Root file

If TeXiFy does not guess your root file(s) correctly, you can help TeXiFy by using the `root` magic comment to point TeXiFy to a root file.
For example, use `%! root = main.tex` in a file that is included by `main.tex`, when TeXiFy cannot figure out that `main.tex` is a root file of this file.

## Language injection

See [Language injection](Language-injection).

## Custom preamble for math and tikz preview

See [Preview](Preview).

## Switching parser off and on

If you want to temporarily switch off the parser for a part of your LaTeX, for example because there is a parse error which is causing other problems in your files, you can use the magic comments `%! parser = off` and `%! parser = on` to avoid parsing the text between these two comments.
The syntax `% !TeX parser = off` is also supported.

## Custom folding regions

You can use either `%! region My description` and `%! endregion` or NetBeans-style `%! <editor-fold desc="My Description">` and `%! <editor-fold>` magic comments to specify custom folding regions.
For more information, see [https://blog.jetbrains.com/idea/2012/03/custom-code-folding-regions-in-intellij-idea-111/](https://blog.jetbrains.com/idea/2012/03/custom-code-folding-regions-in-intellij-idea-111/) and [https://www.jetbrains.com/help/idea/code-folding-settings.html](https://www.jetbrains.com/help/idea/code-folding-settings.html)

## Fake sections

Use `%! fake section` to introduce a fake section which can be folded like any other section.
Here, `section` can be one of `part`, `chapter`, `section`, `subsection`, `subsubsection`, `paragraph`, or `subparagraph`.
Fake sections can also have a title, so `%! fake subsection Introduction part 1` is valid.

Note: if you feel you need to fold code because the file is too big or you lose overview, you probably should split it up into smaller files.
See [https://blog.codinghorror.com/the-problem-with-code-folding/](https://blog.codinghorror.com/the-problem-with-code-folding/)