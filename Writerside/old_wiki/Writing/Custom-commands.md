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