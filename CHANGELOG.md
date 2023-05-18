# Changelog

## [Unreleased]

### Added

### Fixed

## [0.7.30-alpha.3] - 2023-05-17

### Added
* Add inspection preview for the unicode inspection.
* Add partial support for detecting non-global installations of SumatraPDF.

### Fixed
* Internal improvements (#3070, #3072, #3074)
* Fix InvalidVirtualFileAccessException #2991
* Disable some editor actions in non-LaTeX files
* Disable forward search action in non-LaTeX files, by @endorh
* Fix IndexOutOfBoundsException #3036
* Fix FileBasedIndex getting a default project (#3049)
* Fix issue with running an unsupported run configuration taken from another OS.

### Removed
* Removed the LaTeX module type, as this is deprecated by IntelliJ

## [0.7.30-alpha.2] - 2023-05-12

### Added
* Add inspection preview for the unicode inspection.
* Add partial support for detecting non-global installations of SumatraPDF.

### Fixed
* Fix InvalidVirtualFileAccessException #2991
* Disable some editor actions in non-LaTeX files
* Disable forward search action in non-LaTeX files, by @endorh
* Fix IndexOutOfBoundsException #3036
* Fix FileBasedIndex getting a default project (#3049)
* Fix issue with running an unsupported run configuration taken from another OS.

## [0.7.30-alpha.1] - 2023-05-03

### Added
* Add inspection preview for the unicode inspection.
* Add partial support for detecting non-global installations of SumatraPDF.

### Fixed
* Fix issue with running an unsupported run configuration taken from another OS.

## [0.7.29] - 2023-04-14
Welcome to TeXiFy IDEA 0.7.29! This release fixes the equation preview, and fully supports IntelliJ 2023.1.

### Added
* Update minimum required IntelliJ version to 2023.1

### Fixed
* Fix equation preview TranscoderException
* Fix IncorrectOperationException

## [0.7.28] - 2023-04-01
Welcome to TeXiFy IDEA 0.7.28! This release fixes some exceptions, adds comment folding and improves run configuration performance even more.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

Thanks to @jojo2357 for contributing to this release!

### Added
* Support @Comment comments in bibtex
* Fold blocks of comments, by @jojo2357
* Highlight comment environment as a comment, by @jojo2357
* Improve run configuration performance for TeX Live
* Add custom highlighting for user-defined commands
* Add setting to configure default folding of imports
* Add \newminted code blocks as verbatim environments

### Fixed
* Fixed usage of IntelliJ api deprecated in 2023.1
* Fixed exception "Cannot distinguish StubFileElementTypes".
* Fix exception #2971
* Improve file set cache to fix exception #2903
* Fix exception #2950
* Disable obsolete LatexBibinputsRelativePathInspection
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

[Unreleased]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.30-alpha.3...HEAD
[0.7.30-alpha.2]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.30-alpha.1...v0.7.30-alpha.2
[0.7.30-alpha.1]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.29...v0.7.30-alpha.1
[0.7.30-alpha.3]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.30-alpha.2...v0.7.30-alpha.3
[0.7.29]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.28...v0.7.29
[0.7.28]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.27...v0.7.28
[0.7.27]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.26...v0.7.27
[0.7.26]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.25...v0.7.26
[0.7.25]: https://github.com/Hannah-Sten/TeXiFy-IDEA/commits/v0.7.25
