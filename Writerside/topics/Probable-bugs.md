# Probable bugs

## Unsupported Unicode character

This inspections highlights Unicode characters which are likely to cause incorrect output if you have not explicitly chosen the right packages and compiler.

In general, you should use LuaLaTeX or XeLaTeX when working with Unicode characters.

An example file which uses Unicode characters and is compilable with LuaLaTeX:

```latex
\documentclass{article}

% For this document you may need the following (texlive) packages: luatex luaotfload fontspec babel-greek greek-fontenc gfsporson

\usepackage[no-math]{fontspec}

\usepackage[main=english,greek]{babel}
\languageattribute{greek}{ancient}

\newfontfamily\greekfont{GFS Porson}[Ligatures=TeX,%
ItalicFont = GFSPorson-Regular,%
SmallCapsFont = GFSPorson-Regular]

\addto\extrasgreek{\greekfont}

\newcommand\textgreek[1]{\foreignlanguage{greek}{#1}}

\begin{document}
    English and \textgreek{ελληνικά} and {\greekfont πιο ελληνικό}
\end{document}
```

Another example, tested with XeLaTeX:

```latex
\documentclass{article}
\usepackage{fontspec}

% Download from https://www.fontsquirrel.com/fonts/computer-modern and put ttf files in /usr/share/fonts/TTF
\setmainfont{CMU Serif}

\begin{document}
    This is a paragraph with a PI (π) in it, which looks different than a $\pi$.
\end{document}
```

### Quick fixes

#### Escape Unicode character

In some cases, there is a plain LaTeX equivalent available.
This will change for example `ö` into `\"o`.

Note that since TeX Live 2018 this is unnecessary: UTF-8 is assumed as the default input encoding (source: [https://tex.stackexchange.com/a/370280/98850](https://tex.stackexchange.com/a/370280/98850)).

#### Include Unicode support packages

This will load

```latex
\usepackage[T1]{fontenc}
\usepackage[utf8]{inputenc}
```

Note that since TeX Live 2018 loading `inputenc` does not solve anything: UTF-8 is assumed as the default input encoding (source: https://tex.stackexchange.com/a/370280/98850).

Also note that in general it is better to use `\usepackage[T1]{fontenc}`, see [https://tex.stackexchange.com/questions/664/why-should-i-use-usepackaget1fontenc](https://tex.stackexchange.com/questions/664/why-should-i-use-usepackaget1fontenc)

#### Change compiler compatibility

Opens the settings page with the [option to change compiler for which to check compatibility](Run-configuration-settings.md#switching-compilers).

## File argument should not include the extension

Some commands with which you include files, especially `\bibliography` and `\include`, should not include the file extension.
For example if you want to include a file `section1.tex`, then `\include{section1.tex}` will not work.

## File argument should include the extension

Some commands with which you include files, especially `\addbibresource`, should include the file extension.
For example if you want to include a bibliography in `references.bib` then `\addbibresource{references}` will not work, but `\addbibresource{references.bib}` will.

## Missing documentclass
A LaTeX file that is the root of a document should contain a `\documentclass{...}`.

This inspection is off by default.

## Missing document environment
A LaTeX file that is the root of a document should contain a `document` environment.

This inspection is off by default.

## Unresolved references

Reports references that could not be resolved, for example to labels or citations that don’t exist.

## Non matching environment commands

The environment name in the `\begin` command should match the name in the `\end` command.

## Open if-then-else control sequence

This inspection warns for `\if` commands (and variations) which are not closed with the corresponding `\fi`.

## File not found

If a Latex command takes a path argument TeXiFy checks whether the file or path exists and throws an error if not.

## Absolute path not allowed

With some special commands absolute paths are not allowed. E.g. \include and \includeonly.
A error is thrown if you still enter an absolute path.

## Inclusion loops

When two files include each other, this will be detected.
On one of the inclusions a warning will be shown.

## Nested includes

You cannot use an `\include` command in file included by a `\include` command.

## Label is before caption

A label command in an environment (e.g., a figure or table environment) should go after the caption.
This inspection shows a weak warning when the order is the wrong way around, and a quickfix is available to swap the commands.

Currently this inspection will only trigger when the label and caption are directly next to each other (only whitespace inbetween).

## Unescaped `#`, `&` and `_` symbol
_Since b0.6.10_

When using a `#` symbol outside of a command definition, an `&` outside a tabular environment or a `_` anywhere, it should be escaped with a backslash, like `\&`.

## Multiple \graphicspath definitions

If you have multiple `\graphicspath` commands, only the last one that LaTeX finds defines the graphics path, so you should remove the other one.

## Relative path to parent is not allowed when using BIBINPUTS

The `BIBINPUTS` environment variable cannot handle paths which start with `../`  in the `\bibliography` command, e.g. `\bibliography{../mybib}`.
Solution: set the `BIBINPUTS` path to the parent and use `\bibliography{mybib}` instead (or use a "fake" subfolder and do `\bibliography{fake/../../mybib}`).
This solution can be applied using the quickfix for this inspection.
See [https://tex.stackexchange.com/questions/406024/relative-paths-with-bibinputs](https://tex.stackexchange.com/questions/406024/relative-paths-with-bibinputs)

[Edit March 2023] This issue might not exist anymore, perhaps it has been fixed in bibtex, so the inspection is disabled.

## Command is not defined anywhere

This inspection complains when you use a LaTeX command that is not defined anywhere.
The inspection checks files in the file set, packages and document classes.
It is disabled by default, because many commands from packages are unknown to TeXiFy, usually because they are defined in a way that the code which searches for command definitions doesn’t handle yet.
Therefore, if you find such a command that is defined but this inspection cannot find it, please report it to the issue tracker.

## Suspicious Section formatting

In a `\section` title, if you use formatting like `~` or `\\`, you should provide an explicit entry for the table of contents to avoid messing up the formatting there.
Also see the LaTeX Companion:

> If you try to advise TeX on how to split the heading over a few lines using the '~' symbol so the '\' command, then side effects may result when formatting the table of contents or generating the running head. In this case the simplest solution is to repeat the heading text without the specific markup in the optional parameter of the sectioning command.
