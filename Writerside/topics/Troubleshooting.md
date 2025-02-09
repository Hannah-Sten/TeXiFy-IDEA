# Troubleshooting

If you run into troubles while using TeXiFy, you can try the following things.

* If you have problems with installation, make sure you followed the [installation guide](Installation-guide.md).
* Search this wiki using the search bar in the top right corner.
* Search through the [GitHub issues](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues?q=is%3Aissue) to find similar problems, and if your issue has already been resolved
* Look through the list of common problems at this page
* Ask for help at [gitter](https://matrix.to/#/#TeXiFy-IDEA_Support:gitter.im)
* [Open a discussion](https://github.com/Hannah-Sten/TeXiFy-IDEA/discussions)

## Debugging performance issues

If you are experiencing UI freezes, IntelliJ will generate a thread dump, please upload this file as well.

For any performance issue: if you do not have a favourite profiler yet, you can use VisualVM. Install it using your package manager or go to [https://visualvm.github.io](https://visualvm.github.io)

* First, just run TeXiFy like usual.
* Start VisualVM.
* In the Applications panel on the left, identify the instance of IntelliJ where TeXiFy is running, probably it is named Idea. Right-click on it and open.
* Go to the Sampler tab.
* Click Settings, and click Profile only packages. Specify `nl.hannahsten.**` (or a specific class you want to filter on. Note that if you want to filter for a Kotlin class you have to append `Kt` to the class name, e.g. `nl.hannahsten.texifyidea.editor.UpDownAutoBracketKt`. However, not all classes will appear in the view.)
* Click CPU to start profiling
* Reproduce the performance issue
* Stop the profiling
* Take a Snapshot to view and save results. Note that you may have to click a few more levels open to see the actual methods.
* Now you can zip the nps file and upload it here on GitHub.

## I don't see any syntax highlighting

If you have the TeXiFy plugin installed but still don't see any syntax highlighting, you may accidentally have reassigned the `.tex` file type to plain text.
To undo this, go to <ui-path>Editor | File Types | LaTeX source file</ui-path> and make sure the `*.tex` pattern is there.
Also check that the Text file type does not have this pattern.
If the problem only occurs for one specific file, right-click the file and select 'Revert File Type Override'.
For more information, see [IntelliJ IDEA Help](https://www.jetbrains.com/help/idea/creating-and-registering-file-types.html).
 
## Main file is not detected correctly

If TeXiFy does not detect which file is your main/root LaTeX file, you may experience problems like package imports being placed in the wrong file, or imports not being resolved correctly.
If this is the case, please report a [GitHub issue](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/new/choose).
Until the problem is fixed, you can use a [Magic comment](Editing-a-LaTeX-file.md#magic-comments) as a workaround.

## Known parser issues

TeXiFy relies on a lexer and parser for most of its functionality.
The parser is relatively strict, and it will not accept all valid LaTeX.
This has the advantage that it is relatively easy to implement features which make use of this imposed structure, but you will always be able to create cases of valid LaTeX which break TeXiFy.
We intend to make the parser such that it will accept almost all LaTeX that we think is well-structured and readable.

If you do encounter a parse error that you think is incorrect, please raise an issue.
As a workaround, if you want to keep syntax highlighting for that part you can use magic comments to disable the formatter (see [Code formatting](Code-formatting.md#disabling-the-formatter)) to avoid it incorrectly formatting your file:

<!-- ```latex -->
```
% @formatter:off
...
% @formatter:on
```

If you are fine without the syntax highlighting for that part, you can disable the parser entirely (see [Magic comments](Editing-a-LaTeX-file.md#magic-comments)).
This will ensure that TeXiFy completely ignores this part of the code, and other parts should remain working fine.

<!-- ```latex -->
```
%! parser = off
...
%! parser = on
```

### Examples of known parser bugs

We have two `\begin` commands but only one `\end` command, so the parser will be confused. ([#2141](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/2141))
Since it isn’t clear which `\begin` should be matched with the `\end`, ideally it wouldn’t try to match them at all.
But the only reason this is valid LaTeX at all is the `\if`, and whether we need to match depends on many things, for example whether the `\end` is inside the `\if` or not.

<!-- ```latex -->
```
\newenvironment{messageTable}[2]
{
    \begin{center}
        #2\\
        \ifx{#1}{2}
            \begin{tabular}{|c|c|}
            \hline
            \textbf{Bytes} & \textbf{Name} \\
        \else
            \begin{tabular}{|c|c|c|}
            \hline
            \textbf{Bytes} & \textbf{Name} & \textbf{Value} \\
        \fi

}
{
        \hline
        \end{tabular}
    \end{center}
}

```

A similar example is using the `before` and `after` parameters of `setlist`,

<!-- ```latex -->
```
\newlist{myitemize}{itemize}{1}
\setlist[myitemize]{label=\textbullet, nosep, left=0pt,
before={\begin{minipage}[t]{\hsize}},
after ={\end{minipage}} }
```

In this case, it may be rewritten to something that can be parsed by TeXiFy, for example,

<!-- ```latex -->
```
\newlist{nosepitemize}{itemize}{1}
\setlist[nosepitemize]{label=\textbullet, nosep, left=0pt}
\newenvironment{myitemize}{
    \begin{minipage}[t]{\hsize} \begin{nosepitemize}
}{
    \end{nosepitemize} \end{minipage}
}
```

## Pasting images and tables into LaTeX

If you drag and drop an image file into a LaTeX file, or paste an image or table from your clipboard, TeXiFy will start a wizard to help you inserting the image or table into your document.
See [Insert Graphics wizard](Tools.md#insert-graphic-wizard) and [Table Creation Wizard](Tools.md#table-creation-wizard) for starting these wizards manually.

### Pasting images from the clipboard

_Since b0.7.3_

You can paste images from your clipboard directly into your LaTeX document. When pasting, you will be prompted by a dialog for saving the image to your workspace. The default folder is "resources", then any source root that is not "src/source(s)", then the source root itself. You can customize the location where the image is going to be saved. You can also specify the file name and the format ("jpg" and "png") are supported. Other formats get converted to "jpg" or "png".

An Insert Graphic dialog will be opened immediately after saving the pasted image.

![demo video](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/writerside-images/paste-image.gif)



## The equation/TikZ preview is not working

Make sure you have installed the dependencies as described in [the Preview page](Tool-Windows.md#equation-preview).

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

<!-- ```latex -->
```
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

<!-- ```latex -->
```
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
See [Makeindex](Run-configuration-settings.md#makeindex).

### Version 0.6.6 or older
Note that you _have_ to disable both the `auxil/` (in case of MiKTeX) and `out/` directories in the run configuration, otherwise the `.idx` file will not be found by the index package.

In general we would recommend using the `imakeidx` package like below, but equivalent index packages should also work.

Example:
<!-- ```latex -->
```
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

![Screenshot of live template UI](macro.png)

Once the live template is created, close the Settings dialog. Use <ui-path>Edit | Macros > Start Macro Recording</ui-path| and enter the live template abbreviation. Finish recording the macro, and name it. Via <ui-path|Settings | Keymap</ui-path>, assign the macro a key binding such as <shortcut>Ctrl + Alt + Shift + I</shortcut>.

Now, you can use the macro key binding and hit <shortcut>Enter</shortcut> to insert a new `itemize` environment with an item. The cursor will automatically move to the first `\item`.

For commands, you can define templates for e.g. `\emph{$PARM1$}`.
