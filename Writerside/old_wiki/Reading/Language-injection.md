_Since b0.6.10_

When your cursor is in a `verbatim`-like environment, by using <shortcut>Alt + Enter</shortcut> you can use the intention "Permanently inject language in environment" to insert a magic comment `%! language = languageid` before the environment, where `languageid` corresponds to the language you chose in the intention pop-up.
In principle, any language in IntelliJ is available, including those provided by plugins (including TeXiFy).

When you use the `lstlisting` environment with the `language` option, or you use the `\newminted` command to define an environment, if the value is a language id known by IntelliJ then the language will automatically be injected.
It can still be overridden using a magic comment.
For more information about this option, see the listings documentation at [http://mirrors.ctan.org/macros/latex/contrib/listings/listings.pdf](http://mirrors.ctan.org/macros/latex/contrib/listings/listings.pdf)
The same goes for commands for which the language is known, for example for `\directlua` TeXiFy will automatically inject Lua (if you have the plugin installed).

When you want to edit the code in the listing, it is recommended to use <shortcut>Alt + Enter</shortcut> to open it in a separate window, and edit there.

For more information, see [https://www.jetbrains.com/help/idea/using-language-injections.html](https://www.jetbrains.com/help/idea/using-language-injections.html)

![language-injection](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/reading/language-injection.png)
