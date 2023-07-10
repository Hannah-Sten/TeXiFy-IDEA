TeXiFy relies on a lexer and parser for most of its functionality.
The parser is relatively strict, and it will not accept any valid LaTeX.
This has the advantage that it is relatively easy to implement features which make use of this imposed structure, but you will always be able to create cases of valid LaTeX which break TeXiFy.
We intend to make the parser such that it will accept almost all LaTeX that we think is well structured and readable.

If you do encounter a parse error that you think is incorrect, please raise an issue.
As a workaround, if you want to keep syntax highlighting for that part you can use magic comments to disable the formatter (see [Code formatting](Code-formatting)) to avoid it incorrectly formatting your file:

```latex
% @formatter:off
...
% @formatter:on
```

If you are fine without the syntax highlighting for that part, you can disable the parser entirely (see [Magic comments](Magic-comments)).
This will ensure that TeXiFy completely ignores this part of the code, and other parts should remain working fine.

```latex
%! parser = off
...
%! parser = on
```

## Examples of known parser bugs

We have two `\begin` commands but only one `\end` command, so the parser will be confused. ([#2141](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/2141))
Since it isn’t clear which `\begin` should be matched with the `\end`, ideally it wouldn’t try to match them at all.
But the only reason this is valid LaTeX at all is the `\if`, and whether we need to match depends on many things, for example whether the `\end` is inside the `\if` or not.

```latex
\newenvironment{messageTable}[2]
{
    \begin{center}
        #2\\
        \ifx{#1}{2}
            \begin{tabular}{|c|c|}
            \hline
            \textbf{Bytes} & \textbf{Name} \\
        \else
            \begin{tabular}{|c|c|c|}
            \hline
            \textbf{Bytes} & \textbf{Name} & \textbf{Value} \\
        \fi

}
{
        \hline
        \end{tabular}
    \end{center}
}

```