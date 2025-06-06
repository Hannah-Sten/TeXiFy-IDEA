# Editing a LaTeX file

To create a new LaTeX file, either use <ui-path>File | New | LaTeX File</ui-path> or right-click in the Project tool window and select <ui-path>New | LaTeX File</ui-path>.

![New file](newfile.png)

## Manage the appearance for long lines

You can enable soft wraps by going to <ui-path>Settings | General | Soft Wraps</ui-path>, enable Soft-wrap files and put `**.tex` in the text field, or just `**` to soft-wrap all files.

![Soft wraps](soft-wraps.png)

In <ui-path>Settings | Editor | Code Style | LaTeX</ui-path> you can enable 'wrap on typing' and much more, see [Code formatting settings](Code-formatting.md).

## Line commenting

By default, the shortcut <shortcut>Ctrl + /</shortcut> will add a comment (`%`) character to the beginning of the line.
When multiple lines are selected, these will all be commented.

Note that the block comment shortcut <shortcut>Ctrl + Shift + /</shortcut> should not be used, as LaTeX does not have any block comments by itself.

Also see the option to set [line comment at first column](Code-Style.md#line-comment-at-first-column).

## Multi-cursors

IntelliJ supports handling multiple carets at once, see [https://www.jetbrains.com/help/idea/multicursor.html](https://www.jetbrains.com/help/idea/multicursor.html).


## Styling text
<ui-path>Edit | LaTeX | Font Style</ui-path>

Insert font style commands like `\textbf` for bold face. If any text is selected, it will be used as argument to the command.

![font style](font-style.png)

## Inserting \section-like commands

<ui-path>Edit | LaTeX | Sectioning</ui-path>

Insert sectioning commands like `\part` or `\subsection`. If any text is selected, it will be used as argument to the command.

<ui-path>Edit | LaTeX | Toggle Star</ui-path>

Add or remove the star of a command, e.g. switch between `\section{...}` and `\section*{...}`.


## Surrounding selection with quotes or dollars
_Since b0.6.9_

With some text selected, press <shortcut>Ctrl + Alt + T</shortcut> (surround with) to surround text with quotes, dollar signs (inline math) or braces.
When surrounding with quotes, quotes will be inserted according to the [Smart quotes settings](TeXiFy-settings.md#smart-quote-substitution).
Use <shortcut>Ctrl + Alt + J</shortcut> (surround with live template) to surround with a live template, i.e., surround with dollars or braces.
To surround a selection with dollars, it is also possible to simply press `$`.

![surround](surround.png)

## Automatic package importing

By default, TeXiFy will automatically insert package dependencies when you use autocomplete on a command of which the dependency is known.

An example is the `align` environment, which is provided by the `amsmath` package.
If you use the autocomplete to type `\begin{align} ... \end{align}`, you will see in the autocomplete window that `align` has the `amsmath` dependency.
If you select it, then `\usepackage{amsmath}` will be inserted automatically, as you can see below.

![package-import](package-import.gif)

Note that not all commands' packages might be known to TeXiFy, see [SDK settings](Project-configuration.md#sdks) for ways to help TeXiFy in this.

## Renaming labels and environments {id="refactoring"}

_Since b0.6.9_

Currently, refactoring (renaming) elements is supported for files, references and citations.

To rename a label, place your cursor on a label definition or reference, e.g. `\ref{some-<cursor>label}` and press <shortcut>Shift+F6</shortcut>.

![rename](rename-navigate.gif)

To find out what elements need to be renamed as well (definition and other usages), the functionality from [Find usages](Code-navigation.md#find-usages) is used.

> You need to select 'Search for references' if you get a popup to rename an element, in order to let IntelliJ rename all the references to for example a file.
> 

You can also use the rename functionality to change an environment name in `\begin` and `\end` at the same time, i.e. replace

<!-- ```latex -->
```
\begin{center}
\end{center}
```

with

<!-- ```latex -->
```
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

## Quick documentation

If you want to have quick links to package documentation pdfs, make sure you have installed `texdoc`, for example on TeX Live with `tlmgr install texdoc`.
Then place your cursor on a LaTeX command and press <shortcut>Ctrl + Q</shortcut>.
If the command has a package dependency which is known to TeXiFy, you will get a popup which includes links to the package documentation that is installed locally on your machine.
LaTeX package documentation is written in LaTeX (surprise) so when you click on a link it will open a pdf.

When the command is a `\usepackage` or `\documentclass` then the documentation of the included package or class will be shown.
When your cursor is on an environment name, documentation for that environment will be shown.

Note that you can also use the shortcut <shortcut>Ctrl + Q</shortcut> during autocompletion of commands and environments, and navigate through the completion list using the arrow keys.

![texdoc](texdoc.png)

![env-docs](env-docs.png)

### Source of documentation
When you have set up a [LaTeX SDK](Project-configuration.md#sdks), in most cases you will have all commands and environments from all installed LaTeX packages in the autocompletion (see [Autocomplete](Code-completion.md#autocomplete-installed-commands)).
In a lot of cases, this includes some documentation for each command and environment.
However, this relies on package authors respecting the LaTeX conventions (using the doc package) whether the documentation is actually useful.
If you find something incorrect, please let us know and then we can determine whether something needs to be improved in the LaTeX package or in TeXiFy (example bug report: [https://gitlab.com/axelsommerfeldt/caption/-/issues/114](https://gitlab.com/axelsommerfeldt/caption/-/issues/114)).


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

> TeXiFy specific shortcuts can be found in <ui-path>File | Settings | Keymap</ui-path> under <control>plugins/TeXiFy IDEA</control>

## Inlining files and command definitions

Nearly every JetBrains IDE offers a refactoring tool called [Inline](https://www.jetbrains.com/help/idea/inline.html) which allows you to replace every reference of something with its definition. TeXiFy implements this in the following way:

<compare>

<!-- ```latex -->
```
\documentclass[11pt]{article}
\begin{document}

   \section{Demo}
   \input{demo}

\end{document}
```

<!-- ```latex -->
```
\documentclass[11pt]{article}
\begin{document}

   \section{Demo}
   Hello World!

\end{document}
```
</compare>

To perform this, you can right click an input command -> refactor -> inline and select what kind on inlining you are looking for.

![inline command](inline-command.gif)

## Swapping command arguments

Using <ui-path>Code | Move Element Left/Right</ui-path> or by default <shortcut>Ctrl + Alt + Shift + Left/Right</shortcut> you can move/swap required arguments of LaTeX commands.

![move-argument](move-argument.gif)

## Magic comments {id="magic-comments"}

_Since b0.6.10_

### Compilers
See [Using magic comments to specify the compiler for new run configurations](Run-configuration-settings.md#using-magic-comments-to-specify-the-compiler-for-new-run-configurations).

### Root file magic comment

If TeXiFy does not guess your root file(s) correctly, you can help TeXiFy by using the `root` magic comment to point TeXiFy to a root file.
For example, use `%! root = main.tex` in a file that is included by `main.tex`, when TeXiFy cannot figure out that `main.tex` is a root file of this file.

### Language injection

See [Language injection](Editor.md#language-injections).

### Custom preamble for math and tikz preview

See [Preview](Tool-Windows.md#equation-preview).

### Switching parser off and on

If you want to temporarily switch off the parser for a part of your LaTeX, for example because there is a parse error which is causing other problems in your files, you can use the magic comments `%! parser = off` and `%! parser = on` to avoid parsing the text between these two comments.
The syntax `% !TeX parser = off` is also supported.

<!-- ```latex -->
```
%! parser=off
    \catcode`#=14
    # Please don't do this
    # $PHP COMMENT
%! parser=on
```

### Custom folding regions

You can use either `%! region My description` and `%! endregion` or NetBeans-style `%! <editor-fold desc="My Description">` and `%! <editor-fold>` magic comments to specify custom folding regions.
They cannot interleave with structural elements like environments or math, but as long as they are balanced they will override the default section folding regions.
For more information, see [https://blog.jetbrains.com/idea/2012/03/custom-code-folding-regions-in-intellij-idea-111/](https://blog.jetbrains.com/idea/2012/03/custom-code-folding-regions-in-intellij-idea-111/) and [https://www.jetbrains.com/help/idea/code-folding-settings.html](https://www.jetbrains.com/help/idea/code-folding-settings.html)

### Fake sections

Use `%! fake section` to introduce a fake section which can be folded like any other section.
Here, `section` can be one of `part`, `chapter`, `section`, `subsection`, `subsubsection`, `paragraph`, or `subparagraph`.
Fake sections can also have a title, so `%! fake subsection Introduction part 1` is valid.

Note: if you feel you need to fold code because the file is too big or you lose overview, you probably should split it up into smaller files.
See [https://blog.codinghorror.com/the-problem-with-code-folding/](https://blog.codinghorror.com/the-problem-with-code-folding/)

## Support for user-defined commands

_Since b0.7_

TeXiFy supports custom definitions (aliases) of `label`-like, `\ref`-like, `\cite`-like and `input`-like commands.
For example, if you write

<!-- ```latex -->
```
\newcommand{\mylabel}[1]{\label{#1}}

\section{One}\mylabel{sec:one}
\section{Two}\label{sec:two}

~\ref{se<caret>} % autocompletion shows both sec:one and sec:two
```

For definitions like `\newcommand{\mycite}[1]{\citeauthor{#1}\cite{#1}}`, this means that you will also get autocompletion of citation labels in `\mycite` commands.

In the case of definitions including a `\label` or any command that has a file parameter, we check the parameter positions as well.
For example,

<!-- ```latex -->
```
\newcommand{\mysectionlabel}[2]{\section{#1}\label{#2}}

\mysectionlabel{One}{sec:one}
\section{Two}\label{sec:two}

~\ref{<caret>} % autocompletion shows sec:one but not One
```

## Autosave and local history

IntelliJ automatically saves files at certain moments, and saves history locally.
For more information, see [Save and revert changes | IntelliJ IDEA Documentation](https://www.jetbrains.com/help/idea/saving-and-reverting-changes.html).


## Subfiles
_Since b0.6.8_

TeXiFy support using the `subfiles` package.
This means that package imports will be placed in the main file when you are writing in a subfile, and imports will be detected correctly.
An example of using the subfile package would be:

<!-- ```latex -->
```
\documentclass{article}
\usepackage{amsmath}
\usepackage{subfiles}
\begin{document}
    \section{One}\label{sec:one}
    \subfile{section1}
\end{document}
```

`section1.tex`:

<!-- ```latex -->
```
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

<!-- ```latex -->
```
\documentclass{article}
\usepackage{graphicx}
\graphicspath{{/path/to/figures/}}
\begin{document}
    \begin{figure}
        \includegraphics{figure.jpg}
    \end{figure}
\end{document}
```


> You can also use relative paths, but no matter what path you use it _has_ to end in a forward slash `/`.
> You also need to use forward slashes on Windows.
> 
{style="note"}

You can include multiple search paths by continuing the list, like `\includegraphics{{/path1/}{../path2/}}`.

For more information, see the documentation linked at [https://ctan.org/pkg/graphicx](https://ctan.org/pkg/graphicx)


## Unicode support

IntelliJ supports Unicode, see for example [https://blog.jetbrains.com/idea/2013/03/use-the-utf-8-luke-file-encodings-in-intellij-idea/](https://blog.jetbrains.com/idea/2013/03/use-the-utf-8-luke-file-encodings-in-intellij-idea/)

Note that if the LaTeX log output contains characters in an incorrect encoding on Windows, you can fix this by going to <ui-path>Help | Edit Custom VM Options</ui-path> and add `-Dfile.encoding=UTF-8`, then restart your IDE.

Also see the [Unicode inspection](Probable-bugs.md#unsupported-unicode-character).