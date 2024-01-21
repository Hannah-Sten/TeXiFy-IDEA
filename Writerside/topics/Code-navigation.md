# Code navigation

## Structure view

<ui-path>View | Tool Windows | Structure</ui-path> or <shortcut>Alt + 7</shortcut>

The structure view shows all includes, sectioning commands (including proper nesting when chapter, section, subsection etc. are used), command definitions, labels and bibliography items (in `.bib` files).
You can show/hide any of these types in the Structure View at the top.
When you click on an item, it will autoscroll to source by default. You can also autoscroll from source, configurable in the Structure View window.
You can also sort alphabetically.

Note that to use `\chapter` in your document you need to use `\documentclass{book}`, so they will only appear in the structure view if you do have the `book` documentclass.

When using `\newcommand` or variants, we recommend to use braces like `\newcommand{\mycommand}{42}` so it will appear correctly in the structure view.

For more information about the structure view, see [https://www.jetbrains.com/help/idea/structure-tool-window-file-structure-popup.html](https://www.jetbrains.com/help/idea/structure-tool-window-file-structure-popup.html)

![structure view](structure-view.png)

## Clickable links

Web links in `\url` and `\href` commands are clickable using <shortcut>Ctrl + Click</shortcut>.

## Go to declaration for labels, citations and commands {id="go-to-declaration"}

By pressing <shortcut>Ctrl + B</shortcut> on a reference to a label, or a citation of a bibtex entry, your cursor will go to the declaration of the reference or citation.
In general, you can go back to your previous cursor location with <shortcut>Ctrl + Alt + &lt;-</shortcut>

This also works with usages of commands defined with `\newcommand` definitions (in your fileset, not in LaTeX packages), but only if your command definition includes braces, like `\newcommand{\mycommand}{definition}`

![go-to-label-declaration](go-to-label-declaration.gif)

![go-to-cite-declaration](go-to-cite-declaration.gif)

Note that your cursor has to be placed somewhere in the parameter of the referencing command, not on the command itself.
For example in the case of `\re|f{mylabel}` where `|` denotes the cursor, then it will not work, but for `\ref{my|label}` it will.

### Peek definition

For the elements for which Go To Declaration is implemented, you also have a Peek Definition, by default <shortcut>Ctrl + Shift + I</shortcut>.
For more information, see [https://www.jetbrains.com/help/idea/viewing-reference-information.html#](https://www.jetbrains.com/help/idea/viewing-reference-information.html#)

![peek-definition](peek-definition.png)

### Using the xr package

TeXiFy also supports the xr package with respect to label reference resolving and autocompletion.
This also includes related features like 'find usages', as in the screenshot below.
As you can see, using a prefix also works as expected.

![externaldocument-usages](externaldocument-usages.png)

An example of using the xr package is as follows.
Note that you need to compile `presentation.tex` before `main.tex`, and that you can do so by specifying the run configuration for `presentation.tex` in the 'Before launch' section in the run configuration for `main.tex`.

`main.tex`:
<!-- ```latex -->
```
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
<!-- ```latex -->
```
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

## Find usages

_Since b0.6.9_

In general, if [Go to declaration](#go-to-declaration) on something works, then Find Usages will probably work as well.
This holds for at least command definitions, labels and bibtex citations.

### Find usages for commands
As a complement for [Go to declaration](#go-to-declaration), you can easily find usages of LaTeX commands you defined in your document, for example using `\newcommand` or `\DeclareMathOperator`, using <shortcut>Ctrl + B</shortcut>.
Note that this is the same shortcut as for Go to declaration.

![find-usages](find-usages.png)

### Find usages for labels

The same functionality exists for labels, where `\label{mylabel}` is a definition and commands like `\ref{mylabel}` are usages.

Note that your cursor needs to be on the label itself, not on the commands, so `\label{sec:my-<cursor>section}` works but `\lab<cursor>el{sec:my-section}` does not.

Also see [Refactoring](Editing-a-LaTeX-file.md#refactoring).

## File inclusion navigation

Next to commands that include other files, there is a gutter icon.
You can click on it to bring you to the file, or you can use <shortcut>Ctrl + B</shortcut> when your cursor is on the filename.
This includes commands like `\documentclass`, `\usepackage` and `\includegraphics` and many more.

![go-to-file-inclusion](go-to-file-inclusion.gif)

File reference resolving also uses `kpsewhich`, so you can for example include a LaTeX file located in `~/texmf/tex/latex/` and TeXiFy recognises it.

### Import package

_Since b0.6.9_

TeXiFy supports the `import` package with which you can include other LaTeX files.
Its main feature is the ability to set import path prefixes when you included a file.
For example, if you have `main.tex` which contains `\subimport{chapters/}{chapter-one.tex}`, and two files `chapters/chapter-one.tex` and `chapters/included.tex`, then in `chapter-one.tex` you can do `\input{included.tex}` and it will resolve correctly.
Without the import package, you would need to write `\input{chapters/included.tex}` for the include to work.

The package also has the `\import` command for absolute instead of relative paths.
For more information, see [http://mirrors.ctan.org/macros/latex/contrib/import/import.pdf](http://mirrors.ctan.org/macros/latex/contrib/import/import.pdf)

### Referencing files in bibtex

When you have a `file` or `bibsource` field, the content can include a path (absolute or relative) to a local pdf file from which the bibtex entry was created.
Then when you press <shortcut>Ctrl + B</shortcut> when your cursor is on the file path, the file will be opened.
Autocompletion is also available (for any file type).

An example:

```bibtex
@book{knuth1990,
    author = {Knuth, Donald E.},
    title = {The {\TeX} book },
    year = {1990},
    isbn = {0-201-13447-0},
    publisher = {Addison\,\textendash\,Wesley},
    bibsource = {../literature/knuth1990.pdf},
}
```

On Linux, the Mendeley format is also supported, which is of the form
`file = {:home/user/.local/share/data/Mendeley Ltd./Mendeley Desktop/Downloaded/filename.pdf:pdf;:home/user/.local/share/data/Mendeley Ltd./Mendeley Desktop/Downloaded/filename2.pdf:pdf}`
