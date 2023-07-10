_Since b0.6.8_

<ui-path>File | Settings | Languages & Frameworks | TeXiFy</ui-path>

TeXiFy supports automatic compilation, which means that when you type in your document, it will automatically be compiled.
In general, we advise against using this because once you are somewhat familiar with LaTeX there is no need to compile all the time to check if what you typed is correct, and compiling so much will have a serious impact on your CPU usage which can slow other things (including IntelliJ itself) down considerably.

When you start typing in a document, the run configuration that is selected in the dropdown menu at the top is run, so make sure you select the right one first.
Recall that you can create a run config with kbd:[Ctrl + Shift + F10].

When you use Evince or Okular, you need to compile manually (so using the button next to the run configuration or using kbd:[Shift + F10]) once before you can use forward search.
This is because you need to tell TeXiFy where the pdf is that you want to forward search to, which depends on the run configuration.
Since these pdf viewers will focus when you forward search, this is not done while you are typing.
This is not the case for SumatraPDF, which can forward search without losing focus.

Currently the automatic compilation is only triggered when you type in a document or use backspace, not yet by other ways you edit the document.

![autocompile](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Running/figures/autocompile.gif)

## Automatic compilation only when the document is saved.
You can enable this in TeXiFy settings.
When enabled, auto compilation will only run when the file is actually saved to disk, instead of after every change.
To configure when a file is saved to disk (for example after a certain idle time), go to <ui-path>Settings | Appearance | System Settings</ui-path>.
Also see [https://www.jetbrains.com/help/idea/saving-and-reverting-changes.html](https://www.jetbrains.com/help/idea/saving-and-reverting-changes.html)

## Automatic compilation support by compilers

Some compilers, at least latexmk and tectonic, also support automatic compilation.
It depends on your preferences whether this will work better than the autocompile in TeXiFy or not.
See [Compilers](Compilers) for more information.