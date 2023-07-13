_Since b0.6.6_

On non-Windows systems, TeXiFy supports Evince as a pdf viewer with forward and inverse search.

## Shortcuts

The default shortcut for forward search in IntelliJ is <shortcut>Ctrl + Alt + Shift + .</shortcut>.
The default shortcut for backward search in Evince is <shortcut>Ctrl + Left mouse click</shortcut>.

## Explanation

Note that spaces in your path (including filename) are not allowed.

### Forward search
When your cursor is in IntelliJ and you have just compiled a document, you can look up which line in the pdf corresponds to the line your cursor is at by going in IntelliJ to the menu <ui-path>Tools | LaTeX | Evince | Forward Search</ui-path>, or using the shortcut <shortcut>Ctrl + Alt + Shift + .</shortcut> which is listed there.
This shortcut can also be used to bring the Evince window in view when you do not see it.

### Backward or inverse search

You can also do the reverse: press <shortcut>Ctrl</shortcut> and click in Evince in a pdf you just compiled, and it should make your cursor go to the correct location in IntelliJ.

Note you need at least Evince 2.32 for this, you can check your version with `evince --version`. Especially on Linux Mint it may be that your version is too old, in which case simply `apt install evince` to update.
