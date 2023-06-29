By default, TeXiFy will automatically insert package dependencies when you use autocomplete on a command of which the dependency is known.

An example is the `align` environment, which is provided by the `amsmath` package.
If you use the autocomplete to type `\begin{align} ... \end{align}`,you will see in the autocomplete window that `align` has the `amsmath` dependency.
If you select it, then `\usepackage{amsmath}` will be inserted automatically, as you can see below.

![package-import](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Writing/figures/package-import.gif)

Note that currently only a few common commands and packages are supported.

## Subfiles
_Since b0.6.8_

TeXiFy support using the `subfiles` package.
This means that package imports will be placed in the main file when you are writing in a subfile, and imports will be detected correctly.
An example of using the subfile package would be:

```latex
\documentclass{article}
\usepackage{amsmath}
\usepackage{subfiles}
\begin{document}
    \section{One}\label{sec:one}
    \subfile{section1}
\end{document}
```

`section1.tex`:

```latex
\documentclass[main.tex]{subfiles}

\begin{document}

    \begin{align}
        \xi
    \end{align}

\end{document}


```
