# Run configuration settings

TeXiFy now uses a **step-based LaTeX run configuration**. A run configuration is built from:

* Common settings (main file, directories, distribution, environment variables)
* A **Compile sequence** (ordered list of steps)
* **Step settings** (options of the selected step)

## Switching compilers

Open <ui-path>Run | Edit Configurations</ui-path>, select your LaTeX run configuration, then:

1. In **Compile sequence**, select the compile step (or add one).
2. In **Step settings**, choose:
   1. `latex-compile` + compiler (`pdfLaTeX`, `LuaLaTeX`, `XeLaTeX`, etc.)
   2. or `latexmk-compile`.

You can also double-click a step in the sequence to change its type.

## Compile sequence and step settings

The left side (**Compile sequence**) controls execution order.
The right side (**Step settings**) edits the currently selected step.

You can:

* add/remove/reorder steps
* drag-and-drop to reorder
* use **Auto configure** to complete a minimal pipeline from current context

Supported step types include:

* `latex-compile`
* `latexmk-compile`
* `bibtex`
* `makeindex`
* `makeglossaries`
* `pythontex`
* `xindy`
* `external-tool`
* `file-cleanup`
* `pdf-viewer`

### Using magic comments to specify the compiler for new run configurations

To set the compiler of a document for **newly created** run configurations, put the magic comment

```
%! Compiler = [compiler executable] [compiler arguments]
```

at the top of the root file.
The syntax `%! Program =` is also supported.

To set the BibTeX compiler for **newly created** run configurations, put

```
%! BibTeX Compiler = [compiler executable] [compiler arguments]
```

at the top of the LaTeX root file.

Example:

```
%! Compiler = lualatex --shell-escape
%! BibTeX Compiler = biber
```

These comments affect only creation/recommendation of new run configurations, not existing ones.

## LaTeX compilers

### pdfLaTeX

`pdfLaTeX` is a stable default compiler.

### LuaLaTeX

Install the `luatex` package.
Use `LuaLaTeX` or `XeLaTeX` when you need modern font support.

### Latexmk {id="latex-compilers-latexmk"}

