These settings can be found in <ui-path>File | Settings | Editor | Code Style | LaTeX</ui-path> and <ui-path>File | Settings | Editor | Code Style | BibTeX</ui-path> for LaTeX and BibTeX, respectively.
These settings will change the behaviour of the [formatter](Formatter), triggered by pressing kbd:[Ctrl + Alt + L], or the behaviour of the editor.

## Common code style settings for both LaTeX and BibTeX

These settings are available for both LaTeX and BibTeX.

### Indent size

Change the number of spaces to indent with in the **Tabs and Indents** tab.

### Option to hard wrap LaTeX and BibTeX files

These options are in the **Wrapping** tab.

Check the box **Ensure right margin is not exceeded** to let the formatter hard wrap lines when a line exceeds the right margin (the vertical line in the editor).
Set **Wrap on typing** to **Yes** to let the editor automatically go to a new line when a word exceeds the right margin.

## LaTeX specific code style settings

These settings are only available for LaTeX.

### Option to start comment at the same indentation as normal text

This setting is in the **Code Generation** tab.

Press kbd:[Ctrl + /] to turn a line into a comment.
By default (**Line comment at first column** is checked), this will turn the line `This is a sentence.`

```
\begin{center}
    Normal text.
    This is a sentence.
\end{center}
```
into the comment

```
\begin{center}
    Normal text
%    This is a sentence.
\end{center}
```

Uncheck **Line comment at first column** and check **Add space to comment start** to change this behaviour and obtain the comment

```
\begin{center}
    Normal text.
    % This is a sentence.
\end{center}
```

Note that this only changes the behaviour when **generating** a comment with kbd:[Ctrl + /], and that this setting has no influence on the formatting of comments.

### Specify number of blank lines before sectioning commands

These settings are in the **Blank Lines** tab.

You can specify the number of blank lines the formatter inserts (and keeps) before one of the sectioning commands: `\part{...}`, `\chapter{...}`, `\section{...}`, `\subsection{...}`, `\subsubsection{...}`, `\paragraph{...}`, and `\subparagraph{...}`.

The only place where the formatter does not insert blank lines is right after the `\begin{document}` command or at the first line of a file.

### Indent text in sections

When enabled, text inside sections, subsections, etc. will be indented.
As an example,

```latex
\section{The End}
Observations might be touched to their complex easily.
\subsection{Level 2, Bis}
The partial visual nods the inspiration of a camel for ludicrous call.
        \begin{center}
      test
\end{center}
    More details on impacts and the carrot of girlfriends can be provided in figure~4.
  \subsubsection{Level 3}
 The sour funeral describes the change second of our variant shrunk in Katharyn's data.
 \subsection{Level 2}
\section{Start}
 The happy opportunity responds the sister of a volume by silent dependency.
```

will be reformatted as

```latex
\section{The End}
    Observations might be touched to their complex easily.

    \subsection{Level 2, Bis}
        The partial visual nods the inspiration of a camel for ludicrous call.
        \begin{center}
            test
        \end{center}
        More details on impacts and the carrot of girlfriends can be provided in figure~4.

        \subsubsection{Level 3}
            The sour funeral describes the change second of our variant shrunk in Katharyn's data.

    \subsection{Level 2}


\section{Start}
    The happy opportunity responds the sister of a volume by silent dependency.
```

### Indent document environment

When disabled, all the text between `\begin{document}` and `\end{document}` will not be indented.
