# ?

# Additions
* Add support for custom labeling and referencing commands. (#1442)

# Changes

# Bug fixes
* Other small improvements and bug fixes (#1436, #1448)

# Beta 0.6.10

# Additions
* Added support for the xcolor package. (#1348, #1396)
* Added support for language injection. (#1363)
* Added support for custom preamble for math and tikz preview. (#1373)
* Added basic algorithmicx pseudocode formatting. (#1393)
* Added commands defined using the xparse package to the autocompletion. (#1312)
* Added support for Dockerized MiKTeX. (#1310)
* Added support for TeX Live from WSL. (#1410)
* Added support for entering relative paths in the run configuration. (#1311)
* Added magic comments to disable and enable the parser. (#1388)
* Added magic comments to choose LaTeX and BibTeX compiler. (#1409)
* Added support for inkscape 1.0 (#1398)
* Added option to include backslash in word selection. (#1316)
* Added postfix templates. (#1326, #1350)
* Added an inspection to check for correct filename/filepath in ProvidesPackage commands. (#1365)
* Added an inspection to check if # is escaped. (#1366)
* Added inspections to check if & and _ are escaped. (#1368, #1411)
* Added many matrix environments to the autocomplete. (#1431)

# Changes
* Major performance increase on Windows. (#1424, #1430)
* Improved performance of the inclusion loop inspection. (#1327)
* & and \\ get aligned in simple tables. (#1341)
* A second $ does no longer get auto-inserted when the first $ closes an inline math environment. (#1323)
* bibtex working directory is now configurable via UI. (#1413)

# Bug fixes
* Fixed parse error on unmatched brackets in math environments. (#1319, #1421)
* Fixed parse error for \@ifnextchar. (#1320)
* Fixed parse error when inline math inside \text inside inline math is used. (#1322)
* Fixed parse error when dollar signs are used in table preamble. (#1324)
* Fixed parse error for \verb|...|, \verb=...=, \verb"..." and \verb!...!. (#1344)
* Fixed parse errors in verbatim-like environments. (#1353, #1382)
* Fixed parse error on \newenvironment definitions. (#1340)
* Fixed parse error on non-ascii characters in bibtex identifiers. (#1367)
* Avoid formatter confusion when ending a line with a backslash. (#1342)
* Fixed autocompletion in custom command parameters. (#1360)
* Fixed some inspections being triggered incorrectly in comments. (#1426)
* Fixed EscapeAmpersand inspection triggering in some matrices. (#1427, #1431)
* Fixed not being able to type '\"' when using smart quotes. (#1425)
* Fixed User Access Control screen popping up on Windows. (#1424, #1430)
* Fixed crashes. (#1332, #1337, #1372, #1425)

Thanks to Boris Dudelsack ([@bdudelsack](https://github.com/bdudelsack)), Nhan Thai ([@dandoh](https://github.com/dandoh)) and Johannes Berger ([@Xaaris](https://github.com/xaaris)) for contributing to this release.

A detailed overview of the changes can be found on the [milestone page](https://github.com/Hannah-Sten/TeXiFy-IDEA/milestone/19?closed=1).

