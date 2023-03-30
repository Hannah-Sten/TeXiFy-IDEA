# Changelog

## [Unreleased]

### Added
* Support @Comment comments in bibtex

### Fixed

## [0.7.28-alpha.3] - 2023-03-25

### Added
* Improve run configuration performance for TeX Live
* Add custom highlighting for user-defined commands
* Add setting to configure default folding of imports
* Add \newminted code blocks as verbatim environments

### Fixed
* Improve file set cache to fix exception #2903
* Fix exception #2950
* Disable obsolete LatexBibinputsRelativePathInspection
* Fix bug when resolving \subimport

## [0.7.28-alpha.1] - 2023-03-11

### Added
* Add custom highlighting for user-defined commands

### Fixed
* Fix bug when resolving \subimport

## [0.7.27] - 2023-03-09
Welcome to TeXiFy IDEA 0.7.27! This release fixes a bug related to dashes, and adds an option to disable the (slow) indexing of your LaTeX installation.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

Thanks to @FWDekker for contributing to this release!

### Added
* Add information about newtxmath by @FWDekker
* Add an option to disable indexing of LaTeX installation.

### Fixed
* Fix an issue with dashes in referencing elements.
* Fix a bug in "Convert to LaTeX alternative" quickfix
* Fix exceptions #2928 and #2937

## [0.7.26] - 2023-02-01
Welcome to TeXiFy IDEA 0.7.26! This release has some notable performance improvements and fixes many exceptions.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

Thanks to @jojo2357 and @Yodude2002 for contributing to this release!

### Added
* Improve performance when starting a run configuration.
* Don't wrap urls when formatting.
* Add (custom) highlighting for the equals separator in key-value pairs
* Add support for inlining user-defined commands, by @jojo2357.
* Add tabularray package, by @Yodude2002.

### Fixed
* Fix exceptions #2895, #2896 and #2856.
* Fix unreliable \item insertion in itemize.
* Reformat suggested file name before showing it to the user, instead of afterwards.
* Improve performance when checking pdf viewer availability.
* Improve error logging.
* Fix exceptions.
* Filter out uninjectable languages in intention.
* Fix robust inline math brace matching.
* Fix custom file name being overridden in inspection quickfix.

## [0.7.25] - 2022-12-01
Welcome to TeXiFy IDEA 0.7.25! This release has many additions by @jojo2357, enjoy!

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

Thanks to @jojo2357 and @MisterDeenis for contributing to this release!

### Added
* Support undoing 'move se(le)ction to file' actions, by @jojo2357. ([#2739](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/2739))
* Add 'inline included file' action, by @jojo2357. ([#2741](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/2741))
* Enable synchronizing remote libraries without opening the tool window. ([#2749](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/2749))
* Ensure renamed commands start with a backslash, by @jojo2357. ([#2756](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/2756))
* Performance improvements. ([#2778](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/2778))
* Add file inclusion cache for MiKTeX on Mac/Linux. ([#2780](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/2780))
* Complete rework of the support for a custom path to SumatraPDF, by @MisterDeenis. ([#2781](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/2781))

### Fixed
* Fix incorrect section contents moved to file, by @jojo2357. ([#2739](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/2739))
* Fix syntax highlighting for custom color scheme, by @jojo2357. ([#2761](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/2761))
* Fix unreliable forward search. ([#2777](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/2777))
* Fix crashes. ([#2747](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/2747))
* Fix some intention previews. ([#2796](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/2796))
* Other small bug fixes and improvements. ([#2776](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/2776), [#2774](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/2774), [#2765](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/2765)-[#2773](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/2773))

[Unreleased]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.28-alpha.3...HEAD
[0.7.28-alpha.1]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.27...v0.7.28-alpha.1
[0.7.28-alpha.3]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.28-alpha.1...v0.7.28-alpha.3
[0.7.27]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.27-alpha.2...v0.7.27
[0.7.27-alpha.2]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.26...v0.7.27-alpha.2
[0.7.26]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.26-alpha.8...v0.7.26
[0.7.25]: https://github.com/Hannah-Sten/TeXiFy-IDEA/commits/v0.7.25
