Next to commands that include other files, there is a gutter icon.
You can click on it to bring you to the file, or you can use <shortcut>Ctrl + B</shortcut> when your cursor is on the filename.
This includes commands like `\documentclass`, `\usepackage` and `\includegraphics` and many more.

![go-to-file-inclusion](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Navigation/figures/go-to-file-inclusion.gif)

File reference resolving also uses `kpsewhich`, so you can for example include a LaTeX file located in `~/texmf/tex/latex/` and TeXiFy recognises it.

## Import package

_Since b0.6.9_

TeXiFy supports the `import` package with which you can include other LaTeX files.
Its main feature is the ability to set import path prefixes when you included a file.
For example, if you have `main.tex` which contains `\subimport{chapters/}{chapter-one.tex}`, and two files `chapters/chapter-one.tex` and `chapters/included.tex`, then in `chapter-one.tex` you can do `\input{included.tex}` and it will resolve correctly.
Without the import package, you would need to write `\input{chapters/included.tex}` for the include to work.

The package also has the `\import` command for absolute instead of relative paths.
For more information, see [http://mirrors.ctan.org/macros/latex/contrib/import/import.pdf](http://mirrors.ctan.org/macros/latex/contrib/import/import.pdf)

## Referencing files in bibtex

When you have a `file` or `bibsource` field, the content can include a path (absolute or relative) to a local pdf file from which the bibtex entry was created.
Then when you press <shortcut>Ctrl + B</shortcut> when your cursor is on the file path, the file will be opened.
Autocompletion is also available (for any file type).

An example:

```bibtex
@book{knuth1990,
    author = {Knuth, Donald E.},
    title = {The {\TeX} book },
    year = {1990},
    isbn = {0-201-13447-0},
    publisher = {Addison\,\textendash\,Wesley},
    bibsource = {../literature/knuth1990.pdf},
}
```

On Linux, the Mendeley format is also supported, which is of the form
`file = {:home/user/.local/share/data/Mendeley Ltd./Mendeley Desktop/Downloaded/filename.pdf:pdf;:home/user/.local/share/data/Mendeley Ltd./Mendeley Desktop/Downloaded/filename2.pdf:pdf}`
