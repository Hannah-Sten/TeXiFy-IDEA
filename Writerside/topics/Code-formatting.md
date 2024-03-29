# Code formatting

Press <shortcut>Ctrl+Alt+L</shortcut> to reformat.

## Fill paragraph

<ui-path>Edit | Fill Paragraph</ui-path>

Fill the paragraph that is currently under the cursor such that each line is filled until the right margin, but does not exceed it.

## Disabling the formatter
To disable formatting on a portion of your `tex` file, wrap the code with

<!-- ```latex -->
```
% @formatter:off
...
% @formatter:on
```

and enable these comments in the settings by going to <ui-path>Settings | Editor | Code Style | Formatter Control</ui-path> and checking **Enable formatter markers in comments**.
This can be particularly useful if you have to adhere to formatting conventions different from TeXiFy standards.

## Indents in (math) environments

By default all environments are indented, and manual indents will be removed by the formatter.

![indent-environment](indent-environment.gif)

This can be disabled in the code style settings, where indenting the document environment can have a different setting than other environments.


## Indents in groups

_Since b0.6.8_

When writing inside a group (`{...}` or `[...]`) the formatter will indent all content that is on a new line.

![indent-group](indent-group.gif)

## Blank lines before sectioning commands

_Since b0.6.8_

Edit this behaviour in the [code style settings](Code-Style.md#specify-number-of-blank-lines-before-sectioning-commands).

![blank-lines-section](blank-lines-section.gif)

## Hard wrap

_Since b0.6.8_

Enable or disable this setting in the [code style settings](Code-Style.md#option-to-hard-wrap-latex-and-bibtex-files).

![hard-wrap](hard-wrap.gif)

## Table formatting

_Since b0.6.10_

Inside a table environment, like `tabular`, the ``&``s and ``\\``s are aligned, so it is clear which cells belong to the same column.

![table-formatter-simple](table-formatter-simple.gif)

When formatting the contents of a table, it ignores all table lines that

* are split over multiple lines, or
* contain less `&` than required (for example when using the `\multicolumn` command).

If you have 'Wrap on typing' set to 'yes', then long lines may be split.

![table-formatter-multiline](table-formatter-multiline.gif)

## Algorithm pseudocode

When writing pseudocode using the `algorithmicx` environment and the `algpseudocode` set of commands, they will be formatted properly.

<compare>

<!-- ```latex -->
```
\begin{algorithm}
\begin{algorithmic}
\State begin
\If {$i\geq maxval$}
\State $i\gets 0$
\Else
\If {$i+k\leq maxval$}
\State $i\gets i+k$
\EndIf
\EndIf
\end{algorithmic}
\caption{Insertion sort}
\label{alg:insertion-sort}
\end{algorithm}
```

<!-- ```latex -->
```
\begin{algorithm}
    \begin{algorithmic}
        \State begin
        \If {$i\geq maxval$}
            \State $i\gets 0$
        \Else
            \If {$i+k\leq maxval$}
                \State $i\gets i+k$
            \EndIf
        \EndIf
    \end{algorithmic}
    \caption{Insertion sort}
    \label{alg:insertion-sort}
\end{algorithm}
```

</compare>

The following commands are supported.

* `\If{<condition>} <text> (\ElsIf{<condition>} <text>)* (\Else <text>)? \EndIf`
* `\For{<condition>} <text> \EndFor`
* `\ForAll{<condition>} <text> \EndFor`
* `\While{<condition>} <text> \EndWhile`
* `\Repeat <text> \Until{<condition>}`
* `\Loop <text> \EndLoop`
* `\Function{<name>}{<params>} <body> \EndFunction`
* `\Procedure{<name>}{<params>} <body> \EndProcedure`

Commands defined using `\algblock` or `\algloop` are currently not recognized by the formatter.

Formatting for algorithms written with `algorithm2e` instead of `algorithmicx` is also supported.

## External formatters

In some cases, you might have external requirements on your formatting which are incompatible with the default TeXiFy formatter.
For these cases, there are some built-in actions that run external formatters.
Most probably you want to bind a shortcut to this action in <ui-path>File | Settings | Keymap</ui-path>.

### Latexindent
_Since b0.7.7_

> Installation: [https://github.com/cmhughes/latexindent.pl#getting-started](https://github.com/cmhughes/latexindent.pl#getting-started)

If your caret is in a LaTeX file, you can use <ui-path>Code | Reformat with Latexindent</ui-path>.
This will run latexindent.pl on the current file, and it will be updated on disk.

### bibtex-tidy
_Since b0.7.11_

> Installation: `npm install -g bibtex-tidy`

At the moment of writing, version 1.7.2 is not yet published to npm, but this version adds a feature which allows TeXiFy to get the output from stdout, which allows for better user feedback because IntelliJ will not have to ask you whether you want to load file changes from disk.
You can install the new version locally by cloning the repo, optionally change the version number, run the npm `build` task and then run `npm link`.

See [https://github.com/FlamingTempura/bibtex-tidy](https://github.com/FlamingTempura/bibtex-tidy) for more information.
