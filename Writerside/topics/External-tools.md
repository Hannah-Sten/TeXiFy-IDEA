# External tools

## Running a custom external tool before a LaTeX compilation

To run any external tool every time before compiling a LaTeX file, see [Run configuration settings](Run-configuration-settings.md#other-tasks-to-run-before-the-run-configuration-including-other-run-configurations-or-external-tools).

## Makeindex

_Since b0.6.7_

When you create a run configuration from context (for example using the button next to `\begin{document}` or the shortcut <shortcut>Ctrl + Shift + F10</shortcut>) then TeXiFy will look whether you need to run makeindex, xindy, makeglossaries, etc., and when needed create the necessary run configurations automatically.
Makeindex is only run when you actually include an index package like `imakeidx` or a glossary package like `makeglossaries`.

Makeindex will be run as a separate configuration, so you will see the output in a window next to your normal LaTeX output window.
When you use the `auxil/` or `out/` directories, makeindex will be run there and the `.ind` file will be copied next to your main file so that the index package can find it.
It will be cleaned up as well to avoid cluttering your source directory, unless you copied it there manually (indicating you want to commit it to git, for example).

Note that when you use `imakeidx` but no `auxil/` or `out/` then imakeidx will handle makeindex itself.

If you use xindy but no `auxil/` or `out/` then in order to allow imakeidx to run xindy, you need to provide the `-shell-escape` custom compiler argument in the run configuration.

Example with an index using makeindex:

<!-- ```latex -->
```
\documentclass{article}
\usepackage{imakeidx}
\makeindex
\begin{document}
    Some random\index{random} text\index{text} which should be indexed\index{index}.
    \printindex
\end{document}
```

TeXiFy also supports using xindy (instead of makeindex) and a custom index name, for example:

<!-- ```latex -->
```
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

### nomencl

Also the nomencl package is supported in the same way, as it uses makeindex.
Example from the nomencl documentation:

<!-- ```latex -->
```
\documentclass{article}
\usepackage[nocfg]{nomencl}
\makenomenclature

\begin{document}
    \section*{Main equations}
    \begin{equation}
        a=\frac{N}{A}
    \end{equation}%
    \nomenclature{$a$}{The number of angels per unit area\nomrefeq}%
    \nomenclature{$N$}{The number of angels per needle point\nomrefpage}%
    \nomenclature{$A$}{The area of the needle point}%
    The equation $\sigma = m a$%
    \nomenclature{$\sigma$}{The total mass of angels per unit area\nomrefeqpage}%
    \nomenclature{$m$}{The mass of one angel}
    follows easily.
    \eqdeclaration{32}
    \printnomenclature
\end{document}
```

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

<!-- ```latex -->
```
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

<!-- ```latex -->
```
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
<!-- ```latex -->
```
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
<!-- ```latex -->
```
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

<!-- ```latex -->
```
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


## Built-in

The run configuration External LaTeX Tool can be used to run other auxiliary tools which need to run inbetween LaTeX runs.
Please raise an issue on GitHub if your favourite tool is missing here.
Currently, the following tools are supported.

## Pythontex
_Since b0.7.2_

Pythontex is a LaTeX package which can, among other things, run Python code which you included in your LaTeX file and nicely format the code and the output as well.

<!-- ```latex -->
```
\documentclass[11pt]{article}
\usepackage{pythontex}
\begin{document}

    %! language = python
    \begin{pyconsole}
def primes_sieve2(limit):
    a = [True] * limit                          # Initialize the primality list
    a[0] = a[1] = False
    for (i, isprime) in enumerate(a):
        if isprime:
            yield i
            for n in range(i*i, limit, i):     # Mark factors non-prime
                a[n] = False
    return [i for i in a if a[i]==True]

list(primes_sieve2(60))
    \end{pyconsole}

\end{document}
```

## Sage
_Since v2.0.0_

To use Sage, you need to have Sagemath installed.
On some operation systems, you might need to install sagetex separately (for Arch there is the `sagetex` package, for example).
Then, make sure the package is found, for example by adding `TEXINPUTS=/usr/share/texmf//:` to your run configuration, assuming the package is located there.
Now you can add `\usepackage{sagetex}` and TeXiFy should be able to find it.
Then you can use sagetex commands, for example `\sage{EllipticCurve(GF(409),[1,2]).order()}`, and run the file.

If the run configuration is not created correctly the first time, you need to make sure to first run pdflatex, then a general command line step to run `sage main.sagetex.sage`, then pdflatex again.

## Jinja2 support

Jinja2 is a templating language, so you can write special commands in LaTeX which will be replaced by actual valid LaTeX by a certain Python script.
The difference with using e.g. lualatex or simply outputting LaTeX from Python is that you can still do the formatting in LaTeX, and from the Python script you only give the raw data to be typeset to the LaTeX file (taking an abstract point of view).

PyCharm supports Jinja2 by default, you can enable it for LaTeX by going to <ui-path>File | Settings | Languages & Frameworks</ui-path> and add LaTeX source file as language.
Also see [https://www.jetbrains.com/help/pycharm/template-languages.html](https://www.jetbrains.com/help/pycharm/template-languages.html)

Then you can write for example a LaTeX file containing

<!-- ```latex -->
```
\documentclass{article}

\begin{document}

    \section{ {{- text -}} }

    \begin{tabular}{lllll}
        Sepal length           & Sepal width           & Petal length           & Petal width           & Species           \\ \hline
        {% for _, row in df.iterrows() %}
        {{row.sepal_length}} & {{row.sepal_width}} & {{row.petal_length}} & {{row.petal_width}} & {{row.species}} \\
        {% endfor %}
    \end{tabular}

\end{document}
```

and note that you have basic autocompletion and syntax highlighting on the Jinja2 commands.

Then you can use a Python file like

```python
import os
import jinja2
import pandas as pd

latex_jinja_env = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.abspath('.'))
)

