_Since b0.6.7_

When you create a run configuration from context (for example using the button next to `\begin{document}` or the shortcut kbd:[Ctrl + Shift + F10]) then TeXiFy will look whether you need to run makeindex, xindy, makeglossaries, etc., and when needed create the necessary run configurations automatically.
Makeindex is only run when you actually include an index package like `imakeidx` or a glossary package like `makeglossaries`.

Makeindex will be run as a separate configuration, so you will see the output in a window next to your normal LaTeX output window.
When you use the `auxil/` or `out/` directories, makeindex will be run there and the `.ind` file will be copied next to your main file so that the index package can find it.
It will be cleaned up as well to avoid cluttering your source directory, unless you copied it there manually (indicating you want to commit it to git, for example).

Note that when you use `imakeidx` but no `auxil/` or `out/` then imakeidx will handle makeindex itself. 

If you use xindy but no `auxil/` or `out/` then in order to allow imakeidx to run xindy, you need to provide the `-shell-escape` custom compiler argument in the run configuration.

Example with an index using makeindex:

```latex
\documentclass{article}
\usepackage{imakeidx}
\makeindex
\begin{document}
    Some random\index{random} text\index{text} which should be indexed\index{index}.
    \printindex
\end{document}
```

TeXiFy also supports using xindy (instead of makeindex) and a custom index name, for example:

```latex
\documentclass{article}
\usepackage[xindy]{imakeidx}
\makeindex[name=myindex]
\begin{document}
    Some random\index[myindex]{random} text\index[myindex]{text} which should be indexed\index[myindex]{index}.

    \printindex[myindex]
\end{document}
```

See the imakeidx documentation at [https://ctan.org/pkg/imakeidx](https://ctan.org/pkg/imakeidx) for more details.
Note that in order to use xindy to need to install Perl.

## Input index file filename.idx not found.
If you use an auxiliary directory (auxil/ or out/) then you may get an error message from imakeidx saying
`Input index file filename.idx not found.`.
You can ignore it, because TeXiFy handles makeindex, but imakeidx doesn’t know that and it will try to handle makeindex itself.
You can tell imakeidx to not worry about it with the `noautomatic` option, so write `\makeindex[noautomatic]`.

## Glossary examples

_Since b0.7.1_

The `glossaries` package [Beginners' guide](http://mirrors.ctan.org/macros/latex/contrib/glossaries/glossariesbegin.pdf) contains four options of building a glossary.
For completeness we repeat these examples below, so you can easily copy paste and run them.

Option 1, using LaTeX.

```latex
\documentclass{article}

\usepackage{glossaries}

\makenoidxglossaries % use TeX to sort
\newglossaryentry{sample}{name={sample},description={an example}}

\begin{document}
    A \gls{sample}.
    \printnoidxglossaries % iterate over all indexed entries
\end{document}
```

Option 2, using makeindex.

```latex
\documentclass{article}

\usepackage{glossaries}

\makeglossaries % create makeindex files
\newglossaryentry{sample}{name={sample},description={an example}}

\begin{document}
    A \gls{sample}.
    \printglossaries % input files created by makeindex
\end{document}
```

Option 3, using xindy (so Perl is required).
```latex
\documentclass{article}

\usepackage[xindy]{glossaries}

\makeglossaries % create xindy files
\newglossaryentry{sample}{name={sample},description={an example}}

\begin{document}
    A \gls{sample}.
    \printglossaries % input files created by xindy
\end{document}
```

Option 4, using bib2gls (requires at least Java 8).
```latex
\documentclass{article}

\usepackage[record]{glossaries-extra} % record -> bib2gls

\GlsXtrLoadResources % input file created by bib2gls
[% instructions to bib2gls:
src={entries}, % terms defined in entries.bib
sort={en-GB}% sort according to this locale
]

\newglossaryentry{sample}{name={sample},description={an example}}

\begin{document}
    A \gls{sample}.
    \printunsrtglossaries % iterate over all defined entries
\end{document}
```

Bonus example: you can even use both an index and a glossary.

```latex
\documentclass{article}

\usepackage{imakeidx}
\usepackage[xindy]{glossaries}

\makeindex
\makeglossaries
\newglossaryentry{sample}{name={sample},description={an example}}

\begin{document}
    A \gls{sample}.
    Some random\index{random} text\index{text} which should be indexed\index{index}.

    \printglossaries
    \printindex
\end{document}
```