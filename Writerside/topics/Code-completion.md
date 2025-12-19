# Code completion

TeXiFy automatically completes many structures as you type.
Some examples are:

* Automatic insertion of `\end` when typing `\begin` for environments
* On typing `\\[` automatic insertion of `\\]`, after <shortcut>Enter</shortcut> also with correct indentation
* Automatic insertion of braces of required parameters
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

## Autocompletion of commands and environments

### Autocompletion of required parameters
_Since b0.6.9_

When invoking autocomplete on a command or environment that takes required parameters, TeXiFy will insert a brace pair for each parameter.
The caret is placed in the first pair, and you can use <shortcut>Tab</shortcut> to skip to the next pair.
Since optional parameters are, well, optional, both the command with and without the optional parameters appear in the autocomplete list.
If you always select the version with optional parameters, after a couple of times IntelliJ will remember your choice and show it first (so above the version without optional parameters).

![required-parameters-autocomplete](required-parameters-autocomplete.gif)

![required-parameters-environments](required-parameters-environments.gif)


### Autocompletion of commands from installed LaTeX packages. {#autocomplete-installed-commands}
_Since b0.7.4_

TeXiFy will look in your LaTeX installation for installed LaTeX packages, and then figures out what commands those packages provide to put those in the autocompletion.
The indexing of all packages can take significant time (up to one minute for TeX Live full with thousands of packages) so this is persistent between restarts.
If you want to reset the index, use <ui-path>File | Invalidate Caches / Restart</ui-path>.

