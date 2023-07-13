By pressing <shortcut>Ctrl + B</shortcut> on a reference to a label, or a citation of a bibtex entry, your cursor will go to the declaration of the reference or citation.
In general, you can go back to your previous cursor location with <shortcut>Ctrl + Alt + &lt;-</shortcut>

This also works with usages of commands defined with `\newcommand` definitions (in your fileset, not in LaTeX packages), but only if your command definition includes braces, like `\newcommand{\mycommand}{definition}`

![go-to-label-declaration](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Navigation/figures/go-to-label-declaration.gif)
![go-to-cite-declaration](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Navigation/figures/go-to-cite-declaration.gif)

Note that your cursor has to be placed somewhere in the parameter of the referencing command, not on the command itself.
For example in the case of `\re|f{mylabel}` where `|` denotes the cursor, then it will not work, but for `\ref{my|label}` it will.

## Peek definition

For the elements for which Go To Declaration is implemented, you also have a Peek Definition, by default <shortcut>Ctrl + Shift + I</shortcut>.
For more information, see [https://www.jetbrains.com/help/idea/viewing-reference-information.html#](https://www.jetbrains.com/help/idea/viewing-reference-information.html#)

![peek-definition](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Navigation/figures/peek-definition.png)

## Using the xr package

TeXiFy also supports the xr package with respect to label reference resolving and autocompletion.
This also includes related features like 'find usages', as in the screenshot below.
As you can see, using a prefix also works as expected.

![externaldocument-usages](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Navigation/figures/externaldocument-usages.png)

An example of using the xr package is as follows.
Note that you need to compile `presentation.tex` before `main.tex`, and that you can do so by specifying the run configuration for `presentation.tex` in the 'Before launch' section in the run configuration for `main.tex`.

`main.tex`:
```latex
\documentclass[11pt]{article}
\usepackage{xr}
\usepackage{xr-hyper}
\usepackage{hyperref}

% Note: name of the aux file in the output directory, so no full path
\externaldocument[P-]{presentation}

\begin{document}

    Slide~\ref{P-slide:first}

\end{document}
```

`presentation.tex`
```latex
\documentclass{beamer}
\usepackage{hyperref}

\begin{document}
    \begin{frame}
        This is not slide~\ref{slide:first}.
    \end{frame}
    \begin{frame}
        \label{slide:first}
        This is slide~\ref{slide:first}.
    \end{frame}
\end{document}

```
