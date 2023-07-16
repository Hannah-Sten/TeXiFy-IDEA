On Windows, TeXiFy supports SumatraPDF as a pdf viewer with forward and inverse search.

It can be downloaded from [https://www.sumatrapdfreader.org/download-free-pdf-viewer.html](https://www.sumatrapdfreader.org/download-free-pdf-viewer.html).
If you know you are on a 64-bit system you can download the 64-bit build installer. If you have no idea, download the normal installer which is the top-most link.

## Shortcuts

The default shortcut for forward search in IntelliJ is <shortcut>Ctrl + Alt + Shift + .</shortcut>.
The default shortcut for backward search in SumatraPDF is a double left mouse click.

## Explanation

### Forward search
When your cursor is in IntelliJ and you have just compiled a document, you can look up which line in the pdf corresponds to the line your cursor is at by going in IntelliJ to the menu <ui-path>Tools | LaTeX | SumatraPDF | Forward Search</ui-path>, or using the shortcut <shortcut>Ctrl + Alt + Shift + .</shortcut> which is listed there.
This shortcut can also be used to bring the SumatraPDF window in view when you recompiled a document but you do not see it.

### Backward or inverse search

You can also do the reverse of the forward search lookup.
To configure this, the first time you have to press <ui-path>Tools | LaTeX | SumatraPDF | Configure Inverse Search</ui-path>, this will update a setting in SumatraPDF.
You also have to do this every time after you update IntelliJ.

Now you can double left-click in SumatraPDF in a pdf you just compiled, and it should make your cursor go to the correct location in IntelliJ.

## Portable SumatraPDF
_Since b0.6.6_

There also is support for portable SumatraPDF installations, in case you cannot install use the normal installer for example if you do not have administrator rights.
Open your [run configuration](Running-a-LaTeX-file.md), click Select custom SumatraPDF path and specify in the text field the folder which contains `SumatraPDF.exe`, either by editing the field or by clicking the folder icon on the right.
When you run the run configuration, SumatraPDF should open.

Note that to use forward and inverse search, the folder containing `SumatraPDF.exe` should be added to your PATH, or the registry key `HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\SumatraPDF.exe` should be set to the path to `SumatraPDF.exe`.

## "No synchronization info at this position" or "Synchronization file cannot be opened"
After compiling your file and after SumatraPDF opens up, if you get errors in SumatraPDF such as "No synchronization info at this position" or "Synchronization file cannot be opened", it might be an issue with SumatraPDF itself (see here : [https://github.com/sumatrapdfreader/sumatrapdf/discussions/2741,](https://github.com/sumatrapdfreader/sumatrapdf/discussions/2741,) https://github.com/sumatrapdfreader/sumatrapdf/issues/2642). The issue doesn’t seem to appear when closing the file in SumatraPDF, and then opening it without compiling. The issue seems to appear only after a compilation. A fix that seems to work is to install the portable version of SumatraPDF. Don’t forget to add "SumatraPDF.exe" to your PATH variables or in the registry editor (see above in Portable SumatraPDF section).
