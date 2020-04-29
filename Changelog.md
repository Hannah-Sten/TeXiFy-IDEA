# Beta 0.6.10

# Additions
* Add support for the xcolor package. (#1348)
* Align & and \\ in simple tables. (#1341)
* Add support for language injection. (#1363)
* Add support for custom preamble for math and tikz preview. (#1373)
* Add commands defined using the xparse package to the autocompletion. (#1312)
* Add support for Dockerized MiKTeX. (#1310)
* Add support for entering relative paths in the run configuration. (#1311)
* Add magic comments to disable and enable the parser. (#1388)
* Add option to include backslash in word selection. (#1316)
* Do not auto-insert a second $ when the first $ closes an inline math environment. (#1323)
* Improve performance of the inclusion loop inspection. (#1327)
* Add postfix templates. (#1326, #1350)
* Add inspection to check for correct filename/filepath in ProvidesPackage commands. (#1365)
* Add inspection to check if # is escaped. (#1366)

# Bug fixes
* Fix parse error on unmatched brackets in math environments. (#1319)
* Fix parse error for \@ifnextchar. (#1320)
* Fix parse error when inline math inside \text inside inline math is used. (#1322)
* Fix parse error when dollar signs are used in table preamble. (#1324)
* Fix parse error for \verb|...|, \verb=...=, \verb"..." and \verb!...!. (#1344)
* Fix parse errors in verbatim-like environments. (#1353, #1382)
* Fix parse error on \newenvironment definitions. (#1340)
* Fix parse error on non-ascii characters in bibtex identifiers. (#1367)
* Avoid formatter confusion when ending a line with a backslash. (#1342)
* Fix autocompletion in custom command parameters. (#1360)
* Fixed crashes. (#1332, #1337, #1372)

Thanks to Boris Dudelsack ([@bdudelsack](https://github.com/bdudelsack)) and Nhan Thai ([@dandoh](https://github.com/dandoh)) for contributing to this release.

A detailed overview of the changes can be found on the [milestone page](https://github.com/Hannah-Sten/TeXiFy-IDEA/milestone/19?closed=1).

