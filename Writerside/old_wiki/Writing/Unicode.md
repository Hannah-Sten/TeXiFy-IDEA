IntelliJ supports Unicode, see for example [https://blog.jetbrains.com/idea/2013/03/use-the-utf-8-luke-file-encodings-in-intellij-idea/](https://blog.jetbrains.com/idea/2013/03/use-the-utf-8-luke-file-encodings-in-intellij-idea/)

Note that if the LaTeX log output contains characters in an incorrect encoding on Windows, you can fix this by going to <ui-path>Help | Edit Custom VM Options</ui-path> and add `-Dfile.encoding=UTF-8`, then restart your IDE.

Also see the [Unicode inspection](Probable-bugs#Unsupported-Unicode-character).
