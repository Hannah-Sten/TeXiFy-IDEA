## Beta 0.7.20

### Additions
* Add support for bibtex reference managers Mendeley and Zotero. (#2539)
* Improved autocompletion initialization performance. (#2517)
* Improve inspection performance when the import package is used. (#2540)
* Resolve symlinks when suggesting home paths for TeX Live SDK. (#2547)

### Bug fixes
* Fix false positive warning about escaping characters when using the blkarray package. (#2504)
* Fix custom compiler arguments when using WSL. (#2543)
* Fix false positive inspection warnings related to ellipsis and hashes. (#2516)
* Fix check if Sumatra is installed. (#2528)
* Fix default working directory for bibtex when using miktex (#2532)
* Fix inspection false positives related to \def redefinitions. (#2544)
* Fix crashes. (#2515, #2552)

Thanks to @daniil-berg and @dpvdberg for contributing to this release!