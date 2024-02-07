# Running a LaTeX file

For a general overview on how to work with run configurations, see [Run/debug configurations | IntelliJ IDEA Documentation](https://www.jetbrains.com/help/idea/run-debug-configuration.html).

## Gutter icons

Next to every `\begin{document}`, there will be a gutter icon with which you can easily start compilation of the file.
The first time you do this, a new run configuration will be generated.
If one exists already for that file, that run configuration will be reused.
You can also right-click in a LaTeX file and select <ui-path>Run myfilename.tex</ui-path>.

## About run configurations

See [https://www.jetbrains.com/help/idea/creating-and-editing-run-debug-configurations.html](https://www.jetbrains.com/help/idea/creating-and-editing-run-debug-configurations.html)

To run all run configurations in the project, you can use the `Build Project` button next to the run configurations dropdown.

## Template run configurations

For the LaTeX run configuration you can change the default template.
This means you can choose for example your favourite compiler or pdf viewer in the template, and it will be used when a new run configuration is created.
Note that choosing a main file to compile in the template generally is not useful because it will be different for each new run config, and when creating a run config from context (like when using the gutter icon next to `\begin{document}`), it will be overwritten anyway.
In principle, all other settings in the run configuration you can configure in the template.
This includes the output path, using the `{mainFileParent}` and `{projectDir}` placeholders which will be resolved when the run configuration is created.
See the [Output path section](Run-configuration-settings.md#set-a-custom-path-for-output-files) below.

You can change the template on two levels, project and global level.

### Changing the project run configuration template

When changing this template, only new run configurations created in that project will be affected.

Open the Run/Debug Configurations by clicking on the dropdown at the top and selecting Edit Configurations.
Then go to Templates, select LaTeX and edit it.
For more information, see [https://www.jetbrains.com/help/idea/changing-default-run-debug-configurations.html](https://www.jetbrains.com/help/idea/changing-default-run-debug-configurations.html)

### Changing the run configuration template for new projects

When changing this template, all new run configurations created in any new project will be affected.

Go to <ui-path>File | Other Settings | Run configuration Templates for New Projects</ui-path> and select LaTeX.

## Log messages

_Since b0.7_

![Log tab image](log-tab.png)

After running a run configuration, next to the full Console output there is a Log message tab which contains errors and warnings from the log.
You can focus this tab by default by right-clicking and selecting Focus On Startup.

By double-clicking on a warning or error you can jump to the source file and line if these are known.

In the left toolbar, there are the following buttons.

* Filtering warnings: when unselected, warnings will not be shown.
* Filtering `\overfull hbox`-like warnings: when unselected, these warnings will not be shown.
* Filtering bibtex messages by latexmk: when using latexmk, unselecting this option will hide the bibtex messages.
* Expand all: expand all files.
* Collapse all: collapse all files.

Right-clicking in the log message tab will bring up a context menu in which you can for example select 'Navigate with single click' or 'Export to Text File'.

If you see anything in the log tab which could be improved, especially errors/warnings we may have missed, please [open an issue](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/new).

The log message parsing is mostly based on the list from the [Errors and warnings](Errors-and-warnings.md) page.

## Automatic compilation

_Since b0.6.8_

<ui-path>File | Settings | Languages & Frameworks | TeXiFy</ui-path>

TeXiFy supports automatic compilation, which means that when you type in your document, it will automatically be compiled.
In general, we advise against using this because once you are somewhat familiar with LaTeX there is no need to compile all the time to check if what you typed is correct, and compiling so much will have a serious impact on your CPU usage which can slow other things (including IntelliJ itself) down considerably.

When you start typing in a document, the run configuration that is selected in the dropdown menu at the top is run, so make sure you select the right one first.
Recall that you can create a run config with <shortcut>Ctrl + Shift + F10</shortcut>.

When you use Evince or Okular, you need to compile manually (so using the button next to the run configuration or using <shortcut>Shift + F10</shortcut>) once before you can use forward search.
This is because you need to tell TeXiFy where the pdf is that you want to forward search to, which depends on the run configuration.
Since these pdf viewers will focus when you forward search, this is not done while you are typing.
This is not the case for SumatraPDF, which can forward search without losing focus.

Currently the automatic compilation is only triggered when you type in a document or use backspace, not yet by other ways you edit the document.

![autocompile](autocompile.gif)

## Automatic compilation only when the document is saved.
You can enable this in TeXiFy settings.
When enabled, auto compilation will only run when the file is actually saved to disk, instead of after every change.
To configure when a file is saved to disk (for example after a certain idle time), go to <ui-path>Settings | Appearance | System Settings</ui-path>.
Also see [https://www.jetbrains.com/help/idea/saving-and-reverting-changes.html](https://www.jetbrains.com/help/idea/saving-and-reverting-changes.html)

### Automatic compilation support by compilers

Some compilers, at least latexmk and tectonic, also support automatic compilation.
It depends on your preferences whether this will work better than the autocompile in TeXiFy or not.
See [Compilers](Run-configuration-settings.md#latex-compilers) for more information.

## Installing LaTeX packages

How you should install a LaTeX package differs per LaTeX distribution.

Note that the name of your _distributions_ package may be different from the _LaTeX_ package, although often they are the same.
If unsure, check https://ctan.org/pkg/packagename which package you have to install to get your LaTeX package.

### Installing a package when you have the MiKTeX distribution

* Open the MiKTeX console, click Packages and search for the right package to install it. For more information, see [https://miktex.org/howto/miktex-console](https://miktex.org/howto/miktex-console)

### Installing a package when you have the TeX Live distribution

* Open a terminal window (in IntelliJ, click Terminal at the bottom) and run `tlmgr install packagename`. tlmgr stands for Tex Live ManaGeR.
