## Add label

Add a label to a sectioning command. Also see [Missing label inspection](Conventions#Missing-labels).

## Toggle inline/display math mode

Switch between inline and display math. A shortcut for an action also present in the [Convert to other math environment](Intentions#convert-to-other-math-environment) intention.

## Insert comments to disable the formatter

Insert `% @formatter:off` and `% @formatter:on` comments around the environment.
See [Code formatting](Code-formatting).
Currently only enabled on verbatim-like environments.

## Change to `\left..\right`

Add `\left` and `\right` to brackets, e.g. changing `$(\frac 1 2)$` into `\left(\frac 1 2 \right)`.

## Convert to other math environment

Switch between math environments, e.g. `align*` and inline math.

## [[move-section-to-file]]Move section contents to separate file
Similar to [move selection contents to separate file](Intentions#selection-to-file), except that it is triggered on
a section heading. Any `\section{...}` command will trigger this. It turns

**main.tex**

```latex
\documentclass{article}

\begin{document}
    \section{One}
    First section.
\end{document}
```

into the two files

**main.tex**

```latex
\documentclass{article}

\begin{document}
    \section{One}
    \input{one}
\end{document}
```

**one.tex**

```latex
First section.
```

## [[selection-to-file]]Move selection contents to separate file
Move the selected text to a new file. When triggering this intention, there will be a popup asking for the name and
location of the new file. Consider the following file:

**main.tex**

```latex
\documentclass{article}

\begin{document}
    This is a selected sentence.
\end{document}
```

Selecting the text "This is a selected sentence." will enable the intention. After triggering the inspection and naming
your new file `content.tex` we have the following two files

**main.tex**

```latex
\documentclass{article}

\begin{document}
    \input{content}
\end{document}
```

**content.tex**

```latex
This is a selected sentence.
```

## Split into multiple `\usepackage` commands

Split `\usepackage{amsmath,amsthm}` into

```latex
\usepackage{amsmath}
\usepackage{amsthm}
```
