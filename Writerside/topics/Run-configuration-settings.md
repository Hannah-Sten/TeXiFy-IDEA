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
`%USERPROFILE%\.latexmkrc` (Windows):

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

Compile steps support injecting LaTeX code before main file compilation.

### Custom latexmk executable path

Set it in `latexmk-compile` step settings.

### Cleaning generated files with Latexmk

When selected run config uses latexmk, TeXiFy cleanup actions use latexmk clean modes.

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

You can pass custom environment variables to run steps.

`TEXINPUTS` and `TEXMFHOME` are common examples.

### Expand macros in environment variables

When enabled, macros like `$ContentRoot$` are expanded in env values.

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
