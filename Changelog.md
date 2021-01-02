# Beta 0.7.2

# Additions
* Add support for pythontex. (#1656)
* Add back support for algorithm2e formatting. (#1661)
* Add fake section magic comments. (#1662)
* Add support for custom LaTeX SDKs in IntelliJ. (#1629, #1689)
* Add rename element for commands. (#1689)
* Add support for language injection on commands, and automatically inject Lua. (#1699)
* Add support for using the out/ directory when using pdflatex and bibtex with TeX Live. (#1697)
* Add live templates for \enquote and \footcite. (#1701)

# Changes
* Improve efficiency of fileset cache. (#1622)
* Inspection which checks that \if commands are closed is now a warning. (#1628)
* Support (almost) any non-letter char as inline verbatim delimiters. (#1645)
* Move PDF viewer setting to the run configuration. (#1649)
* Don't focus Okular after compilation. (#1655)
* Don't override icons from the Material design plugins. (#1669)
* Improve table formatting for very wide tables. (#1672)

# Bug fixes
* Fix formatting indentation inside parameters. (#1627)
* Fix negative offset in LatexUnresolvedReferenceInspection. (#1637)
* Fix file extension incorrectly added when renaming. (#1669)
* Avoid adding items to non-existing menus in MPS. (#1669)
* Fix false positive for escaping math operators when in \text. (#1679)
* Improve smart quote substitution for csquotes. (#1685)
* Fix freeze when WSL bash asks for user input. (#1681)
* Only create subdirs in output dir for non-MiKTeX installations. (#1681)
* \part is not a sectioning command when exam class is used. (#1681)
* Support multiple middle pseudocode blocks. (#1689)
* Fix bibtex documentation popup in autocompletion. (#1695)
* Other small fixes and improvements. (#1651, #1652)

Thanks to Felix Berlakovich ([@fberlakovich](https://github.com/fberlakovich)) and [Stefan Lobbenmeier](https://github.com/StefanLobbenmeier) for contributing to this release.
