# Code completion

TeXiFy automatically completes many structures as you type.
Some examples are:

* Automatic insertion of `\end` when typing `\begin` for environments
* On typing `\[` automatic insertion of `\]`, after kbd:[Enter] also with correct indentation
* Automatic insertion of braces of first required parameter
* Typing `}` at closing brace skips over it, same for `$..$`

## Inserting \item in itemize environments

When writing in an itemize-like environment, pressing kbd:[Enter] will automatically insert an `\item` on the next line.
This allows for easy writing of lists.

If you are writing an item in the list but you do want a linebreak, for example to start a new sentence, use kbd:[Shift + Enter].

If your cursor is in the middle of a line and you want to split it, but without inserting an `\item` in the middle, use kbd:[Ctrl + Enter].

An example which shows the use of kbd:[Enter] at the end of a line, kbd:[Enter] at the middle of a line, kbd:[Shift + Enter] and kbd:[Ctrl + Enter] (in that order):

![itemize-enter](itemize-enter.gif)

Note that for even quicker insertion of an itemize you can use live templates (`itm` for itemize by default) as described in [Live templates](Live-templates).

## Brace matching

TeXiFy matches braces, inline math and more.
If you type the first one, the second one will be automatically inserted.
If you then continue typing, you can exit the brace pair by typing the closing brace or dollar sign, or you can use tab if you have <ui-path>File | Settings | Editor | General | Smart Keys | "Jump outside closing bracket with Tab when typing"</ui-path> enabled.

![brace-matching](brace-matching.png)

![brace-matching2](brace-matching2.png)

## Live templates

Using live templates, you can quickly insert a predefined piece of text by typing just a few characters of a certain key.
You can denote places to which the cursor skips when you press kbd:[Tab] after inserting the live template.

To use a live template, type (a part of) the key, for example `fig`, hit enter when the live template is suggested in the autocomplete, type things and use kbd:[Tab] to skip to the next place to type information.

![live-templates](live-templates.gif)

Currently implemented by default are live templates for:

* figures, tables, itemize, enumerate, and in math for summations and integrals;
* sectioning with automatic label (triggered with `\partl`, `\chapl`, `\secl`, etc.), _since b0.7.3_.

You can find these live templates, as well as add your own, under <ui-path>File | Settings | Editor | Live Templates | LaTeX</ui-path>. _Since b0.7.4:_ the default live templates are disabled in verbatim contexts.

![live-template-settings](live-template-settings.png)

For more information, see [https://www.jetbrains.com/help/idea/creating-and-editing-live-templates.html](https://www.jetbrains.com/help/idea/creating-and-editing-live-templates.html)