template_file = latex_jinja_env.get_template('jinja2-test-template.tex')
df = pd.read_csv('https://raw.githubusercontent.com/mwaskom/seaborn-data/master/iris.csv').head()
rendered_template = template_file.render(df=df, text='Table:')
with open('jinja2-test.tex', 'w') as f:
    f.write(rendered_template)
```

which will produce a rendered LaTeX file.
Now you can run the Python file to check that it works, run the produced LaTeX file using TeXiFy, and when it works you can edit the LaTeX run configuration to add under `Before launch` the Python run configuration.

If at any time you encounter problems because Jinja is not interpreting the LaTeX correctly, you can use different Jinja delimiters.
For example, as given by [this blog post](https://web.archive.org/web/20121024021221/http://e6h.de/post/11/) you can use

```python
import os
import jinja2
import pandas as pd

latex_jinja_env = jinja2.Environment(
    block_start_string='\BLOCK{',
    block_end_string='}',
    variable_start_string='\VAR{',
    variable_end_string='}',
    comment_start_string='\#{',
    comment_end_string='}',
    line_statement_prefix='%%',
    line_comment_prefix='%#',
    trim_blocks=True,
    autoescape=False,
    loader=jinja2.FileSystemLoader(os.path.abspath('.'))
)

template_file = latex_jinja_env.get_template('jinja2-test-template.tex')
df = pd.read_csv('https://raw.githubusercontent.com/mwaskom/seaborn-data/master/iris.csv').head()
rendered_template = template_file.render(df=df, text='Table:')
with open('jinja2-test.tex', 'w') as f:
    f.write(rendered_template)
```

and

<!-- ```latex -->
```
\documentclass{article}

\begin{document}

    \VAR{text}

    \begin{tabular}{lllll}
        Sepal length           & Sepal width           & Petal length           & Petal width           & Species           \\ \hline
        \BLOCK{ for _, row in df.iterrows() }
        \VAR{row.sepal_length} & \VAR{row.sepal_width} & \VAR{row.petal_length} & \VAR{row.petal_width}
        & \VAR{row.species} \\
        \BLOCK{ endfor }
    \end{tabular}

\end{document}
```

to get the same result.
