## How to view documentation
If you want to have quick links to package documentation pdfs, make sure you have installed `texdoc`, for example on TeX Live with `tlmgr install texdoc`.
Then place your cursor on a LaTeX command and press kbd:[Ctrl + Q].
If the command has a package dependency which is known to TeXiFy, you will get a popup which includes links to the package documentation that is installed locally on your machine.
LaTeX package documentation is written in LaTeX (surprise) so when you click on a link it will open a pdf.

When the command is a `\usepackage` or `\documentclass` then the documentation of the included package or class will be shown.

When your cursor is on an environment name, documentation for that environment will be shown.

Note that you can also use the shortcut kbd:[Ctrl + Q] during autocompletion of commands and environments, and navigate through the completion list using the arrow keys.

![texdoc](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Reading/figures/texdoc.png)
![env-docs](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Reading/figures/env-docs.png)

## Source of documentation
When you have set up a [LaTeX SDK](Latex-Sdk), in most cases you will have all commands and environments from all installed LaTeX packages in the autocompletion (see [Autocomplete](Autocomplete#command-completion)).
In a lot of cases, this includes some documentation for each command and environment.
However, this relies on package authors respecting the LaTeX conventions (using the doc package) whether the documentation is actually useful.
If you find something incorrect, please let us know and then we can determine whether something needs to be improved in the LaTeX package or in TeXiFy (example bug report: [https://gitlab.com/axelsommerfeldt/caption/-/issues/114](https://gitlab.com/axelsommerfeldt/caption/-/issues/114)).
