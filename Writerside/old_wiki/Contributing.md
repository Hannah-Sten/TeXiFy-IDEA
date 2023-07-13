This wiki uses AsciiDoc.
You can clone this wiki and edit in IntelliJ using the AsciiDoc plugin.

## AsciiDoc guidelines/tips/cheatsheet
* Display keyboard shortcuts:

```asciidoc
<shortcut>Ctrl + \</shortcut>]
```

results in <shortcut>Ctrl + \</shortcut>]
which requires adding the `:experimental:` attribute at the top of the file

* Display menu selections:

```asciidoc
<ui-path>File | New | LaTeX File</ui-path>
```

results in <ui-path>File | New | LaTeX File</ui-path>

* Pages with dashes in the filename will appear with spaces on the wiki, so include them with:

```asciidoc
link:Page-name[Link text]
```

* To include images/gifs, put it in a `figures/` subdirectory, and link to it with

```asciidoc
image::https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/path/to/figure.gif[]
```

## Useful AsciiDoc links

GitHub uses AsciiDoctor to render `.asciidoc` files.

https://asciidoctor.org/docs/asciidoc-syntax-quick-reference/

https://asciidoctor.org/docs/user-manual/
