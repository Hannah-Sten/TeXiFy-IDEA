# Beta 0.7.11

Welcome to TeXiFy IDEA 0.7.11! This release improves the equation preview, improves support for Tectonic and adds a bibtex-tidy action.

## Additions
* Add 'reformat with bibtex-tidy' action. (#2069)
* Add angular brackets as parameter to parser. (#2076)
* Add Tectonic SDK. (#2080)
* Implement reference resolving for packages for Tectonic SDK. (#2080)

## Changes
* Reduce number of commands in the autocompletion for TeX Live. (#2054)
* By default, do the equation preview with jlatexmath, if there is no custom preamble. (#2028)
* Updated changelog. (#2089)

## Bug fixes
* An empty bibtex field is not a parse error anymore. (#2082)