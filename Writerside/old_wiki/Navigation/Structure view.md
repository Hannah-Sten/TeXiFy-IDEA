<ui-path>View | Tool Windows | Structure</ui-path> or kbd:[Alt + 7]

The structure view shows all includes, sectioning commands (including proper nesting when chapter, section, subsection etc. are used), command definitions, labels and bibliography items (in `.bib` files).
You can show/hide any of these types in the Structure View at the top.
When you click on an item, it will autoscroll to source by default. You can also autoscroll from source, configurable in the Structure View window.
You can also sort alphabetically.

Note that to use `\chapter` in your document you need to use `\documentclass{book}`, so they will only appear in the structure view if you do have the `book` documentclass.

When using `\newcommand` or variants, we recommend to use braces like `\newcommand{\mycommand}{42}` so it will appear correctly in the structure view.

For more information about the structure view, see [https://www.jetbrains.com/help/idea/structure-tool-window-file-structure-popup.html](https://www.jetbrains.com/help/idea/structure-tool-window-file-structure-popup.html)
