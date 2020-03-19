# Beta 0.6.10

# Additions
* Add commands defined using the xparse package to the autocompletion. (#1312)
* Add support for Dockerized MiKTeX. (#1310)
* Add support for entering relative paths in the run configuration. (#1311)

A detailed overview of the changes can be found on the [milestone page](https://github.com/Hannah-Sten/TeXiFy-IDEA/milestone/19?closed=1).

# Beta 0.6.9

# Additions
* Add go to definition for labels, citations and new commands. (#1191, #2)
* Add find usages action for labels, citations and new commands. (#1237, #1252)
* Add support for refactor (rename) of labels, citations, environments and files. (#1255, #1264, #1266, #1275, #1284)
* Add support for opening source files of installed LaTeX packages and classes. (#1191)
* Add support for opening files from include commands. (#1191)
* Many more improvements to file reference resolving. (#1281)
* Add support for a custom output or auxiliary directory. (#1201)
* Add support for environment variables in the LaTeX run configuration. (#1289).
* Add biblatex commands to the autocomplete. (#1195)
* Add support for labels defined with Verbatim or lstlisting environments. (#1232)
* Add support for \graphicspath from the graphicx package. (#1224, #1295)
* Add support for using \input with absolute paths. (#1244)
* Add a warning to update Evince when the version is too old for backwards/forward search. (#1226)
* Add more user configurable live templates. (#1203)
* Surround text with $..$ or [..] using a shortcut or by typing $ or [. (#1207)
* Surround text with quotes depending on your Smart quotes setting. (#1207)
* Add inspection which checks that floating environments have a label. (#1216)
* Add support for the import package to include files. (#1281)
* Add inspection to check if an included package could not be found in the CTAN list. (#1230)
* Add inspection to check if a TeX Live package is installed locally, with a quickfix to install it. (#1276, #1291)
* Use live templates for inserting braces for all required parameters of commands and environments. (#1258, #1261)
* Make links in \url and \href commands clickable. (#1238)
* Remove second $ of an empty inline math environment when deleting the first one. (#1227)
* Add chapterbib support. (#1223)
* Add biblatex entry types to the autocomplete. (#1270)
* Add support for referencing local pdf files in a 'file' bibtex field. (#1300)
* Use Ctrl+Enter to split lines in an itemize. (#1228)
* Disable the formatter on files that only contain a verbatim-like environment. (#1225)
* Add inspection plus quickfix to insert formatter magic comments or move verbatim-like environments to a separate file. (#1225)
* Add support for commands with the same name from different packages. (#1262, #1285)
* Add inspection to check that the label comes after the caption. (#1235)
* Other performance improvements. (#1277)

# Changes
* If a file has a run configuration associated, treat it as a root file. (#1198, #1298)
* Improve performance of line markers. (#1200)
* Ignore \ifoot for \if-like commands. (#1220)
* Also execute Grazie grammar checking at the beginning of sentences. (#1196)
* Always show LaTeX Tools menu to avoid performance problems. (#1257)
* Improve insertion of missing usepackage command. (#1286)

# Bug fixes
* Fixed crashes. (#1211, #1269, #1292, #1306, #1307)

Thanks to Niko Strijbol [(@niknetniko)](https://github.com/niknetniko), Felix Berlakovich [(@fberlakovich)](https://github.com/fberlakovich) and [@Lukas-Heiligenbrunner](https://github.com/Lukas-Heiligenbrunner) for contributing to this release.

A detailed overview of the changes can be found on the [milestone page](https://github.com/Hannah-Sten/TeXiFy-IDEA/milestone/18?closed=1).