Often, the extracted information includes the command parameters and some documentation about the command (see [LaTeX documentation](Editing-a-LaTeX-file.md#quick-documentation)).
However, this relies on package authors respecting the LaTeX conventions (using the doc package).
If you find something incorrect, please let us know and then we can determine whether something needs to be improved in the LaTeX package or in TeXiFy.

By default, TeXiFy will not suggest all the commands from all installed packages, but only from packages that you have included in your project,
otherwise the list would be flooded with too many commands that are rarely used.
Nevertheless, you can change this behavior in [autocompletion modes](#autocompletion-modes).

![command-autocomplete1](command-autocomplete1.png)

![command-autocomplete2](command-autocomplete2.png)

#### MiKTeX admin install

With MiKTeX, TeXiFy needs to extract zipped files in order to obtain source files of LaTeX packages.
If you installed MiKTeX as admin, this will not be possible.
The MiKTeX installer clearly warns about this:

![miktex-admin](miktex-admin.PNG)

On Linux the warning is less clear though:

![miktex-linux](miktex-linux.png)


### Context-aware autocompletion
_Since b0.11.3_

TeXiFy will try to be context-aware when suggesting commands and environments in the autocompletion.
For example, it will not suggest math-mode commands when you are in text mode, and vice versa.
For user-defined commands and environments, TeXiFy will try to determine their applicable context based on their definition.
This feature is still experimental, so if you find something that does not work as expected, please let us know.
See also [Autocompletion modes](#autocompletion-modes).

![ctx-autocompletion1.png](ctx-autocompletion1.png)

![ctx-autocompletion2.png](ctx-autocompletion2.png)

The hint `in <math>` indicates that the command is only available in math mode.

### User-defined commands and environments
_Since b0.11.3_

TeXiFy will also recognize custom commands and environments defined via `\newcommand`, `\NewDocumentCommand` (from the `xparse` package), and similar commands.
Moreover, TeXiFy will try to determine the context of the command or environment and its parameters.

In the following example, `\ep` is defined as `\varepsilon`, so it is only available in math mode.
Moreover, the first optional parameter of `\mmycmd` is determined to be math mode, because of the `\( #1 \)` in the definition,
so `\ep` is also suggested as a possible completion inside the first parameter of `\mmycmd`.

![custom-command-autocompletion1](custom-command-autocompletion1.png)

![custom-command-autocompletion2](custom-command-autocompletion2.png)

To be more detailed, several context-resolving rules are applied:
* Command alias copies the original semantics (if any).
    * `\newcommand{\ep}{\varepsilon}`
* The applicable context of the user-defined command is guessed from the required context(s) of its definition.
    *  `\newcommand{\R}{\mathbb{R}}`: requires math context because of `\mathbb`.
* The context of the arguments are computed from the code:
    * `\newcommand{\mymath}[1]{\( #1 \)}` makes the first argument of `\mymath` introduce math context.
* The context of environment block is guessed from the end of the begin block definition:
    * `\NewDocumentEnvironment{table3}{}{\begin{tabular}{c c}}{\end{tabular}}`: gives the context of tabular inside the block.

### Autocompletion modes
_Since b0.11.3_

You can choose between three autocompletion modes in <ui-path>File | Settings | Languages & Frameworks | TeXiFy</ui-path>:
* **Smart**(default): context-aware autocompletion as described above, showing only commands and environments that are included in the document and applicable in the current context.
* **Included only**: shows all commands and environments from packages that are included in the document, but without context-awareness.
* **All**: shows all commands and environments from all installed packages (can be very slow), without context-awareness.


## Autocompletion of labels, references, citations and more

TeXiFy can autocomplete labels, references and citations.
When you type `\ref{}` or `\cite{}`, TeXiFy will suggest labels and citation keys that are defined in your project.

![label-autocompletion.png](label-autocompletion.png)

### Custom commands
_Since b0.11.3_

For custom commands that take labels, references or citations as parameters, TeXiFy will also suggest the appropriate keys.

![label-autocompletion-ctx1](label-autocompletion-ctx1.png)

![label-autocompletion-ctx2](label-autocompletion-ctx2.png)

![label-autocompletion-ctx3](label-autocompletion-ctx3.png)


## Inserting \item in itemize environments

When writing in an itemize-like environment, pressing <shortcut>Enter</shortcut> will automatically insert an `\item` on the next line.
This allows for easy writing of lists.


If your cursor is in the middle of a line and you want to split it, but without inserting an `\item` in the middle, use <shortcut>Ctrl + Enter</shortcut> to invoke the 'split line' action.
<shortcut>Shift + Enter</shortcut> is bound by default to the 'Start new line' action, which is different from the <shortcut>Enter</shortcut> action in that it will not put everything after the cursor on the new line.

An example which shows the use of <shortcut>Enter</shortcut> at the end of a line, <shortcut>Enter</shortcut> at the middle of a line, <shortcut>Shift + Enter</shortcut> and <shortcut>Ctrl + Enter</shortcut> (in that order):

![itemize-enter](itemize-enter.gif)

Note that for even quicker insertion of an itemize you can use live templates (`itm` for itemize by default) as described in [Live templates](#live-templates).
If you want to start a new line without an `\item`, but you do want your cursor to move there automatically (which the 'split line' action does not do), you can for example [record a macro](https://www.jetbrains.com/help/idea/using-macros-in-the-editor.html).
For example, you could use the 'Start new line' action, following by deleting the `\item`.
Then, in the Keymap you can bind a shortcut to the Macro.

## Brace matching

TeXiFy matches braces, inline math and more.
If you type the first one, the second one will be automatically inserted.
You can disable this in <ui-path>File | Settings | Editor | General | Smart Keys | Insert paired brackets</ui-path>.

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

A list of available templates is in <ui-path>File | Settings | Editor | General | Postfix Completion</ui-path>, where you can edit the key of a template. You can also add your own templates.
For more advanced editing of postfix templates, have a look at the [Custom Postfix Templates](https://plugins.jetbrains.com/plugin/9862-custom-postfix-templates) plugin. The plugin allows the creation of custom postfix templates for a number of languages, including LaTeX.

See also [https://www.jetbrains.com/help/idea/auto-completing-code.html#postfix_completion](https://www.jetbrains.com/help/idea/auto-completing-code.html#postfix_completion).

## Smart quote substitution

The <control>csquotes</control> package provides the `\enquote` command.
TeXiFy can automatically insert the command if you type regular quotes, see [Global Settings](TeXiFy-settings.md#csquotes).