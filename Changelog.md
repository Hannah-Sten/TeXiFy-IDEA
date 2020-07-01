# Beta 0.7

# Additions
* Add inspection to encourage proper nesting of sectioning commands. (#1492)
* Add support for custom labeling and referencing commands. (#1442, #1507)
* Add folding for escaped symbols. (#1511)

# Changes
* Commands with optional parameters now appear separately in the autocomplete. (#1461)
* Create and use default output path if no output path is present. (#1460)
* Delay registering TikZ and Equation Preview tool windows until they are needed. (#1504)

# Bug fixes
* Disable Non-Ascii inspection in non-identifier commands. (#1506)
* Fix optional parameter parsing for \newcommand when first parameter for the new command is optional. (#1453)
* Cite before interpunction inspection should not trigger on abbreviations. (#1498)
* Other small improvements and bug fixes (#1436, #1448, #1473, #1498, #1509)

Thanks to Johannes Berger ([@Xaaris](https://github.com/xaaris)) and [@VhJoren](https://github.com/VhJoren) for contributing to this release.
