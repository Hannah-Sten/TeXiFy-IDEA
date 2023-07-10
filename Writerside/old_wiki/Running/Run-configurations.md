## About run configurations

See [https://www.jetbrains.com/help/idea/creating-and-editing-run-debug-configurations.html](https://www.jetbrains.com/help/idea/creating-and-editing-run-debug-configurations.html)

To run all run configurations in the project, you can use the `Build Project` button next to the run configurations dropdown.

## Template run configurations

For the LaTeX run configuration you can change the default template.
This means you can choose for example your favourite compiler or pdf viewer in the template, and it will be used when a new run configuration is created.
Note that choosing a main file to compile in the template generally is not useful because it will be different for each new run config, and when creating a run config from context (like when using the gutter icon next to `\begin{document}`), it will be overwritten anyway.
In principle, all other settings in the run configuration you can configure in the template.
This includes the output path, using the `{mainFileParent}` and `{projectDir}` placeholders which will be resolved when the run configuration is created.
See the [Output path section](#outputpath) below.

You can change the template on two levels, project and global level.

### Changing the project run configuration template

When changing this template, only new run configurations created in that project will be affected.

Open the Run/Debug Configurations by clicking on the dropdown at the top and selecting Edit Configurations.
Then go to Templates, select LaTeX and edit it.
For more information, see [https://www.jetbrains.com/help/idea/changing-default-run-debug-configurations.html](https://www.jetbrains.com/help/idea/changing-default-run-debug-configurations.html)

### Changing the run configuration template for new projects

When changing this template, all new run configurations created in any new project will be affected.

Go to <ui-path>File | Other Settings | Run configuration Templates for New Projects</ui-path> and select LaTeX.

## Run configuration settings

### Choose compiler

See [Compilers](Compilers).

### Custom compiler path

Select a path to a LaTeX compiler.

### Custom compiler arguments

Extra arguments to pass to the compiler.
It depends on the compiler which ones are there by default.
For more info, check the implementation at [https://github.com/Hannah-Sten/TeXiFy-IDEA/blob/master/src/nl/hannahsten/texifyidea/run/compiler/LatexCompiler.kt](https://github.com/Hannah-Sten/TeXiFy-IDEA/blob/master/src/nl/hannahsten/texifyidea/run/compiler/LatexCompiler.kt)

### Environment variables

You can pass environment variables to the command that is run to compile the LaTeX file.
There is an option to include system variables.
You can use for example the `TEXINPUTS` environment variable to include LaTeX files in a different directory anywhere on your system.
For example `TEXINPUTS=/path/to/directory//:`, where `//` means that LaTeX (and TeXiFy) will search in any subdirectory of `/path/to/directory` for the file to be included, and `:` means to include the standard content of `TEXINPUTS`. For Windows, it is similar: `TEXINPUTS=C:...\path\to\directory\\;` (note the semicolon).
For more information about paths resolving, see [https://www.tug.org/texinfohtml/kpathsea.html#Path-searching](https://www.tug.org/texinfohtml/kpathsea.html#Path-searching)

### Custom SumatraPDF path

See [(Windows) Choose a custom path to SumatraPDF](Running/SumatraPDF-support#Portable-SumatraPDF)

### Choose pdf viewer
_Since b0.7.2_

This lists all supported pdf viewers that are installed on your system, which you can select as the default pdf viewer.
Selecting a supported viewer as default means that you get forward and inverse search, and that the selected pdf viewer is the viewer that will open when compilation is done.

The supported pdf viewers are [Sumatra](SumatraPDF-support) for Windows, and [Evince](Evince-support) and [Okular](Okular-support) for linux, or no pdf viewer at all.
You can use any other pdf viewer by selecting the option Custom PDF Viewer.

### Custom pdf viewer

In the Custom pdf viewer field you can specify the command for your favourite pdf viewer, so for example if the command is `okular somefile.pdf` then you can fill in `okular`  here.
If the pdf file is not the last argument, you can use the `{pdf}` placeholder, so `okular {pdf}`.

Then when you run the run configuration, when the compilation has finished the pdf will open in the viewer you specified.

If you don’t want to open any pdf viewer for some reason, select the checkbox but leave the field empty.

### Choose LaTeX source file to compile

Select a LaTeX file.

### (MiKTeX only) Set a custom path for auxiliary files

When using MiKTeX, this path will be passed to the `-aux-directory` flag for pdflatex, and similar for other compilers which support an auxiliary directory.

### Set a custom path for output files

This path will be passed to the `-output-directory` for pdflatex, and similar for other compilers which support an output directory.

If you are using pdflatex and bibtex under TeX Live, when your output directory is set to something different than the directory of your main file, then you need to provide the `BIBINPUTS` environment variable in the _bibtex_ run configuration.
This should point to the directory your main file is in, e.g. `BIBINPUTS=../src`.
TeXiFy will automatically do this for you if you create a run configuration from context (for example using the gutter icon next to `\begin{document}`).
The exception to this is when you have changed your `openout_any` setting in TeX Live.

_Since b0.7.1_
You can use the `{mainFileParent}` and `{projectDir}` placeholders here, which will be resolved when you run the run configuration.
The first one resolves to the directory your main file is in, the second to the content root of the main file.
These placeholders are especially useful in template run configurations, so you can specify paths relative to these directories in the template run configuration, when the main file is not yet known.
If you enter for example `{mainFileParent}/out`, then the `out` directory will always be created next to the main file when new run configurations are created.

### Always compile twice

When enabled, TeXiFy will always compile at least twice.
Can be useful to make sure your references are always updated.

### Choose output format

Some compilers support different output formats than just pdf, for example dvi.

### Choose LaTeX distribution

When a different LaTeX distribution is detected, like Dockerized MiKTeX or TeX Live from WSL, you can choose it here.
Note that you can also change this in the [run configuration template](Run-configurations#template) to always use a different LaTeX distribution.

### Choose External LaTeX tool run configuration

You can add [BibTeX](BibTeX), [Makeindex](Makeindex) or [other external tool](External-tools) run configurations to your main LaTeX run configuration.
They will be run appropriately inbetween LaTeX runs.

### Other tasks to run before the run configuration, including other run configurations or external tools

Use this to run anything before the run configuration.
See [https://www.jetbrains.com/help/idea/run-debug-configurations-dialog.html#before-launch-options](https://www.jetbrains.com/help/idea/run-debug-configurations-dialog.html#before-launch-options)