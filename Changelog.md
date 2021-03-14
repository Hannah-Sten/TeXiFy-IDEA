# Beta 0.7.5

# Additions
* Add quick fix for normal space insertion to line break inspection. (#1791)
* Add .cbx and biblatex-dm.cfg to recognised file extensions. (#1805)
* Add documentation popup for environments. (#1802)
* Add warning when trying to use documentation while texdoc is not installed. (#1802)
* Index which LaTeX packages include which other LaTeX packages. (#1799)
* Improve autocompletion performance. (#1798)

# Changes
* Show a warning when trying to submit a crash report when not using the latest version of TeXiFy. (#1778)
* Make inspection levels configurable by user. (#1781)
* Look in run configurations for possible paths to latex executables when using PyCharm on a Mac. (#1791)
* Underscore is now a valid bibtex type character. (#1791)
* \url and \href are now verbatim commands. (#1784)
* Use safe delete quickfixes for some 'unused element' inspections. (#1787)

# Bug fixes
* Fix line breaking of comments during formatting. (#1776)
* Fix go to command definition when it is a custom label referencing command. (#1784)
* Fix parse error on \newenvironment. (#1802)
* Fix exception when MiKTeX is not installed on Windows. (#1798)
* Fix executable path for native TeX Live SDK. (#1798)