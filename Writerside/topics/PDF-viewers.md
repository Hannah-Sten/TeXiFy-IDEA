# PDF viewers

## PDF Viewer IntelliJ plugin

_Since b0.7.4_

TeXiFy supports using the [PDF Viewer](https://plugins.jetbrains.com/plugin/14494-pdf-viewer) plugin to view PDFs inside the IDE.
This includes forward and inverse search functionality.

To forward search, use <ui-path>Tools | LaTeX | Forward search</ui-path> or the shortcut listed there.
To inverse search, use <shortcut>Ctrl + click</shortcut>.

To use the PDF Viewer plugin, install it via the plugin settings in your IDE.
In IntelliJ: <ui-path>Settings | Plugins | search in marketplace</ui-path>, and select it in your LaTeX run configuration as the PDF viewer.
After compilation it will open the PDF viewer on the right.

At the moment of writing (April 2023), a newer version with bug fixes is available at [https://github.com/slideclimb/intellij-pdf-viewer/releases](https://github.com/slideclimb/intellij-pdf-viewer/releases)

![built-in-pdf-viewer](built-in-pdf-viewer.gif)

---

Thanks to [Ivan Posti](https://github.com/FirstTimeInForever) from JetBrains for this plugin.

For any bugs, questions, or feature requests for the PDF viewer, please refer to the PDF Viewer’s [GitHub](https://github.com/FirstTimeInForever/intellij-pdf-viewer).

## SumatraPDF

On Windows, TeXiFy supports SumatraPDF as a pdf viewer with forward and inverse search.

It can be downloaded from [https://www.sumatrapdfreader.org/download-free-pdf-viewer.html](https://www.sumatrapdfreader.org/download-free-pdf-viewer.html).
If you know you are on a 64-bit system you can download the 64-bit build installer. If you have no idea, download the normal installer which is the top-most link.

Normally, the plugin will automatically detect the SumatraPDF executable if it is in your PATH or if you have installed it in one of the default locations.
If it does not, you can specify the path to the executable in the [TeXiFy settings](TeXiFy-settings.md#path-to-sumatrapdf-windows-only).

### Shortcuts {id="sumatra-shortcuts"}

The default shortcut for forward search in IntelliJ is <shortcut>Ctrl + Alt + Shift + .</shortcut>.
The default shortcut for backward search in SumatraPDF is a double left mouse click.

### Explanation

#### Forward search {id="sumatra-forward-search"}
When your cursor is in IntelliJ and you have just compiled a document, you can look up which line in the pdf corresponds to the line your cursor is at by going in IntelliJ to the menu <ui-path>Tools | LaTeX | SumatraPDF | Forward Search</ui-path>, or using the shortcut <shortcut>Ctrl + Alt + Shift + .</shortcut> which is listed there.
This shortcut can also be used to bring the SumatraPDF window in view when you recompiled a document but you do not see it.

#### Backward or inverse search {id="sumatra-inverse-search"}

You can also do the reverse of the forward search lookup.
To configure this, the first time you have to press <ui-path>Tools | LaTeX | SumatraPDF | Configure Inverse Search</ui-path>, this will update a setting in SumatraPDF.
You also have to do this every time after you update IntelliJ.

Make sure the installation directory of SumatraPDF is added to your PATH, and that you rebooted after adding it.
If it still does not work, you can run the following command in the directory containing SumatraPDF, replacing the path to your IntelliJ installation:

```
cmd.exe /C start SumatraPDF -inverse-search "\"C:\path\to\idea\bin\idea64.exe\" --line %l \"%f\""
```

Now you can double left-click in SumatraPDF in a pdf you just compiled, and it should make your cursor go to the correct location in IntelliJ.

### Portable SumatraPDF {id="sumatra-portable"}
_Since b0.6.6_

There also is support for portable SumatraPDF installations, in case you cannot install use the normal installer for example if you do not have administrator rights.
Open your [TeXiFy settings](TeXiFy-settings.md), find the setting "SumatraPDF executable path"
and specify in the text field the path to `SumatraPDF.exe`, either by editing the field or by clicking the folder icon on the right.


### "No synchronization info at this position" or "Synchronization file cannot be opened"
After compiling your file and after SumatraPDF opens up, if you get errors in SumatraPDF such as "No synchronization info at this position" or "Synchronization file cannot be opened", it might be an issue with SumatraPDF itself (see here : [https://github.com/sumatrapdfreader/sumatrapdf/discussions/2741,](https://github.com/sumatrapdfreader/sumatrapdf/discussions/2741,) https://github.com/sumatrapdfreader/sumatrapdf/issues/2642). The issue doesn’t seem to appear when closing the file in SumatraPDF, and then opening it without compiling. The issue seems to appear only after a compilation. A fix that seems to work is to install the portable version of SumatraPDF. Don’t forget to add "SumatraPDF.exe" to your PATH variables or in the registry editor (see above in Portable SumatraPDF section).

## Evince

_Since b0.6.6_

On non-Windows systems, TeXiFy supports Evince as a pdf viewer with forward and inverse search.

### Shortcuts {id="evince-shortcuts"}

The default shortcut for forward search in IntelliJ is <shortcut>Ctrl + Alt + Shift + .</shortcut>.
The default shortcut for backward search in Evince is <shortcut>Ctrl + Left mouse click</shortcut>.

Note that spaces in your path (including filename) are not allowed.

### Forward search {id="evince-forward-search"}
When your cursor is in IntelliJ and you have just compiled a document, you can look up which line in the pdf corresponds to the line your cursor is at by going in IntelliJ to the menu <ui-path>Tools | LaTeX | Evince | Forward Search</ui-path>, or using the shortcut <shortcut>Ctrl + Alt + Shift + .</shortcut> which is listed there.
This shortcut can also be used to bring the Evince window in view when you do not see it.

### Backward or inverse search {id="evince-inverse-search"}

You can also do the reverse: press <shortcut>Ctrl</shortcut> and click in Evince in a pdf you just compiled, and it should make your cursor go to the correct location in IntelliJ.

Note you need at least Evince 2.32 for this, you can check your version with `evince --version`. Especially on Linux Mint it may be that your version is too old, in which case simply `apt install evince` to update.

## Okular

_Since b0.6.7_

On Linux systems, TeXiFy supports Okular as a pdf viewer with forward and inverse search.

### Shortcuts {id="okular-shortcuts"}
The default shortcut for forward search in IntelliJ is is <shortcut>Ctrl + Alt + Shift + .</shortcut>.
The default shortcut for inverse search in Okular is <shortcut>Shift + Left mouse click</shortcut>.

### Configuring inverse (or backwards) search {id="okular-inverse-search"}
_Note that inverse search only works when you’re viewing the pdf in browse mode (<shortcut>Ctrl + 1</shortcut>)_

* In Okular, open the settings and go to the Editor tab.
* Specify the correct binary for your Jetbrains IDE
* General instructions:
  * custom Text editor and add `{bin} --line %l %f`
* For IDEA:
  *  For Okular version > 1.8.3, select TeXiFy-IDEA from the list. For Okular 1.8.3 or less, select the Custom Text Editor in the editor field, and type the command `idea --line %l %f`
* For Pycharm
  * `pycharm-community --line %l %f`
* If you have problems ensure that your binary is in the system path.  You can check with the `which {bin}` command, so for IDEA: `which idea` and for pycharm community edition `which pycharm-community`
* If the binary is missing from the system path you may need to add it e.g. via `echo export PATH="$PATH:/bin/pest" >> ~/.bashrc`.  Some users have needed to enable the "Generate shell scripts" settings in Jetbrains Toolbox; however I have had success with a snap installed pycharm (no Jetbrains Toolbox installed). For more information on the Jetbrains Toolbox see the [IntelliJ documentation](https://www.jetbrains.com/help/idea/opening-files-from-command-line.html).

See the [Okular documentation](https://docs.kde.org/stable5/en/kdegraphics/okular/inverse_search.html) for more information on inverse search with Okular.

## Zathura

_Since b0.7.1_

On non-Windows systems, TeXiFy supports [Zathura](https://pwmt.org/projects/zathura/) as a pdf viewer with forward and inverse search.

### Shortcuts {id="zathura-shortcuts"}

The default shortcut for forward search in IntelliJ is <shortcut>Ctrl + Alt + Shift + .</shortcut>.
The default shortcut for backward search in Zathura is <shortcut>Ctrl + Left mouse click</shortcut>.

Note that spaces in your path (including filename) are not allowed.

### Forward search {id="zathura-forward-search"}
When your cursor is in IntelliJ and you have just compiled a document, you can look up which line in the pdf corresponds to the line your cursor is at by going in IntelliJ to the menu <ui-path>Tools | LaTeX | Zathura | Forward Search</ui-path>, or using the shortcut <shortcut>Ctrl + Alt + Shift + .</shortcut> which is listed there.
This shortcut can also be used to bring the Zathura window in view when you do not see it.

### Backward or inverse search {id="zathura-inverse-search"}

You can also do the reverse: press <shortcut>Ctrl</shortcut> and click in Zathura in a pdf you just compiled, and it should make your cursor go to the correct location in IntelliJ.

## Skim

_Since b0.6.8_

On MacOS, TeXiFy supports Skim as a pdf viewer with forward and inverse search.

### Shortcuts {id="skim-shortcuts"}
The default shortcut for forward search in IntelliJ is is <shortcut>⌥ + ⇧ + ⌘ + .</shortcut>.
The default shortcut for inverse search in Skim is <shortcut>⌘ + ⇧ + Click</shortcut>.

### Configuring inverse (or backwards) search {id="skim-inverse-search"}

* In Skim, open the settings (<shortcut>⌘ + ,</shortcut>) and go to the Sync tab.
* Select the Custom as preset, and fill in `idea` as command and `--line %line %file` as arguments (for PyCharm replace `idea` with `pycharm`).
* Check that `idea` is available with `which idea`. If it isn’t, you might have to enable the "Generate shell scripts" settings in Jetbrains Toolbox, see the [IntelliJ documentation](https://www.jetbrains.com/help/idea/opening-files-from-command-line.html) for more information.

See the [Skim documentation](https://skim-app.sourceforge.io/manual/SkimHelp_51.html) for more information on inverse search with Skim.

## Other pdf viewers

If you have a different pdf viewer not in the list, you can still let TeXiFy automatically open it after compilation, see [Run configuration settings](Run-configuration-settings.md#custom-pdf-viewer).
