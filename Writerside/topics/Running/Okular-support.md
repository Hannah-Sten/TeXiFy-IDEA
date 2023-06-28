_Since b0.6.7_

On Linux systems, TeXiFy supports Okular as a pdf viewer with forward and inverse search.

## Shortcuts
The default shortcut for forward search in IntelliJ is is kbd:[Ctrl + Alt + Shift + .].
The default shortcut for inverse search in Okular is kbd:[Shift + Left mouse click].

## Configuring inverse (or backwards) search
_Note that inverse search only works when you’re viewing the pdf in browse mode (kbd:[Ctrl + 1])_

* In Okular, open the settings and go to the Editor tab.
* For Okular version > 1.8.3, select TeXiFy-IDEA from the list. For Okular 1.8.3 or less, select the Custom Text Editor in the editor field, and type the command `idea --line %l %f` (for PyCharm replace `idea` with `pycharm`).
* Check that `idea` is available with `which idea`. If it isn’t, you might have to enable the "Generate shell scripts" settings in Jetbrains Toolbox, see the [IntelliJ documentation](https://www.jetbrains.com/help/idea/opening-files-from-command-line.html) for more information.

See the [Okular documentation](https://docs.kde.org/stable5/en/kdegraphics/okular/inverse_search.html) for more information on inverse search with Okular.
