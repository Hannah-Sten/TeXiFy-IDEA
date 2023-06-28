_Since b0.6.8_

On MacOS, TeXiFy supports Skim as a pdf viewer with forward and inverse search.

## Shortcuts
The default shortcut for forward search in IntelliJ is is kbd:[⌥ + ⇧ + ⌘ + .].
The default shortcut for inverse search in Skim is kbd:[⌘ + ⇧ + Click].

## Configuring inverse (or backwards) search

* In Skim, open the settings (kbd:[⌘ + ,]) and go to the Sync tab.
* Select the Custom as preset, and fill in `idea` as command and `--line %line %file` as arguments (for PyCharm replace `idea` with `charm`).
* Check that `idea` is available with `which idea`. If it isn’t, you might have to enable the "Generate shell scripts" settings in Jetbrains Toolbox, see the [IntelliJ documentation](https://www.jetbrains.com/help/idea/opening-files-from-command-line.html) for more information.

See the [Skim documentation](https://skim-app.sourceforge.io/manual/SkimHelp_51.html) for more information on inverse search with Skim.
