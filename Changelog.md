# Beta 0.7.4

# Additions
* Added support for opening an internal pdf viewer using the IntelliJ PDF Viewer plugin. (#1675)
* Added commands from all installed LaTeX packages to autocompletion. (#1710)
* Added symbol tool window. (#1752)

# Changes
* Changed Grazie rules to ignore non-text elements in a sentence. (#1744)
* Implemented a file based index for commands and environments. (#1710)

# Bug fixes
* Fixed plugin icon not showing up in the marketplace. (#1759)
* Fixed bug in inline math highlighting. (#1744)


# Beta 0.7.3

# Additions
* Added a wizard to insert graphics. (#1729)
* Paste html tables from the clipboard directly into LaTeX tables using a table insert wizard. (#1738)
* Paste images from the clipboard directly into LaTeX using the insert graphic wizard. (#1739)
* Added support for labels defined in optional parameters of commands. (#1698, #1751)
* Added Detexify tool window. (#1731)
* Added BibTeX unused entry inspection. (#1717)
* Added label intention for \item commands. (#1719)
* Added vertically centered colon inspection. (#1743)
* Added inspection to encourage replacing \text{min} by \min for a lot of similar math operators. (#1737)
* Added sectioning commands with label live template. (#1746)
* Added support for custom output directories to the 'Delete generated files' action. (#1726, #1745)
* Improve performance of file set cache creation for large projects. (#1749)

# Changes
* Decreased size of gutter icons to 12x12. (#1715)
* Dragging and dropping graphic files opens a graphic insertion wizard. (#1729)
* Adding labels with no reasonable defaults now starts a refactoring. (#1733)
* Supported lstlisting for add label quickfix. (#1733)
* Performance improvements. (#1716)
* Picture arguments are now considered as command arguments in the formatter. (#1712, #1741)
* Don't collapse citations when optional parameters are not the same. (#1732)

# Bug fixes
* Fixed compile gutter icons showing up on parameters. (#1734)
* Fix parse errors for \NewDocumentEnvironment-like commands and \newenvironment. (#1754)
* Fixed a bug with run configurations not being copied correctly. (#1728, #1745)
* Fixed overriding backspace handlers in non-LaTeX files. (#1740, #1745)
* Ignore \& when counting & for aligning tables. (#1754)
* Fixed crashes. (#1720, #1721, #1754)

Thanks to Felix Berlakovich ([@fberlakovich](https://github.com/fberlakovich)) for contributing to this release.
