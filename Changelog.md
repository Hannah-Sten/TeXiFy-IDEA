# Beta 0.6.8


## Additions
- Add support for the Grazie grammar and spellchecking plugin. (#1120)
- Add support for texdoc on TeX Live. (#1125)
- Add option to disable auto package insert. (#1131)

- Add subfiles support. (#1131)

## Changes
- Improve the math environment switcher. (#1080)
- Remove obsolete soft wraps setting which is built-in in IDEA now. (#1084)
- Improve exception handling for forward search on Linux. (#1087)
- Disable unicode quickfixes for TeX Live versions 2018 or later as these are included by default. (#1088)
- First stop Sumatra before starting it with new inverse search settings. (#1128)

## Bug fixes
- Include commands like \bibliography and \input can now recognize multiple files as argument. (#782)
- Add forward slash as valid bibtex identifier. (#1086)
- Allow number-only bibtex identifiers. (#1126)
- Include files included by class file in fileset. (#1123)
- Only check for normal spaces after abbreviations when they end with a full stop. (#1129)
- Fixed crashes. (#1085)

Thanks to [@TanVD](https://github.com/TanVD) for contributing to this release.

A detailed overview of the changes can be found on the [milestone page](https://github.com/Hannah-Sten/TeXiFy-IDEA/milestone/17?closed=1).