See [latexmk docs](https://mg.readthedocs.io/latexmk.html).
With TeX Live, install via `tlmgr install latexmk`.

Latexmk compiles as needed and can orchestrate bibliography/index tools.
TeXiFy supports latexmk as a dedicated **compile step type** (`latexmk-compile`).

When a `latexmkrc` is detected, TeXiFy avoids overriding your latexmkrc behavior where possible and still appends configured run options.

#### Tip: compiling automatically when IntelliJ loses focus

With latexmk `-pvc`, files are watched and recompiled on save.

For automatic PDF viewer startup with latexmk itself, configure `$HOME/.latexmkrc` (Linux/macOS) or
<code ignore-vars="true">\%USERPROFILE%\\.latexmkrc</code> (Windows):

```
$pdf_previewer = '"/path to/your/favorite/viewer" %O %S';
```

### XeLaTeX

Install the `xetex` package.

### Texliveonfly

Install `texliveonfly` when you want on-demand package installation in TeX Live.

### Tectonic {id="latex-compilers-tectonic"}

See [Tectonic docs](https://tectonic-typesetting.github.io/en-US/).

## Latexmk run configuration {id="latexmk-run-configuration"}

TeXiFy no longer uses a separate Latexmk run configuration type.
Use a regular **LaTeX run configuration** with a `latexmk-compile` step.

### Compile mode

`latexmk-compile` supports compile modes such as:

* `PDFLATEX_PDF`
* `LUALATEX_PDF`
* `XELATEX_PDF`
* `XELATEX_XDV`
* `LATEX_DVI`
* `LATEX_PS`
* `CUSTOM`

When `CUSTOM` is selected, provide a custom engine command.

### Citation tool

`latexmk-compile` supports citation tool options (`AUTO`, `BIBTEX`, `BIBER`, `DISABLED`).

### Additional latexmk arguments

Use this field for extra latexmk flags such as `-pvc`, `-silent`, etc.

### Output / auxiliary / working directory

These are configured in common run settings (not per step).
`{mainFileParent}` and `{projectDir}` placeholders are supported.

### LaTeX Distribution

Distribution selection applies to latexmk steps as well.

### PDF viewer and focus

Use a `pdf-viewer` step to control viewer behavior.

### Before-run LaTeX code

For the regular LaTeX compile step, use the 'Run code before compilation' setting in the common settings, so it applies to all LaTeX compile steps.
With the example main file below, just put `\newcommand{\waarde}{false}` in the text field.

For latexmk, use the `-usepretex` command line argument to run code before the main document.
For example:

```
\documentclass{article}
\usepackage{etoolbox}

\providecommand{\waarde}{true}
\newbool{binair}
\setbool{binair}{\waarde}

\begin{document}
    \ifbool{binair}{waar}{onwaar}
\end{document}
```

Then provide `-usepretex="\newcommand{\waarde}{false}" -g` as arguments (the `-g` is to force recompilation after updating the pretex code).

### Custom latexmk executable path

Set it in `latexmk-compile` step settings.

### Cleaning generated files with Latexmk

When selected run config uses latexmk, TeXiFy cleanup actions use latexmk clean modes.

### Cleaning temporary build files

Use a `file-cleanup` step to remove temporary build artifacts for the current document while preserving final outputs such as PDF.

For latexmk-based configurations, this step runs `latexmk -c`.

For classic compile flows, TeXiFy removes temporary artifacts such as `.aux`, `.log`, `.bbl`, `.synctex`, and `.synctex.gz` from the current document's source/output/auxiliary locations.

## BibTeX compilers

For bibliography compiler details, see [BibTeX](BibTeX.md).

## Custom compiler path

Set per compile step (`latex-compile` or `latexmk-compile`).

## Custom compiler arguments

Set per compile step.

Argument autocompletion is available for:

* `latex-compile` (based on selected compiler executable)
* `latexmk-compile` (latexmk options)

## Environment variables

You can pass environment variables to the command that is run to compile the LaTeX file.
There is an option to include system variables.

#### Including LaTeX files from a different directory
You can use for example the `TEXINPUTS` environment variable to include LaTeX files in a different directory anywhere on your system.
For example `TEXINPUTS=/path/to/directory//:`, where `//` means that LaTeX (and TeXiFy) will search in any subdirectory of `/path/to/directory` for the file to be included, and `:` means to include the standard content of `TEXINPUTS`. 

For Windows, it is similar: `TEXINPUTS=C:...\path\to\directory\\;` (note the semicolon, which adds the default value of TEXINPUTS).

Similarly, you can also set `TEXMFHOME` to some other path than the default `~/texmf`, so that sty and cls files will be searched in the `tex/latex` subdirectory or any child directory of it.
For more information about paths resolving, see [https://www.tug.org/texinfohtml/kpathsea.html#Path-searching](https://www.tug.org/texinfohtml/kpathsea.html#Path-searching)

### Expand macros in environment variables

When ticked, macros such as `$ContentRoot$` (the path to the content root of the current run configuration's main file) are expanded.
An example use for this would be to add a directory containing the document class to be used to `TEXINPUTS`, e.g., `TEXINPUTS=$ContentRoot$/MyDir//:`.
Doing so might be especially helpful in the context of setting up a 'run configuration template,' which is a run configuration that is used by default for any time a LaTeX file is run (and thus compiled) for the first time. For more details on run configuration templates, see [https://www.jetbrains.com/help/idea/run-debug-configuration.html#templates](https://www.jetbrains.com/help/idea/run-debug-configuration.html#templates).

An overview of all built-in macros can be found at [https://www.jetbrains.com/help/idea/built-in-macros.html](https://www.jetbrains.com/help/idea/built-in-macros.html).
Whenever the documentation mentions 'the current file,' in the context of a TeXiFy run configuration, this always refers to the main `.tex` file being compiled.


## LaTeX code to run before compiling the main file

Compile steps support LaTeX snippets that are injected before compilation.

## Choose pdf viewer

Configure with a `pdf-viewer` step.

Supported viewers include internal viewer and platform-specific external viewers.

## Allow PDF viewer to focus after compilation

Configure in the `pdf-viewer` step.

## Custom pdf viewer

In `pdf-viewer` step settings, select custom viewer and use command with optional `{pdf}` placeholder.

## Choose LaTeX source file to compile

Set **Main file** in common settings.

## Set a custom path for auxiliary files

Set **Auxiliary directory** in common settings.

By default TeXiFy uses clean project-local directories (`out` and `auxil`).

### Bibtex and TeX Live

When BibTeX needs path help (for example with separated aux/output directories), TeXiFy adjusts execution context and environment so bibliography tools can still resolve inputs.

### Makeindex

Index-related steps are supported in the step pipeline, including artifact synchronization behavior required by index workflows.

### Minted

If using `minted` with custom aux/output layout, configure `outputdir` accordingly (for example `\usepackage[outputdir=../auxil]{minted}`).

## Set a custom path for output files

Set **Output directory** in common settings.

You can use `{mainFileParent}` and `{projectDir}` placeholders.

## Always compile twice

This is no longer a dedicated checkbox.

Pipeline closure now follows step semantics:

* Classic `latex-compile` flow: TeXiFy may add follow-up compile steps.
* `latexmk-compile` flow: TeXiFy avoids duplicate consecutive latexmk compiles and only adds required follow-up compile after auxiliary steps.

## Choose output format

Configure output format in `latex-compile` step settings.
For `latexmk-compile`, use latexmk compile mode.

## Choose LaTeX distribution

Distribution is configured in common settings.

### Dockerized MiKTeX

Supported when Dockerized distributions are available.

### Dockerized TeX Live

Supported similarly.

### TeX Live from WSL

Supported on Windows environments with WSL setup.

## Choose External LaTeX tool run configuration

Use `external-tool` step in the compile sequence.

## Other tasks to run before the run configuration, including other run configurations or external tools

Use IntelliJ "Before launch" tasks for extra pre-run actions:
[Run/Debug before launch options](https://www.jetbrains.com/help/idea/run-debug-configurations-dialog.html#before-launch-options)
