# Beta 0.6.10

# Additions
* Add commands defined using the xparse package to the autocompletion. (#1312)
* Add support for Dockerized MiKTeX. (#1310)
* Add support for entering relative paths in the run configuration. (#1311)
* Add option to include backslash in word selection. (#1316)
* Do not auto-insert a second $ when the first $ closes an inline math environment. (#1323)
* Improve performance of the inclusion loop inspection. (#1327)
* Add postfix templates. (#1326, #1350)

# Bug fixes
* Fix parse error on unmatched brackets in math environments. (#1319)
* Fix parse error for \@ifnextchar. (#1320)
* Fix parse error when inline math inside \text inside inline math is used. (#1322)
* Fix parse error when dollar signs are used in table preamble. (#1324)
* Fix parse error for \verb|...|, \verb=...=, \verb"..." and \verb!...!. (#1344)
* Avoid formatter confusion when ending a line with a backslash. (#1342)
* Fixed crashes. (#1332, #1337)

Thanks to Boris Dudelsack ([@bdudelsack](https://github.com/bdudelsack)) for contributing to this release.

A detailed overview of the changes can be found on the [milestone page](https://github.com/Hannah-Sten/TeXiFy-IDEA/milestone/19?closed=1).

