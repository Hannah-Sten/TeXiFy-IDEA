_Since b0.7.1_

On non-Windows systems, TeXiFy supports [Zathura](https://pwmt.org/projects/zathura/) as a pdf viewer with forward and inverse search.

## Shortcuts

The default shortcut for forward search in IntelliJ is kbd:[Ctrl + Alt + Shift + .].
The default shortcut for backward search in Zathura is kbd:[Ctrl + Left mouse click].

## Explanation

Note that spaces in your path (including filename) are not allowed.

### Forward search
When your cursor is in IntelliJ and you have just compiled a document, you can look up which line in the pdf corresponds to the line your cursor is at by going in IntelliJ to the menu <ui-path>Tools | LaTeX | Zathura | Forward Search</ui-path>, or using the shortcut kbd:[Ctrl + Alt + Shift + .] which is listed there.
This shortcut can also be used to bring the Zathura window in view when you do not see it.

### Backward or inverse search

You can also do the reverse: press kbd:[Ctrl] and click in Zathura in a pdf you just compiled, and it should make your cursor go to the correct location in IntelliJ.
