## Math functions in `\text`
_Since b0.7.3_

Math functions using `\text{myoperator}` can be replaced by their dedicated function (`\myoperator`), for example `\text{sin}` by `\sin`.

## Grouped superscript and subscript

If you write `\xi_ij` then you probably meant to write `\xi_{ij}`, because `\xi_ij` will be interpreted as `\xi_{i}j`.
If not, you probably should have written `\xi_i j` instead.

## Gather equations
Multiple display math equations (which are surrounded by `\[..\]`) can be grouped into the `gather` environment.

## Figure not referenced

_Since b0.6.6_

You should always reference your figures, otherwise you have figure floating around in your document with no connection to the text.
This is undesirable, because then it may not be clear why the figure is there.

Because figures float around (LaTeX determines the location automatically) you should never refer to figures as 'The figure below'.

This inspection includes a quick fix to safe delete the figure environment.

**Example file which triggers the inspection.**

```latex
\documentclass{article}

\begin{document}

    \begin{figure}
        A figuring float.
        \label{fig:not-referenced}
    \end{figure}

\end{document}
```

## Missing labels
TeXiFy checks for missing labels for sections, chapters and environments. If any of these elements misses a label, a weak warning is displayed together with a quickfix to insert a label.

![Missing label quickfix](https://user-images.githubusercontent.com/7955528/73370333-5a045000-42b4-11ea-8148-971fb0a5858b.png)

The inserted label is guaranteed to be unique and follows the label conventions. For sections and chapters the label is inserted after the section, for environments the label is inserted in the environment. If the environment already contains a `\caption`, the label is inserted after the `\caption`. Otherwise the label would label the surrounding environment.

You can configure in Settings which elements need a label, see [Conventions settings.](Features#Conventions)

## Label conventions

TeXiFy defines label conventions to specify how labels should typically look like and includes an inspection to verify that all labels adhere to the convention. Furthermore, labels inserted as part of a quickfix are also named accordingly. The convention states, that labels should have a prefix depending on the command or environment they label. For example, a label for a figure has the prefix `fig:`.

The prefixes can be configured in settings, see [Conventions settings.](Features#Conventions)

## Start sentences on a new line

In general, we recommend starting every sentence on a new line, especially because this works nicer with version control systems, which work per line.
That also makes it more meaningful to reference a certain line in a document.
It also makes it easier to read when soft wrapping is off.

Note that this inspection would trigger on abbreviations like `etc.`.
However, when you use abbreviations like that you should always tell LaTeX to use a normal space instead of an end-of-sentence space, so instead of `This etc. contains a sentence` you should write `This etc.\ contains a sentence`.
(Try it, the spaces are really different in the pdf!)

![link="https://xkcd.com/1285/"](https://imgs.xkcd.com/comics/third_way.png)

## [[ins:eqref]] Use `\eqref{...}` instead of `(\ref{...})`
_Since b0.6.6_

Equations should be referenced using `\eqref{...}` rather than `(\ref{...})` to ensure that all your equation references
are formatted in the same way. This inspection is only triggered when the corresponding label was defined in a math environment.

## [[ins:requirepackage]] Use `\RequirePackage{...}` instead of `\usepackage{...}`
_Since b0.7.8_

While the `\usepackage` and `\RequirePackage` commands do roughly the same thing -- including packages -- it is common practice to use the `\RequirePackage` in class and style files and to use the `\usepackage` command in most other files.

See [Difference between RequirePackage and usepackage](https://tex.stackexchange.com/questions/19919/whats-the-difference-between-requirepackage-and-usepackage) for more information.

## [[ins:documentclass]] File that contains a document environment should contain a `\documentclass` command
_Since b0.7.1_

A LaTeX root file has the following structure:

```latex
\documentclass{article}

% Document preamble

\begin{document}
    % Contents of document.
\end{document}
```

Note that the preamble does ***not*** contain the `\documentclass` command.
To quote The LaTeX Companion:

> Commands placed between `\documentclass` and `\begin{document}` are in the so-called _document preamble_.

When separating the preamble from the root LaTeX file, keep the `\documentclass` command in the root file.
Put the preamble in a `sty` (package) file, e.g. `personal.sty`, and include it in the root document with `\usepackage{personal}`.
When the need arises to specify the document class in the personal package, transform the package to a document class `personal.cls` and include it with `\documentclass{personal}`.

## Might break TeXiFy functionality

If you redefine common commands such as `\begin` or `\newcommand` to do something different, this may confuse TeXiFy and break functionality.
It will probably also confuse anyone (like your future self) who reads your LaTeX.

## [[too-large-section]]Too large section

Similar to [move selection contents to separate file](Intentions#selection-to-file) and [move section contents to separate file](Intentions#move-section-to-file), except that this is a visible warning on a section command whenever a section is too large (currently some thousand characters).
