## Switching compilers

You can switch to a different compiler by clicking on the dropdown list next to the run button, select Edit Configurations and select a different compiler in the Compiler dropdown menu.
Make sure you have it installed correctly.

![change-compiler](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Running/figures/change-compiler.gif)

### Using magic comments to specify the compiler for new run configurations
To set the compiler of a document, put the magic comment

```
%! Compiler = [compiler executable] [compiler arguments]
```

at the top of the root file.
The syntax `%! Program =` is also supported.

To set the BibTeX compiler, put the magic comment

```
%! BibTeX Compiler = [compiler executable] [compiler arguments]
```

at the top of the ***LaTeX root*** file.
An example for compiling with lualatex that needs `--shell-escape` and biber:

```
%! Compiler = lualatex --shell-escape
%! BibTeX Compiler = biber
```

***Note:*** These magic comments only take affect when creating a new run configuration (using kbd:[Ctrl] + kbd:[Shift] + kbd:[F10] or using the gutter icon).
They have no effect on already existing run configurations, which can be edited using Edit Configurations dialog.

## LaTeX compilers

### pdfLaTeX

pdfLaTeX is the default compiler for TeXiFy. It is the most simple compiler, but one which is very stable.

### LuaLaTeX

To use LuaLaTeX, install the `luatex` package.
When using custom fonts, use either LuaLaTeX or XeLaTeX.
LuaLaTeX has the advantage that you can use Lua (a programming language) in your LaTeX.

### Latexmk

See [https://mg.readthedocs.io/latexmk.html](https://mg.readthedocs.io/latexmk.html) for installation and more information.
With TeX Live, install with `tlmgr install latexmk`.
With Latexmk the project is compiled just as much times as needed and handles BibTeX/Biber.
It uses pdflatex by default, but you can use an other compiler as well.

When you use a `latexmkrc` file (see the man page of latexmk for information) then TeXiFy will only add `output-format`, `-auxil-directory` and `-output-directory` command line arguments, because otherwise your options in your latexmkrc file would be overwritten.
You can still of course add additional command line arguments in the run configuration, including the location of the latexmkrc file with the `-r` flag.
If you want to tell TeXiFy to use the output format of your latexmkrc, make sure the `DEFAULT` output format is selected.
TeXiFy will also read additions to the TEXINPUTS environment variable from your latexmkrc file, assuming you add it with the syntax like `ensure_path('TEXINPUTS', 'subdir1//');`.

#### Tip: compiling automatically when IntelliJ loses focus
When you add the flag `-pvc` it watches the files and recompiles automatically on saved changes (in IntelliJ a save is
triggered, when the window looses focus, or by kbd:[Ctrl +S]).

For an automatic start of your pdf viewer you have to create a file in your users home directory.
The path for the file is under Linux and Mac `$HOME/.latexmkrc` and under Windows `%USERPROFILE%\.latexmkrc`.
In this file you need to add the following line:

```
$pdf_previewer = '"/path to/your/favorite/viewer" %O %S';
```

The quotes are needed to handle whitespaces in the path properly.

### XeLaTeX

To use XeLaTeX, install the `xetex` package.
When using custom fonts, use either LuaLaTeX or XeLaTeX.

### Texliveonfly

To use texliveonfly, install the `texliveonfly` package.
The purpose of texliveonfly is to install TeX Live packages automatically during compilation of a document, like the on the fly installation of MiKTeX but then much slower.
So it is only relevant if you have TeX Live, and if you do not have a full install with all the packages already installed.
You can use texliveonfly with any other LaTeX compiler.

For more info have a look at [https://tex.stackexchange.com/a/463842/98850](https://tex.stackexchange.com/a/463842/98850)

### Tectonic
_Since b0.6.6_

See [https://tectonic-typesetting.github.io/en-US/](https://tectonic-typesetting.github.io/en-US/) for installation and more info.
Tectonic has the advantage that it downloads packages automatically, compiles just as much times as needed and handles BibTeX, but it often only works for not too complicated LaTeX documents.

It also has automatic compilation using `tectonic -X watch`.

The documentation can be found at [https://tectonic-typesetting.github.io/book/latest/](https://tectonic-typesetting.github.io/book/latest/)

## BibTeX compilers

For more information about the bibtex compilers, see the [Bibtex](bibtex) page.