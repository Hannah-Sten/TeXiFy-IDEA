# Beta 0.7.5

# Additions

# Changes
* Show a warning when trying to submit a crash report when not using the latest version of TeXiFy. (#1778)

# Bug fixes


# Beta 0.7.4

# Additions
* Added support for opening an internal pdf viewer using the IntelliJ PDF Viewer plugin. (#1675)
* Added commands from all installed LaTeX packages to autocompletion. (#1710, #1762, #1763)
* Added symbol tool window. (#1752)
* Added dummy text insertion wizard. (#1767)
* Added graphic, table and dummy text wizards to a generate actions popup. (#1770)
* Added SDK type for MiKTeX on Linux/Mac. (#1774)

# Changes
* Changed Grazie rules to ignore non-text elements in a sentence. (#1744)
* Implemented a file based index for commands and environments. (#1710)
* _ and : are now only part of commands when latex3 syntax is explicitly switched on. (#1756)
* Include text in headings and quotes (and more) in word count. (#1727)
* Add BSTINPUTS by default to the bibtex run config. (#1766)
* Use texcount for word count functionality when available. (#1766)

# Bug fixes
* Fixed plugin icon not showing up in the marketplace. (#1759)
* Fixed duplicate local packages in autocompletion. (#1756)
* Fixed bibtex autocompletion for @preamble and @string. (#1756)
* Fixed bug in inline math highlighting. (#1744)
* Disable smart quotes and default live templates in verbatim. (#1764)
* Fixed a crash when multiple MiKTeX installs are present. (#1766)
* Fixed a false positive for the missing import inspection for commands being defined. (#1766)

Thanks to Stefan Lobbenmeier ([@StefanLobbenmeier](https://github.com/StefanLobbenmeier)) for contributing to this release.
