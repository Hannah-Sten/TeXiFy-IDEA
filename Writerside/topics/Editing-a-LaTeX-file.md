# Editing a LaTeX file

## Unicode support

IntelliJ supports Unicode, see for example [https://blog.jetbrains.com/idea/2013/03/use-the-utf-8-luke-file-encodings-in-intellij-idea/](https://blog.jetbrains.com/idea/2013/03/use-the-utf-8-luke-file-encodings-in-intellij-idea/)

Note that if the LaTeX log output contains characters in an incorrect encoding on Windows, you can fix this by going to <ui-path>Help | Edit Custom VM Options</ui-path> and add `-Dfile.encoding=UTF-8`, then restart your IDE.

Also see the [Unicode inspection](Probable-bugs#Unsupported-Unicode-character).

## Line commenting

By default, the shortcut kbd:[Ctrl + /] will add a comment (`%`) character to the beginning of the line.
When multiple lines are selected, these will all be commented.

Note that the block comment shortcut kbd:[Ctrl + Shift + /] should not be used, as LaTeX does not have any block comments by itself.

Also see the [Option to start a comment at the same indentation as normal text](Code-style-settings#indent-comment).

## Shortcuts for styling text

See [Menu Entries](Menu-entries.md)

## Automatic package importing

By default, TeXiFy will automatically insert package dependencies when you use autocomplete on a command of which the dependency is known.

An example is the `align` environment, which is provided by the `amsmath` package.
If you use the autocomplete to type `\begin{align} ... \end{align}`,you will see in the autocomplete window that `align` has the `amsmath` dependency.
If you select it, then `\usepackage{amsmath}` will be inserted automatically, as you can see below.

![package-import](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Writing/figures/package-import.gif)

Note that currently only a few common commands and packages are supported.

## Subfiles
_Since b0.6.8_

TeXiFy support using the `subfiles` package.
This means that package imports will be placed in the main file when you are writing in a subfile, and imports will be detected correctly.
An example of using the subfile package would be:

```latex
\documentclass{article}
\usepackage{amsmath}
\usepackage{subfiles}
\begin{document}
    \section{One}\label{sec:one}
    \subfile{section1}
\end{document}
```

`section1.tex`:

```latex
\documentclass[main.tex]{subfiles}

\begin{document}

    \begin{align}
        \xi
    \end{align}

\end{document}


```
