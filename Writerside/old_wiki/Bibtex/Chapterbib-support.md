_Since b0.6.9_

chapterbib is a package which allows you to have a separate bibliography per chapter.
This means you have in your main file `\include` commands to include chapters, and each chapter file includes its own bibliography (this can be different per chapter).
TeXiFy will recognise this and automatically create the necessary BibTeX run configurations for each chapter, and run them.
For more information, see [https://ctan.org/pkg/chapterbib?lang=en](https://ctan.org/pkg/chapterbib?lang=en)

Example:

`main.tex`
```latex
\documentclass{article}
\usepackage[authoryear,sectionbib]{natbib}
\usepackage{chapterbib}

\begin{document}
    \include{introduction}
    \include{discrete_shape}
\end{document}
```

`introduction.tex`
```latex
Introduction cites~\cite{knuth90}.

\bibliographystyle{plainnat}
\bibliography{references}
```

`introduction.tex`
```latex
Discrete shape cites~\cite{goossens93}.

\bibliographystyle{plainnat}
\bibliography{references}
```

`references.bib`
```bibtex
@Book{knuth1990,
    author    = {Knuth, Donald E.},
    title     = {The {\TeX}book },
    year      = {1990},
    isbn      = {0-201-13447-0},
    publisher = {Addison\,\textendash\,Wesley},
}

@Book{goossens1993,
    author    = "Michel Goossens and Frank Mittelbach and Alexander Samarin",
    title     = "The LaTeX Companion",
    year      = "1993",
    publisher = "Addison-Wesley",
    address   = "Reading, Massachusetts"
}
```
