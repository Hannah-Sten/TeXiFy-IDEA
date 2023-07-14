# Editor

<tldr>
<p>
<ui-path>File | Settings | Editor</ui-path>
</p>
<p>
<shortcut>Ctrl + Alt + S</shortcut>
</p>
</tldr>

On this page, we only highlight the settings that are specific to LaTeX and BibTeX.
For all available settings, see [https://www.jetbrains.com/help/idea/settings-editor.html](https://www.jetbrains.com/help/idea/settings-editor.html).

## General

See [General](General.md).

## Color scheme

Here you can edit the syntax highlighting colours of both LaTeX and BibTeX, for example for commands, math and references.

## Language injections

_Since b0.6.10_

When your cursor is in a `verbatim`-like environment, by using <shortcut>Alt + Enter</shortcut> you can use the intention "Permanently inject language in environment" to insert a magic comment `%! language = languageid` before the environment, where `languageid` corresponds to the language you chose in the intention pop-up.
In principle, any language in IntelliJ is available, including those provided by plugins (including TeXiFy).

When you use the `lstlisting` environment with the `language` option, or you use the `\newminted` command to define an environment, if the value is a language id known by IntelliJ then the language will automatically be injected.
It can still be overridden using a magic comment.
For more information about this option, see the listings documentation at [http://mirrors.ctan.org/macros/latex/contrib/listings/listings.pdf](http://mirrors.ctan.org/macros/latex/contrib/listings/listings.pdf)
The same goes for commands for which the language is known, for example for `\directlua` TeXiFy will automatically inject Lua (if you have the plugin installed).

When you want to edit the code in the listing, it is recommended to use <shortcut>Alt + Enter</shortcut> to open it in a separate window, and edit there.

For more information, see [https://www.jetbrains.com/help/idea/using-language-injections.html](https://www.jetbrains.com/help/idea/using-language-injections.html)

![language-injection](language-injection.png)

## TODO comments
<ui-path>Settings | Editor | TODO comments</ui-path>

TODO comments are special comments that have highlighting, appear in the errors/warnings overview, and have their own TODO tool window with a project-wide overview of all todo comments.
See [https://www.jetbrains.com/help/idea/using-todo.html](https://www.jetbrains.com/help/idea/using-todo.html).

