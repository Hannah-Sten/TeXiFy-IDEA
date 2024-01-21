# Contributing to the documentation

This documentation project uses Writerside.
If you want to edit it, you can click the <control>Edit page</control> button.

![Edit page](edit-page.png)

Then, you will be taken to GitHub where you can edit the page.
Once you are done, click <control>Commit Changes</control> and create a pull request.

The Writerside documentation can be found at [Writerside | JetBrains Marketplace](https://plugins.jetbrains.com/plugin/20158-writerside/docs).

## Writerside syntax overview
Writerside syntax is based on Markdown, but with some extra features.

* Display keyboard shortcuts:

<!-- ```markdown -->
```
<shortcut>Ctrl + \\</shortcut>
```

results in <shortcut>Ctrl + \\</shortcut>

* Display menu selections:

<!-- ```markdown -->
```
<ui-path>File | New | LaTeX File</ui-path>
```

results in <ui-path>File | New | LaTeX File</ui-path>

* Reference elements like [headings on the same page](#writerside-syntax-overview):

<!-- ```markdown -->
```
[Refer to](#my-heading)

## My Heading
```

* Reference [other pages](Contributing-to-the-source-code.md):

<!-- ```markdown -->
```
[Other page](Contributing-to-the-source-code.md)
```

* To include images/gifs, put them in the `images` directory, possibly in a subdirectory, and use

<!-- ```markdown -->
```
![My Image](my-image.png)
```
