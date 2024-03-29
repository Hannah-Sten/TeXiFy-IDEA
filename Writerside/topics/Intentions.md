# Intentions

Intentions are similar to inspection quickfixes, but they are always available, not only when an inspection is triggered.

## Add label

Add a label to a sectioning command. Also see [Missing label inspection](Code-style-issues.md#missing-labels).

## Toggle inline/display math mode

Switch between inline and display math. A shortcut for an action also present in the [Convert to other math environment](Intentions.md#convert-to-other-math-environment) intention.

## Insert comments to disable the formatter

Insert `% @formatter:off` and `% @formatter:on` comments around the environment.
See [Code formatting](Code-formatting.md#disabling-the-formatter).
Currently only enabled on verbatim-like environments.

## Change to `\left..\right`

Add `\left` and `\right` to brackets, e.g. changing `$(\frac 1 2)$` into `\left(\frac 1 2 \right)`.

## Convert to other math environment

Switch between math environments, e.g. `align*` and inline math.

## Move section contents to separate file {id="move-section-to-file"}
Similar to [move selection contents to separate file](Intentions.md#selection-to-file), except that it is triggered on
a section heading. Any `\section{...}` command will trigger this. It turns

**main.tex**

<!-- ```latex -->
```
\documentclass{article}

\begin{document}
    \section{One}
    First section.
\end{document}
```

into the two files

**main.tex**

<!-- ```latex -->
```
\documentclass{article}

\begin{document}
    \section{One}
    \input{one}
\end{document}
```

**one.tex**

<!-- ```latex -->
```
First section.
```

## Move selection contents to separate file {id="selection-to-file"}
Move the selected text to a new file. When triggering this intention, there will be a popup asking for the name and
location of the new file. Consider the following file:

**main.tex**

<!-- ```latex -->
```
\documentclass{article}

\begin{document}
    This is a selected sentence.
\end{document}
```

Selecting the text "This is a selected sentence." will enable the intention. After triggering the inspection and naming
your new file `content.tex` we have the following two files

**main.tex**

<!-- ```latex -->
```
\documentclass{article}

\begin{document}
    \input{content}
\end{document}
```

**content.tex**

<!-- ```latex -->
```
This is a selected sentence.
```

## Split into multiple `\usepackage` commands

Split `\usepackage{amsmath,amsthm}` into

<!-- ```latex -->
```
\usepackage{amsmath}
\usepackage{amsthm}
```
