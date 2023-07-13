# Editing a LaTeX file

## Unicode support

IntelliJ supports Unicode, see for example [https://blog.jetbrains.com/idea/2013/03/use-the-utf-8-luke-file-encodings-in-intellij-idea/](https://blog.jetbrains.com/idea/2013/03/use-the-utf-8-luke-file-encodings-in-intellij-idea/)

Note that if the LaTeX log output contains characters in an incorrect encoding on Windows, you can fix this by going to <ui-path>Help | Edit Custom VM Options</ui-path> and add `-Dfile.encoding=UTF-8`, then restart your IDE.

Also see the [Unicode inspection](Probable-bugs#Unsupported-Unicode-character).

## Line commenting

By default, the shortcut <shortcut>Ctrl + /</shortcut> will add a comment (`%`) character to the beginning of the line.
When multiple lines are selected, these will all be commented.

Note that the block comment shortcut <shortcut>Ctrl + Shift + /</shortcut> should not be used, as LaTeX does not have any block comments by itself.

Also see the [Option to start a comment at the same indentation as normal text](Code-style-settings#indent-comment).

## Shortcuts for styling text

See [Menu Entries](Menu-entries.md)

## Automatic package importing

By default, TeXiFy will automatically insert package dependencies when you use autocomplete on a command of which the dependency is known.

An example is the `align` environment, which is provided by the `amsmath` package.
If you use the autocomplete to type `\begin{align} ... \end{align}`,you will see in the autocomplete window that `align` has the `amsmath` dependency.
If you select it, then `\usepackage{amsmath}` will be inserted automatically, as you can see below.

![package-import](package-import.gif)

Note that currently only a few common commands and packages are supported.

## Renaming labels and environments

_Since b0.6.9_

Currently, refactoring (renaming) elements is supported for files, labels and environments.

To rename a label, place your cursor on a label definition or reference, e.g. `\ref{some-<cursor>label}` and press <shortcut>Shift+F6</shortcut>.

To find out what elements need to be renamed as well (definition and other usages), the functionality from [Find usages](Find-usages) is used.

> You need to select 'Search for references' if you get a popup to rename an element, in order to let IntelliJ rename all the references to for example a file.
{style="note"}

Similarly, you can easily rename an environment, i.e. replace

```latex
\begin{center}
\end{center}
```

with

```latex
\begin{abstract}
\end{abstract}
```

by making sure your cursor is on the environment name inside either the `\begin` or `\end` command and using <shortcut>Shift + F6</shortcut>, then type the new name.

When you try to rename an element for which refactoring is not supported, the element will simply not change or in some cases a warning "Inserted identifier is not valid" will be shown.

## Switching between math environments
To switch between math environments, press <shortcut>Alt + Enter</shortcut> when your cursor is in a math environment.
Then you can choose 'Convert to other math environment' and you will get a popup to choose from.

![Environment switching](environment-switch.png)

When you switch from an `alignat` environment to a different environment, the environment parameter will be removed.

## Shortcuts

Note that all shortcuts are customizable, you can change them in <ui-path>File | Settings | Keymap</ui-path>.

### General IntelliJ shortcuts

See [https://www.jetbrains.com/help/idea/mastering-keyboard-shortcuts.html](https://www.jetbrains.com/help/idea/mastering-keyboard-shortcuts.html) for more information.

Some useful shortcuts are for example:

* Double <shortcut>Shift</shortcut>: Search for any IntelliJ command, like Reformat.
* <shortcut>Alt + Enter</shortcut>: When your cursor is in a place where an inspection ribbon is shown, view the quick fix, if there is one. Apply the fix with <shortcut>Enter</shortcut>.
* <shortcut>Ctrl + Alt + L</shortcut>: Reformat the file.
* <shortcut>Ctrl + D</shortcut>: Duplicate the line or selection.
* <shortcut>Alt + Shift + &#8593;</shortcut> or <shortcut>Alt + Shift + &#8595;</shortcut>: Move the line up or down.
* <shortcut>Ctrl + K</shortcut>: Commit and push changes with git.
* <shortcut>Ctrl + T</shortcut> Pull changes with git.
* <shortcut>Ctrl + Alt + &lt;-</shortcut> Go back to previous cursor location.

### TeXiFy-IDEA shortcuts

See the [Menu entries](Features#menu-entries), of which many have shortcuts.
In the menu in IntelliJ you can see the shortcuts.

## Surrounding selection with quotes or dollars
_Since b0.6.9_

With some text selected, press <shortcut>Ctrl + Alt + T</shortcut> (surround with) to surround text with quotes, dollar signs (inline math) or braces.
When surrounding with quotes, quotes will be inserted according to the [Smart quotes settings](Global-settings#smart-quotes).
Use <shortcut>Ctrl + Alt + J</shortcut> (surround with live template) to surround with a live template, i.e., surround with dollars or braces.
To surround a selection with dollars, it is also possible to simply press `$`.

## Multi-cursors

IntelliJ supports handling multiple carets at once, see [https://www.jetbrains.com/help/idea/multicursor.html](https://www.jetbrains.com/help/idea/multicursor.html).


## Inlining files and command definitions

Nearly every JetBrains IDE offers a refactoring tool called [Inline](https://www.jetbrains.com/help/idea/inline.html) which allows you to replace every reference of something with its definition. TeXiFy implements this in the following way:

#### Before
------------
Main.tex:
```latex
\documentclass[11pt]{article}
\begin{document}

   \section{Demo}
   \input{demo}

\end{document}
```

demo.tex:
```latex
Hello World!
```

#### After
--------------
Main.tex:
```latex
\documentclass[11pt]{article}
\begin{document}

   \section{Demo}
   Hello World!

\end{document}
```

To perform this, you can right click an input command -> refactor -> inline and select what kind on inlining you are looking for.

## Swapping command arguments

Using <ui-path>Code | Move Element Left/Right</ui-path> or by default <shortcut>Ctrl + Alt + Shift + Left/Right</shortcut> you can move/swap required arguments of LaTeX commands.

![move-argument](move-argument.gif)

## Magic comments {id="magic-comments"}

_Since b0.6.10_

### Compilers
See [Using magic comments to specify the compiler for new run configurations](Compilers#using-magic-comments-to-specify-the-compiler-for-new-run-configurations).

### Root file

If TeXiFy does not guess your root file(s) correctly, you can help TeXiFy by using the `root` magic comment to point TeXiFy to a root file.
For example, use `%! root = main.tex` in a file that is included by `main.tex`, when TeXiFy cannot figure out that `main.tex` is a root file of this file.

### Language injection

See [Language injection](Language-injection).

### Custom preamble for math and tikz preview

See [Preview](Preview).

### Switching parser off and on

If you want to temporarily switch off the parser for a part of your LaTeX, for example because there is a parse error which is causing other problems in your files, you can use the magic comments `%! parser = off` and `%! parser = on` to avoid parsing the text between these two comments.
The syntax `% !TeX parser = off` is also supported.

### Custom folding regions

You can use either `%! region My description` and `%! endregion` or NetBeans-style `%! <editor-fold desc="My Description">` and `%! <editor-fold>` magic comments to specify custom folding regions.
For more information, see [https://blog.jetbrains.com/idea/2012/03/custom-code-folding-regions-in-intellij-idea-111/](https://blog.jetbrains.com/idea/2012/03/custom-code-folding-regions-in-intellij-idea-111/) and [https://www.jetbrains.com/help/idea/code-folding-settings.html](https://www.jetbrains.com/help/idea/code-folding-settings.html)

### Fake sections

Use `%! fake section` to introduce a fake section which can be folded like any other section.
Here, `section` can be one of `part`, `chapter`, `section`, `subsection`, `subsubsection`, `paragraph`, or `subparagraph`.
Fake sections can also have a title, so `%! fake subsection Introduction part 1` is valid.

Note: if you feel you need to fold code because the file is too big or you lose overview, you probably should split it up into smaller files.
See [https://blog.codinghorror.com/the-problem-with-code-folding/](https://blog.codinghorror.com/the-problem-with-code-folding/)

## Support for user-defined commands

_Since b0.7_

TeXiFy supports custom definitions of `label`-like, `\ref`-like and `\cite`-like commands.
For example, if you write

```latex
\newcommand{\mylabel}[1]{\label{#1}}

\section{One}\mylabel{sec:one}
\section{Two}\label{sec:two}

~\ref{la<caret>} % autocompletion shows both sec:one and sec:two
```

For definitions like `\newcommand{\mycite}[1]{\citeauthor{#1}\cite{#1}}`, this means that you will also get autocompletion of citation labels in `\mycite` commands.

In the case of definitions including a `\label` command, we check the parameter positions as well.
For example,

```latex
\newcommand{\mysectionlabel}[2]{\section{#1}\label{#2}}

\mysectionlabel{One}{sec:one}
\section{Two}\label{sec:two}

~\ref{<caret>} % autocompletion shows sec:one but not One
```



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

## Graphicspath support

_Since b0.6.9_

TeXiFy supports the use of the `\graphicspath` command from the `graphicx` package.
You can use this to add extra directories in which graphicx will search for images.

For example, if you have images in a path `/path/to/figures` you could write

```latex
\documentclass{article}
\usepackage{graphicx}
\graphicspath{{/path/to/figures/}}
\begin{document}
    \begin{figure}
        \includegraphics{figure.jpg}
    \end{figure}
\end{document}
```

You can also use relative paths, but no matter what path you use it _has_ to end in a forward slash `/`.
You also need to use forward slashes on Windows.

You can include multiple search paths by continuing the list, like `\includegraphics{{/path1/}{../path2/}}`.

For more information, see the documentation linked at [https://ctan.org/pkg/graphicx](https://ctan.org/pkg/graphicx)
