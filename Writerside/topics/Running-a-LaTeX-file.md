# Running a LaTeX file

For a general overview of run configurations, see [Run/debug configurations | IntelliJ IDEA Documentation](https://www.jetbrains.com/help/idea/run-debug-configuration.html).

## Gutter icons

Next to `\begin{document}`, TeXiFy shows a gutter run icon.

The first run creates a LaTeX run configuration for that file.
Later runs reuse the same run configuration.

You can also right-click a `.tex` file and use <ui-path>Run myfilename.tex</ui-path>.

## About run configurations

TeXiFy LaTeX execution uses a **step-based** run configuration.
A run consists of ordered steps (compile, bibliography/index/tool steps, viewer).

For IntelliJ run configuration basics, see
[Creating and editing run/debug configurations](https://www.jetbrains.com/help/idea/creating-and-editing-run-debug-configurations.html).

To run all run configurations in a project, use **Build Project** next to the run configuration dropdown.

## Template run configurations

You can customize the **LaTeX run configuration template**.
New run configurations created from context use that template as the starting point.

Typical template customizations:

* default compile sequence (for example `latexmk-compile -> pdf-viewer`)
* default output/auxiliary directories (`{mainFileParent}`, `{projectDir}` placeholders)
* default PDF viewer and distribution

Changing a template affects only newly created run configurations.
Existing run configurations are not rewritten.

### Changing the project run configuration template

Open <ui-path>Run | Edit Configurations</ui-path>, then edit the LaTeX template.

### Changing the run configuration template for new projects

Go to <ui-path>File | Other Settings | Run Configuration Templates for New Projects</ui-path> and edit the LaTeX template.

## Log messages

After running, TeXiFy provides a **Step Log** tab.

The tree view is organized as:

* run node
* step nodes
* parsed warning/error message nodes

Selection behavior:

* selecting run root shows merged raw output of all steps
* selecting a step shows that step's raw output
* selecting a message tries to jump the console to the corresponding log position
* if a message cannot be mapped to a valid console offset, the console is **not refreshed**

Double-clicking a message still navigates to source file/line when location data is available.

Tree toolbar actions:

* Show Bibtex Messages
* Show Overfull/Underfull Box Messages
* Expand All
* Collapse All

The filter and expand/collapse states are persisted per project workspace.

If you find logs that should be parsed better, please [open an issue](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/new).

## Automatic compilation

_Since b0.6.8_

<ui-path>File | Settings | Languages & Frameworks | TeXiFy</ui-path>

When automatic compilation is enabled, TeXiFy reruns the currently selected LaTeX run configuration after edits.

Notes:

* auto compile uses the run configuration selected in the top toolbar
* for best behavior, run once manually first when viewer sync requires an existing PDF context
* trigger granularity depends on selected auto-compile mode (always / after save / disabled in power save)

![autocompile](autocompile.gif)

### Automatic compilation only when the document is saved.

Enable this in TeXiFy settings.
When enabled, auto compilation runs only after file save.

File save behavior can be configured in <ui-path>Settings | Appearance & Behavior | System Settings</ui-path>.

### Disable automatic compilation in power save mode

Enable this option to auto compile normally but suspend it when <ui-path>File | Power Save Mode</ui-path> is on.

### Automatic compilation support by compilers

Some compilers (for example latexmk, tectonic) also provide their own watch/continuous modes.
Pick either TeXiFy auto compile or compiler-native watch mode based on your workflow.

## Installing LaTeX packages

Package installation depends on your distribution.

### Installing a package when you have the MiKTeX distribution

Use MiKTeX Console > Packages to search and install.

### Installing a package when you have the TeX Live distribution

Run `tlmgr install packagename` in a terminal.
