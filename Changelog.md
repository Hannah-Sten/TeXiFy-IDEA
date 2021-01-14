# Beta 0.7.3

ETA: First half of February, 2021

# Additions
* Added a wizard to insert graphics. (#1729)
* Paste html tables from the clipboard directly into LaTeX tables using a table insert wizard. (#1738)
* Paste images from the clipboard directly into LaTeX using the insert graphic wizard. (#1739)
* Added support for labels defined in optional parameters of commands. (#1698)
* Added Detexify tool window. (#1731)
* Added BibTeX unused entry inspection. (#1717)
* Added label intention for \item commands. (#1719)
* Add inspection to encourage replacing \text{min} by \min for a lot of similar math operators. (#1737)

# Changes
* Decreased size of gutter icons to 12x12. (#1715)
* Dragging and dropping graphic files opens a graphic insertion wizard. (#1729)
* Adding labels with no reasonable defaults now starts a refactoring. (#1733)
* Supported lstlisting for add label quickfix. (#1733)
* Performance improvements. (#1716)
* Picture arguments are now considered as command arguments in the formatter. (#1712, #1741)

# Bug fixes
* Fixed compile gutter icons showing up on parameters. (#1734)
* Fixed crashes. (#1720, #1721)

Thanks to Felix Berlakovich ([@fberlakovich](https://github.com/fberlakovich)) for contributing to this release.
