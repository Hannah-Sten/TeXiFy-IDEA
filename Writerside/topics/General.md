# General

<tldr>
<p>
<ui-path>File | Settings | Editor General</ui-path>
</p>
</tldr>

## Code folding

Using the gutter icons (to the left of your open file), the menu (<ui-path>Code | Folding</ui-path>) or context menu (right-click), or the shortcuts (see the menu or the keymap), you can fold and unfold regions of text.

You can for example fold sections, subsections, greek letters, etc.
Note you can easily fold or collapse until a certain level using the menu or shortcuts.

Which elements are folded by default can be configured in <ui-path>File | Settings | Editor | General | Code Folding | LaTeX</ui-path>.

![folding](folding.png)

For more information, see [https://www.jetbrains.com/help/idea/code-folding-settings.html](https://www.jetbrains.com/help/idea/code-folding-settings.html).

### Unicode math preview

TeXiFy can show a unicode preview of some math characters like greek letters.
You can trigger this with your cursor on a math command and clicking <ui-path>Code | Folding | Collapse</ui-path> or using <shortcut>Ctrl + NumPad -</shortcut>.
Clicking on it will expand it again.
TeXiFy shows these previews automatically when a project is opened.

## Line markers

<ui-path>File | Settings | Editor | General | Appearance | Show method separators</ui-path>

When enabled, horizontal line markers will be shown above sectioning commands.

![line-markers](line-markers.png)

## Gutter icons

### Color preview

_Since b0.6.10_

A color preview will be shown in the gutter when using an xcolor command like `\color` or `\textcolor`, and when defining a color with xcolor.
This also supports the xcolor syntax to mix colors, like `red!50!yellow`.
When you use the color picker on a color _definition_, choosing a different color will update the color definition in the document.

![color-gutter](color-gutter.png)

### Navigate to referenced file

If you include a file, for example like `\input{beta.tex}`, there will be a gutter icon in front of the line which you can click to navigate to the included file (<shortcut>Ctrl + B</shortcut>).

### Run file

Next to any `\begin{document}`, there will be a gutter icon which you can press to compile the file (<shortcut>Ctrl + Shift + F10</shortcut>).