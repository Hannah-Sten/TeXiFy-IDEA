_Since b0.6.9_

In general, if [Go to declaration](Go-to-declaration) on something works, then Find Usages will probably work as well.
This holds for at least command definitions, labels and bibtex citations.

## Find usages for commands
As a complement for [Go to declaration](Go-to-declaration), you can easily find usages of LaTeX commands you defined in your document, for example using `\newcommand` or `\DeclareMathOperator`, using <shortcut>Ctrl + B</shortcut>.
Note that this is the same shortcut as for Go to declaration.

![find-usages](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Navigation/figures/find-usages.png)

## Find usages for labels

The same functionality exists for labels, where `\label{mylabel}` is a definition and commands like `\ref{mylabel}` are usages.

Note that your cursor needs to be on the label itself, not on the commands, so `\label{sec:my-<cursor>section}` works but `\lab<cursor>el{sec:my-section}` does not.

Also see [Refactoring](Refactoring).
