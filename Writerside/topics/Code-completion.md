# Code completion

TeXiFy automatically completes many structures as you type.
Some examples are:

* Automatic insertion of `\end` when typing `\begin` for environments
* On typing `\[` automatic insertion of `\]`, after <shortcut>Enter</shortcut> also with correct indentation
* Automatic insertion of braces of first required parameter
* Typing `}` at closing brace skips over it, same for `$..$`

Some of this basic code completion can be disabled in <ui-path>Settings | Languages & Frameworks | TeXiFy</ui-path>.

See the [IntelliJ documentation about Code Completion](https://www.jetbrains.com/help/idea/auto-completing-code.html).

TeXiFy supports autocomplete of labels, all commands from installed LaTeX packages, user defined commands, and (user defined) environments.

This includes for example commands you defined with the `xparse` package.

![xparse-autocomplete](xparse-autocomplete.png)

## Autocompletion for all words

If you are looking for a 'dumb' autocompletion mode to autocomplete on any word in the file/project, you can use Hippie completion: [https://www.jetbrains.com/go/guide/tips/cyclic-expand-word/](https://www.jetbrains.com/go/guide/tips/cyclic-expand-word/) and [https://www.jetbrains.com/help/idea/auto-completing-code.html#hippie_completion](https://www.jetbrains.com/help/idea/auto-completing-code.html#hippie_completion)

## GitHub Copilot

GitHub Copilot is machine-learning based autocompletion provided as a separate plugin.
It can give larger suggestions than TeXiFy will do, but in some cases the completion conflicts with or confuses TeXiFy, so use with care.

![Copilot](copilot.png)

![Copilot](copilot2.png)

## Autocompletion of required parameters
_Since b0.6.9_

When invoking autocomplete on a command or environment that takes required parameters, TeXiFy will insert a brace pair for each parameter.
The caret is placed in the first pair, and you can use <shortcut>Tab</shortcut> to skip to the next pair.
Since optional parameters are, well, optional, both the command with and without the optional parameters appear in the autocomplete list.
If you always select the version with optional parameters, after a couple of times IntelliJ will remember your choice and show it first (so above the version without optional parameters).

![required-parameters-autocomplete](required-parameters-autocomplete.gif)
![required-parameters-environments](required-parameters-environments.gif)

## Autocompletion of commands from installed LaTeX packages.
_Since b0.7.4_

TeXiFy will look in your LaTeX installation for installed LaTeX packages, and then figures out what commands those packages provide to put those in the autocompletion.
The indexing of all packages can take significant time (up to one minute for TeX Live full with thousands of packages) so this is persistent between restarts.
If you want to reset the index, use <ui-path>File | Invalidate Caches / Restart</ui-path>.

Often, the extracted information includes the command parameters and some documentation about the command (see [LaTeX documentation](LaTeX-documentation)).
However, this relies on package authors respecting the LaTeX conventions (using the doc package).
If you find something incorrect, please let us know and then we can determine whether something needs to be improved in the LaTeX package or in TeXiFy.

In the case of TeX Live, TeXiFy will currently not suggest commands from _all_ packages you have installed, because a lot of users have TeX Live full installed, so you would get completion for _all_ commands in _any_ LaTeX package ever written!
This would flood the completion with many commands that are very rarely used.
Therefore, TeXiFy will only suggest commands from packages that you have already included somewhere in your project, directly or indirectly via other packages.

![command-autocomplete1](command-autocomplete1.png)
![command-autocomplete2](command-autocomplete2.png)

### MiKTeX admin install

With MiKTeX, TeXiFy needs to extract zipped files in order to obtain source files of LaTeX packages.
If you installed MiKTeX as admin, this will not be possible.
The MiKTeX installer clearly warns about this:

![miktex-admin](miktex-admin.PNG)

On Linux the warning is less clear though:

![miktex-linux](miktex-linux.png)


## Inserting \item in itemize environments

When writing in an itemize-like environment, pressing <shortcut>Enter</shortcut> will automatically insert an `\item` on the next line.
This allows for easy writing of lists.

If you are writing an item in the list but you do want a linebreak, for example to start a new sentence, use <shortcut>Shift + Enter</shortcut>.

If your cursor is in the middle of a line and you want to split it, but without inserting an `\item` in the middle, use <shortcut>Ctrl + Enter</shortcut>.

An example which shows the use of <shortcut>Enter</shortcut> at the end of a line, <shortcut>Enter</shortcut> at the middle of a line, <shortcut>Shift + Enter</shortcut> and <shortcut>Ctrl + Enter</shortcut> (in that order):

![itemize-enter](itemize-enter.gif)

Note that for even quicker insertion of an itemize you can use live templates (`itm` for itemize by default) as described in [Live templates](#live-templates).

## Brace matching

TeXiFy matches braces, inline math and more.
If you type the first one, the second one will be automatically inserted.
If you then continue typing, you can exit the brace pair by typing the closing brace or dollar sign, or you can use tab if you have <ui-path>File | Settings | Editor | General | Smart Keys | "Jump outside closing bracket with Tab when typing"</ui-path> enabled.

![brace-matching](brace-matching.png)

![brace-matching2](brace-matching2.png)

## Live templates

Using live templates, you can quickly insert a predefined piece of text by typing just a few characters of a certain key.
You can denote places to which the cursor skips when you press <shortcut>Tab</shortcut> after inserting the live template.

To use a live template, type (a part of) the key, for example `fig`, hit enter when the live template is suggested in the autocomplete, type things and use <shortcut>Tab</shortcut> to skip to the next place to type information.

![live-templates](live-templates.gif)

Currently implemented by default are live templates for:

* figures, tables, itemize, enumerate, and in math for summations and integrals;
* sectioning with automatic label (triggered with `\partl`, `\chapl`, `\secl`, etc.), _since b0.7.3_.

You can find these live templates, as well as add your own, under <ui-path>File | Settings | Editor | Live Templates | LaTeX</ui-path>. _Since b0.7.4:_ the default live templates are disabled in verbatim contexts.

![live-template-settings](live-template-settings.png)

For more information, see [https://www.jetbrains.com/help/idea/creating-and-editing-live-templates.html](https://www.jetbrains.com/help/idea/creating-and-editing-live-templates.html)


## Postfix code completion

_Since b0.6.10_

Using postfix templates, you can quickly add some code to the text you previously typed.
You can apply a postfix template to the previous word (or command) by typing a `.` directly after the word and then typing the key of the template.

![text-postfix](text-postfix.gif)

Currently available are commands for text decoration, and some commonly used math mode accents.

![math-postfix](math-postfix.gif)

A list of available templates is in <ui-path>File | Settings | Editor | General | Postfix Completion</ui-path>, where you can edit the key of a template. If you want to add your own postfix templates, have a look at the [Custom Postfix Templates](https://plugins.jetbrains.com/plugin/9862-custom-postfix-templates) plugin. The plugin allows the creation of custom postfix templates for a number of languages, including Latex.

See also [https://www.jetbrains.com/help/idea/auto-completing-code.html#postfix_completion](https://www.jetbrains.com/help/idea/auto-completing-code.html#postfix_completion).

## Smart quote substitution

The <control>csquotes</control> package provides the `\enquote` command.
TeXiFy can automatically insert the command if you type regular quotes, see [Global Settings](Global-settings.md#csquotes).