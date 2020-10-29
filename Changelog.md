# Beta 0.7.2

# Additions

# Changes
* Improve efficiency of fileset cache. (#1622)

# Bug fixes
* Fix formatting indentation inside parameters. (#1627)


# Beta 0.7.1

# Additions
* Support paths relative to main file or project dir in run configuration template. (#1553, #1601)
* Add support for the Zathura pdf viewer. (#1551)
* Add support for the glossaries package. (#1572)
* Add action to shift arguments. (#1585)
* Add inspection which checks if the documentclass is in the same file as the document environment. (#1617)
* Improve subfiles support. (#1462)

# Changes
* Don't override command line arguments when a latexmkrc file is used. (#1586, #1597)
* Improve speed of package not installed inspection when a package is not installed. (#1595)
* Use the document environment instead of the documentclass command as indication of the root file. (#1617)

# Bug fixes
* Fix some log parsing issues with parentheses. (#1579)
* Include optional parameters in position when looking for labels from redefined commands. (#1577, #1598)
* Fix a parse error with nested brackets in optional parameter of verbatim environment. (#1569)
* Fix file inclusion loop triggered incorrectly. (#1570)
* Fix a concurrency issue with the fileset cache. (#1567)
* Fix bibtex autocompletion not working with multiple comma-separated keys. (#1566)
* Fix incorrect insertion of spaces around braces in bibtex. (#1609)
* Fix some log parsing issues with long filenames and improve log parsing in some other cases. (#1604, #1613)
* Fix sync from source for structure view. (#1613)
* Fix kpsewhich not being used for other files except sty/cls. (#1620)
* Other bug fixes and improvements. (#1576, #1603, #1587, #1626)

Thanks to Felix Berlakovich ([@
fberlakovich](https://github.com/fberlakovich)) and Johannes Berger ([@Xaaris](https://github.com/xaaris)) for contributing to this release.
