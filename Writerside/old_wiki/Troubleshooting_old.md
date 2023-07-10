If you run into troubles while using TeXiFy, you can try the following things.

* If you have problems with installation, make sure you followed the [Installation wiki page](Installation).
* Search through the features list at [Features](Features) or the [complete wiki](Features#searching) to find documentation about the feature you’re having problems with.
* Search through the [GitHub issues](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues?q=is%3Aissue) to find similar problems, and if your issue has already been resolved
* Look through the list of common problems at this page
* Ask for help at [gitter](https://gitter.im/TeXiFy-IDEA)
* [Open an issue](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/new/choose)

## The equation/TikZ preview is not working

Make sure you have installed the dependencies as described in [the Preview page](Preview#Equation-preview).

If that doesn’t help and you can run Kotlin programs, you can run the program below and report the output in your (new) issue.

+++ &lt;details>&lt;summary> +++
`preview.kt`
+++ &lt;/summary>&lt;div> +++

```kotlin
import java.io.File
import java.io.PrintWriter
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

/**
* Repeat the behaviour of TeXiFy as close as possible while providing debug prints.
*/
fun main() {

    // Test constants

    val preamble = """
        \pagestyle{empty}

        \usepackage{color}

        \usepackage{amsmath,amsthm,amssymb,amsfonts}
    """.trimIndent()

    val previewCode = "\$\\xi\$"
    val isWindows = false
    val waitTime = 3L

    // Actual code

    fun runCommand(command: String, args: Array<String>, workDirectory: File): String? {

        val executable = Runtime.getRuntime().exec(
            arrayOf(command) + args,
            null,
            workDirectory
        )

        val (stdout, stderr) = executable.inputStream.bufferedReader().use { stdout ->
            executable.errorStream.bufferedReader().use { stderr ->
                Pair(stdout.readText(), stderr.readText())
            }
        }

        executable.waitFor(waitTime, TimeUnit.SECONDS)

        if (executable.exitValue() != 0) {
            println("$command exited with ${executable.exitValue()}\n$stdout\n$stderr")
            return null
        }

        return stdout
    }


    fun inkscapeExecutable(): String {
        var suffix = ""
        if (isWindows) {
            suffix = ".exe"
        }
        return "inkscape$suffix"
    }

    fun pdf2svgExecutable(): String {
        var suffix = ""
        if (isWindows) {
            suffix = ".exe"
        }
        return "pdf2svg$suffix"
    }

    fun runPreview(tempDirectory: File) {

        val tempBasename = Paths.get(tempDirectory.path.toString(), "temp").toString()
        val writer = PrintWriter("$tempBasename.tex", "UTF-8")

        val tmpContent = """\documentclass{article}
$preamble

\begin{document}

$previewCode

\end{document}"""

        writer.println(tmpContent)
        writer.close()

        println("Running latex in " + tempDirectory.path)

        println(
            runCommand(
                "pdflatex",
                arrayOf(
                    "-interaction=nonstopmode",
                    "-halt-on-error",
                    "$tempBasename.tex"
                ),
                tempDirectory
            )
        )

        println("Running pdf2svg...")

        println(
            runCommand(
                pdf2svgExecutable(),
                arrayOf(
                    "$tempBasename.pdf",
                    "$tempBasename.svg"
                ),
                tempDirectory
            )
        )

        println("Running inkscape...")

        runCommand(
            inkscapeExecutable(),
            arrayOf(
                "$tempBasename.svg",
                "--export-area-drawing",
                "--export-dpi", "1000",
                "--export-background", "#FFFFFF",
                "--export-png", "$tempBasename.png"
            ),
            tempDirectory
        ) ?: throw AccessDeniedException(tempDirectory)

        println("Check out the end result in $tempBasename.png")
    }

    try {
        runPreview(createTempDir())
    } catch (e: AccessDeniedException) {
        println("Trying again in user home dir...")
        runPreview(createTempDir(directory = File(System.getProperty("user.home"))))
    }
}
```

+++ &lt;/div>&lt;/details> +++

## Error running 'main': Cannot run program "pdflatex"

Make sure you have followed all the installation instructions at [https://github.com/Ruben-Sten/TeXiFy-IDEA#installation-instructions-installing-intellij-and-the-texify-idea-plugin](https://github.com/Ruben-Sten/TeXiFy-IDEA#installation-instructions-installing-intellij-and-the-texify-idea-plugin)

1. Check if `pdflatex` is properly installed by running in a terminal or command prompt `pdflatex -v`. Probably this is not the case. If it is the case, then for some reason pdflatex cannot run. Test this with `pdflatex small2e` in a location where you have write access.
2. Check if `pdflatex` is installed: if you have MikTeX start the MikTeX console and check that the `pdftex` package is installed. If you have TeX Live, check with `tlmgr install pdftex`.
3. If so, make sure you have logged in and out to complete the installation of LaTeX, and especially with TeX Live make sure that TeX Live is added to your PATH.
4. Make sure you did install MikTeX or TeX Live _for your user only_, so not for all users. If not, uninstall, install the right way and reboot.
5. If you are on Windows or Mac and installing MikTeX, you can also try installing TeX Live instead.
6. Ask on [https://tex.stackexchange.com,](https://tex.stackexchange.com,) providing as much details as possible (at least operating system, results of the tests of the first step, any attempts to solve it).

## What should my document structure look like?

In general you have a main file which contains the documentclass and the document environment (the `\begin{document}` and `\end{document}`).
From here you can include other files which can then include even more files and so on.
An example is:

```latex
\documentclass{exam}

% Packages
\usepackage{amsthm}

\author{L.A.\ TeX}
\title{Example document setup}

% Possible other definitions, configurations etc. that you need
\theoremstyle{definition}
\newtheorem{theorem}{Theorem}

% Document
\begin{document}

    \maketitle

    \section{Introduction}\label{sec:introduction}
    \input{introduction}

    \section{Example theorems} \label{sec:exampleTheorem}
    \input{example-theorems}

\end{document}
```

where the files `introduction.tex` and `example-theorems.tex` contain just the content, for example these could be the complete file contents of `introduction.tex`:

```latex
\begin{theorem}
    If the meanings of 'true' and 'false' were switched, then this sentence wouldn't be false.
\end{theorem}
```

## I have an error which complains about openout_any = p

When you get during compilation the error
```
makeindex: Not writing to /path/to/project/latexfile.ind (openout_any = p).
Can't create output index file /path/to/project/latexfile.ind.
```

or
```
bibtex: Not writing to ../out/latexfile.blg (openout_any = p).
```

this probably happens because you are trying to use makeindex or bibtex with a separate output directory. You should either disable the out directory in the run config or change the security setting in `texmf.cnf`, see [this tex.stackexchange.com answer](https://tex.stackexchange.com/questions/12686/how-do-i-run-bibtex-after-using-the-output-directory-flag-with-pdflatex-when-f/289336#289336).
This is a TeX Live security setting which you can change by editing `/path/to/texlive/2019/texmf-dist/web2c/texmf.cnf` and changing `openout_any = p` to `openout_any = a`.

## My index is not generated correctly

If you have TeXiFy version 0.6.7 or later, make sure that makeindex is run by TeXiFy, so you should see a run window called makeindex with the output.
See [Makeindex](Makeindex).

### Version 0.6.6 or older
Note that you _have_ to disable both the `auxil/` (in case of MiKTeX) and `out/` directories in the run configuration, otherwise the `.idx` file will not be found by the index package.

In general we would recommend using the `imakeidx` package like below, but equivalent index packages should also work.

Example:
```latex
\documentclass{article}
\usepackage{imakeidx}
\makeindex

\begin{document}
    \section{Introduction}
    Some \index{keywords} here
    for the \index{Index}.

    Another line with \index{words}.

    \printindex
\end{document}
```

## How can I set-up shortcuts for e.g. `\emph{}` or the itemize environment?

You can define a live template via <ui-path>File | Settings | Editor | Live Templates</ui-path>. For example, for the `itemize` environment, you could use the following template:

```
\begin{itemize}
    \item $PARM1$
\end{itemize}
```

Set the template to be applicable in LaTeX files.
Also see [https://github.com/Hannah-Sten/TeXiFy-IDEA/wiki/Live-templates](https://github.com/Hannah-Sten/TeXiFy-IDEA/wiki/Live-templates)

![Screenshot of live template UI](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Writing/figures/marco.png)

Once the live template is created, close the Settings dialog. Use <ui-path>Edit | Macros > Start Macro Recording</ui-path| and enter the live template abbreviation. Finish recording the macro, and name it. Via <ui-path|Settings | Keymap</ui-path>, assign the macro a key binding such as kbd:[Ctrl + Alt + Shift + I].

Now, you can use the macro key binding and hit kbd:[Enter] to insert a new `itemize` environment with an item. The cursor will automatically move to the first `\item`.

For commands, you can define templates for e.g. `\emph{$PARM1$}`.