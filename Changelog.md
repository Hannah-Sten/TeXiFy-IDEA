# Beta 0.6.8


## Additions
- Add support for the Grazie grammar and spellchecking plugin. (#1120)
- BibTeX autocompletion now also gives suggestions when typing author or title. (#1152)
- Add support for Skim on MacOS including forward and backward search. (#1163)
- Add support for texdoc on TeX Live. (#1125)
- Add support for automatic compilation. (#1140)
- Add option to disable auto package insert. (#1131)
- Add support for compression prevention from the cleveref package. (#1134) 
- Add subfiles support. (#1131)

## Changes
- Improve the math environment switcher. (#1080)
- Remove obsolete soft wraps setting which is built-in in IDEA now. (#1084)
- Improve exception handling for forward search on Linux. (#1087)
- Disable unicode quickfixes for TeX Live versions 2018 or later as these are included by default. (#1088)
- First stop Sumatra before starting it with new inverse search settings. (#1128)
- Internal parser code improvements. (#1156, #1162, #1157)

## Bug fixes
- Include commands like \bibliography and \input can now recognize multiple files as argument. (#782)
- Add forward slash as valid bibtex identifier. (#1086)
- Allow number-only bibtex identifiers. (#1126)
- Show correct gutter icons for included files. (#1137)
- Include files included by class file in fileset. (#1123)
- Only check for normal spaces after abbreviations when they end with a full stop. (#1129)
- Use actual instead of incorrectly indexed command name for missing label inspection. (#1136)
- Register the analyze menu action group using a service instead of a deprecated application component. (#1144 )
- Fixed crashes. (#1085)

Thanks to [@TanVD](https://github.com/TanVD), [@fberlakovich](https://github.com/fberlakovich) and [@stsundermann](https://github.com/stsundermann) for contributing to this release.

A detailed overview of the changes can be found on the [milestone page](https://github.com/Hannah-Sten/TeXiFy-IDEA/milestone/17?closed=1).