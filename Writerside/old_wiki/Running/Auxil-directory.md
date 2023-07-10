By default, TeXiFy will put output files (pdf) in an `out` directory, and auxiliary files (aux, log, etc.) in an `auxil` directory to keep your project clean.
However, there are some special cases.
Note that using latexmk is also a great way to keep your project clean as it will not keep the intermediate files at all, but it requires Perl to be installed.

## Bibtex and TeX Live
When using TeX Live and bibtex, using the `auxil` directory will not work for bibtex as it will not be able to find the source bib file.
TeXiFy detects when you are using bibtex to generate a bibtex run configuration.
When doing so, it will set the working directory of the bibtex run configuration to the `auxil` directory, and update the `BIBINPUTS` environment variable so that everything should work as you would expect.

## Makeindex
TeXiFy will generate a Makeindex run configuration when it detects you are using an index.
However, generated index files like `.ind` need to be next to the main file otherwise these index packages won’t work, so TeXiFy will, after running the index program in the auxil directory, temporarily copy these files to the right place and clean them up afterwards (similar, though perhaps less efficient, to what latexmk would do).
For bib2gls, which needs the bib file, we copy the auxil file and run bib2gls next to the main file instead.

## Minted
Minted needs to find its own generated `.pyg` file in the auxil directory, if you use it.
You can tell this to minted by using `\usepackage[outputdir=../auxil]{minted}`.