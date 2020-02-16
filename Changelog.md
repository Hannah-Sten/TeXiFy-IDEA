# Beta 0.6.9

# Additions
* Add go to definition for labels, citations and new commands. (#1191, #2)
* Add find usages action for new commands. (#1237)
* Add support for opening source files of installed LaTeX packages and classes. (#1191)
* Add support for opening files from include commands. (#1191)
* Add support for a custom output or auxiliary directory. (#1201)
* Add biblatex commands to the autocomplete. (#1195)
* Add support for labels defined with Verbatim or lstlisting environments. (#1232)
* Add support for \graphicspath from the graphicx package. (#1224)
* Add support for using \input with absolute paths. (#1244)
* Add a warning to update Evince when the version is too old for backwards/forward search. (#1226)
* Add more user configurable live templates. (#1203)
* Surround text with $..$ or [..] using a shortcut or by typing $ or [. (#1207)
* Surround text with quotes depending on your Smart quotes setting. (#1207)
* Add inspection which checks that floating environments have a label. (#1216)
* Add inspection to check if an included package could not be found. (#1230)
* Make links in \url and \href commands clickable. (#1238)
* Remove second $ of an empty inline math environment when deleting the first one. (#1227)
* Add chapterbib support. (#1223)
* Use Ctrl+Enter to split lines in an itemize. (#1228)
* Disable the formatter on files that only contain a verbatim-like environment. (#1225)
* Add inspection plus quickfix to insert formatter magic comments or move verbatim-like environments to a separate file. (#1225)
* Add inspection to check that the label comes after the caption. (#1235)

# Changes
* If a file has a run configuration associated, treat it as a root file. (#1198)
* Improve performance of line markers. (#1200)
* Ignore \ifoot for \if-like commands. (#1220)
* Also execute Grazie grammar checking at the beginning of sentences. (#1196)
* Always show LaTeX Tools menu to avoid performance problems. (#1257)

# Bug fixes
* Fixed crashes. (#1211)

Thanks to Niko Strijbol [(@niknetniko)](https://github.com/niknetniko), Felix Berlakovich [(@fberlakovich)](https://github.com/fberlakovich) and [@Lukas-Heiligenbrunner](https://github.com/Lukas-Heiligenbrunner) for contributing to this release.

A detailed overview of the changes can be found on the [milestone page](https://github.com/Hannah-Sten/TeXiFy-IDEA/milestone/18?closed=1).
