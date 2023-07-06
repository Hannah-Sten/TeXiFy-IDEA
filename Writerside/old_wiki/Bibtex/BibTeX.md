When you want to use references in your LaTeX, you should use BibTeX.

First, you have to decide which package and compiler you are going to use.
There are many different ways to do this, but we will compare two of the most used ways.

Before starting, make sure you have TeXiFy and LaTeX installed according to [https://github.com/Hannah-Sten/TeXiFy-IDEA#installation-instructions-installing-intellij-and-the-texify-idea-plugin](https://github.com/Hannah-Sten/TeXiFy-IDEA#installation-instructions-installing-intellij-and-the-texify-idea-plugin)

## Terminology

To avoid confusion, we first name some relevant parts.

* You write your references in a _bibtex file_ which has a `.bib` extension. This is essentially a database. You can use other software like Mendeley or JabRef to manage this database.
* You can use LaTeX packages to help you typesetting your bibliography in the way you want, an example is biblatex.
* You also have to compile your `.bib` file using a special _bibtex compiler_. The most common ones are the biber compiler and the (confusingly named) bibtex compiler.

For more information about these differences and more examples, see for example [https://tex.stackexchange.com/questions/25701/bibtex-vs-biber-and-biblatex-vs-natbib](https://tex.stackexchange.com/questions/25701/bibtex-vs-biber-and-biblatex-vs-natbib)

## Choosing your way of using bibtex

For both methods, your `.bib` file can be the same, but you include it in a different way.
TeXiFy will try to detect this, and automatically compile with the right compiler.

### Using the bibtex compiler and no extra package

#### Example

`main.tex`
```latex
\documentclass{article}

\begin{document}
    When you are not looking at it, this sentences stops citing~\cite{knuth1990}.

    \bibliography{references}
    \bibliographystyle{plain}
\end{document}
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
```

For a downloadable example, see [https://github.com/PHPirates/bibtex-mwe](https://github.com/PHPirates/bibtex-mwe)

### Using the biber compiler and the biblatex package

#### Example
An example of using the biblatex package:

`main.tex`
```latex
\documentclass{article}

\usepackage[giveninits=true]{biblatex}
\addbibresource{references.bib}

\begin{document}
    If I had finished writing the sentence citing~\cite{goossens1993} and~\cite{greenwade1993},
    \printbibliography
\end{document}
```

`references.bib`
```bibtex
@article{greenwade1993,
    author  = "George D. Greenwade",
    title   = "The {C}omprehensive {T}ex {A}rchive {N}etwork ({CTAN})",
    year    = "1993",
    journal = "TUGBoat",
    volume  = "14",
    number  = "3",
    pages   = "342--351"
}

@book{goossens1993,
    author    = "Michel Goossens and Frank Mittelbach and Alexander Samarin",
    title     = "The LaTeX Companion",
    year      = "1993",
    publisher = "Addison-Wesley",
    address   = "Reading, Massachusetts"
}
```

For a downloadable example, see [https://github.com/PHPirates/biber-biblatex-mwe](https://github.com/PHPirates/biber-biblatex-mwe)

## Compiling

Once you have chosen which compiler and packages you want, make sure you have a simple example file like above to test it with.
This makes it easier to detect and solve any errors you get.

* Place your cursor in your main LaTeX file (`main.tex` in the example) and hit kbd:[Ctrl + Shift + F10] to create a run configuration and run it.
* TeXiFy should have created a LaTeX run configuration which is linked to a BibTeX run configuration, so you should see output windows for both LaTeX and BibTeX, and the references appear correctly in the pdf.

## Troubleshooting

If the references do not appear correctly and no bibliography section is shown, read on to debug the problem.

* On Linux, make sure you have the 'Separate output files from source' checkbox disabled.
* Make sure that a BibTeX run configuration was created, after running you should see multiple tabs in the Run window at the bottom of your screen, like `main`, `main bibliography`, `main`, `main`. If not, you have multiple options.
  1. Switch to a different compiler which handles BibTeX automatically for you. You can for example use latexmk, see [Compilers#latexmk](Compilers#latexmk) on how to install it, then [switch to it](Compilers#Switching-compilers). An other compiler which can do this is [Tectonic](Compilers#Tectonic).
  2. Create a BibTeX run configuration manually:
     * Click the dropdown list with the name of your run configuration (probably the name of your main file)
     * Click Edit Configurations
     * Click the Plus icon to create a new one
     * Select BibTeX
     * Select the right compiler
     * Go to your LaTeX run configuration and click Bibliography: Disabled
     * Click on the edit icon and choose the BibTeX run configuration you just created
     * Now run your _main_ run configuration (not the BibTeX one) and you should see it first runs LaTeX, then BibTeX then LaTeX twice.
* If the exit code of any run is not 0, check the log for any error message.
