## Built-in

The run configuration External LaTeX Tool can be used to run other auxiliary tools which need to run inbetween LaTeX runs.
Please raise an issue on GitHub if your favourite tool is missing here.
Currently, the following tools are supported.

### Pythontex
_Since b0.7.2_

Pythontex is a LaTeX package which can, among other things, run Python code which you included in your LaTeX file and nicely format the code and the output as well.

```latex
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

### Sage
_Since b0.8.0_

To use Sage, you need to have Sagemath installed.
On some operation systems, you might need to install sagetex separately (for Arch there is the `sagetex` package, for example).
Then, make sure the package is found, for example by adding `TEXINPUTS=/usr/share/texmf//:` to your run configuration, assuming the package is located there.
Now you can add `\usepackage{sagetex}` and TeXiFy should be able to find it.
Then you can use sagetex commands, for example `\sage{EllipticCurve(GF(409),[1,2]).order()}`, and run the file.

If the run configuration is not created correctly the first time, you need to make sure to first run pdflatex, then a general command line step to run `sage main.sagetex.sage`, then pdflatex again.

## Other external tools

### Jinja2 support

Jinja2 is a templating language, so you can write special commands in LaTeX which will be replaced by actual valid LaTeX by a certain Python script.
The difference with using e.g. lualatex or simply outputting LaTeX from Python is that you can still do the formatting in LaTeX, and from the Python script you only give the raw data to be typeset to the LaTeX file (taking an abstract point of view).

PyCharm supports Jinja2 by default, you can enable it for LaTeX by going to menu:File[Settings > Languages & Frameworks] and add LaTeX source file as language.
Also see [https://www.jetbrains.com/help/pycharm/template-languages.html](https://www.jetbrains.com/help/pycharm/template-languages.html)

Then you can write for example a LaTeX file containing

```latex
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

```latex
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
