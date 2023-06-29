_Since b0.7_

![Log tab image](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Running/figures/log-tab.png)

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

If you see anything in the log tab which could be improved, especially errors/warnings we may have missed, please [open an issue](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/new?assignees=&labels=bug%2C+untriaged&template=bug_report.md&title=).

The log message parsing is mostly based on the list from the [Errors and warnings](Errors-and-warnings) page.
