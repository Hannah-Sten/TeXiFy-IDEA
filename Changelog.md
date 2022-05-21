## Beta 0.7.18

### Additions
* Replace non-ascii characters when generating a label name. (#2360)
* Improve performance of inspections and formatting. (#2344)
* Disable some autocompletion related to inline math in verbatim environments. (#2362)
* Add command line arguments for makeindex run config. (#2363)
* Improve latex3 syntax highlighting
* Improve completion in itemize. (#2368)

### Bug fixes
* Fix false positive inspection when using cleveref with varioref. (#2361)
* Fix macro resolving for auxiliary path of run config. (#2365)
* Fix parse error on partial environment definitions using \pretitle \and \posttitle. (#2372)
* Fix bug in text detection for Grazie inspections. (#2375)