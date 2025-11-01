# Changelog

## [Unreleased]

### Added

### Fixed

## [0.11.4] - 2025-11-01

Welcome to TeXiFy IDEA 0.11.4! This release mainly improves on the refactoring done in the previous release, and fixes some bugs.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

### Added

* Improve many inspections, by @Ezrnest
* Improve support for custom label commands, by @Ezrnest
* Fold \left...\right expressions, by @jojo2357

### Fixed

* Fix support for custom label commands in structure view
* Fix 'file not found' error for input commands without extension specified
* Fix symbol tool window insertion
* Do not override build project action if there are no run configurations

## [0.11.3] - 2025-09-14

Welcome to TeXiFy IDEA 0.11.3! For this release, @Ezrnest has (re-)added a much improved support for custom command definitions (using \newcommand etc), including showing the parameter context in autocompletion.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

### Added

* The context of commands and environments (math, reference, etc) is now also supported for custom defined commands, by @Ezrnest
* Seperate arguments can even have different contexts, by @Ezrnest
* This context is now also shown in the autocompletion, by @Ezrnest
* Fix many issues related to custom command definitions, by @Ezrnest
* Improvements to predefined command and environment definitions, by @Ezrnest
* Add support for acronym package to missing aronym references inspection
* Automatically reload pdf when using latexmk continuous update
* Support no-break space character in parser
* Add support for multiple paths (comma-separated) in TEXMFHOME environment variables

## [0.11.2] - 2025-08-10

Welcome to TeXiFy IDEA 0.11.2! This release fixes a few exceptions introduced in the previous release.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

### Fixed

* Fix exception #4160 when using a tool window in non-IntelliJ IDEs
* Fix exceptions when using a makeindex run configuration
* Fix an issue with relative paths in graphic insertion wizard
* Fix exception #4144

## [0.11.1] - 2025-08-04

Welcome to TeXiFy IDEA 0.11.1! This release is a major rewrite of all index-related functionality by @Ezrnest, which massively improves performance. This may have introduced new bugs, so please report any new issue to GitHub.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

### Added

* Rewrite all index-related functionality to improve performance, by @Ezrnest

### Fixed

* Fix a parser issue when using inline math inside a \hbox inside inline math 
* Fix a parser issue when using \newcommand without braces
* Fix a possible UI freeze when an output directory is used with more than 10000 subdirectories
* Fix a few cases which could freeze the UI.
* Fix exception when running LaTeX on WSL with PyCharm 2025.1+ 
* Fix a caching issue when building the file set
* Fix a rare case where an autocompile rerun would try to start before the previous run finished
* Fix incorrect slashes in path on Windows after pasting image
* Fix exceptions #4116, #4129

## [0.10.4] - 2025-07-01

Welcome to TeXiFy IDEA 0.10.4! This release contains a lot of improvements to the performance and to pdf viewers support, contributed by @Ezrnest.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

### Added

* Improve memory usage, by @Ezrnest
* Performance improvements for folding, breadcrumbs and more, by @Ezrnest
* Improve Sumatra installation detection, by @Ezrnest
* Move Sumatra custom path from run configuration to global settings, by @Ezrnest
* Add option to disallow the pdf viewer to focus, by @Ezrnest
* Add system default as option for the pdf viewer, for Mac/Linux
* Add warning to Detexify tool window
* Improve performance on startup
* Add support for file references for the tikzfig command
* Add run configuration option to configure pdf viewer focus after compilation, by @Ezrnest
* Update wsl command to use wsl --exec
* Support user defined commands when checking for text arguments for Grazie

### Fixed

* Avoid a possible UI freeze when finding files to index for native TeX Live installations
* Fix a bug where the documentation popup would not update correctly
* Fix a few bugs related to the root files cache
* Fix an issue with the file set cache when opening multiple projects
* Fix auto compilation not rerunning correctly, by @Ezrnest
* Fix SumatraPDF forward search not using the correct file, by @Ezrnest
* Fix extra whitespace when inserting commands in math mode, by @Ezrnest
* Fix exceptions #4035, #4044, #4058, #4072, #4074, #4079, #4085, #4086, #4092, #4098 
* Create run configuration process in background

## [0.10.3] - 2025-05-16

Welcome to TeXiFy IDEA 0.10.3! This release has again a lot of performance improvements, and fixes a critical issue if you have many source roots in your LaTeX module.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

### Added

* Improve performance of file set cache refresh
* Always create output directory if it does not exist
* Remove action to delete all generated files
* Add support for reference resolving and autocompletion for the acronym package
* Improve performance of the file structure popup
* Add filters to filter out some elements by default in the file structure popup
* Add setting to enable spellcheck in 'code' scope

### Fixed

* Workaround for disappearing stub indexes causing inspection false positives
* Make sure not to start a new autocompile run if a previous one is still running
* Fix validation when renaming a bibtex identifier
* Fix a possible UI freeze when computing obsolete pdf viewer settings
* Do not run grammar checks in graphicspath argument
* Fix the command line becoming too long when there are hundreds of source roots in the module
* Fix graphic insertion wizard not opening after pasting an image from the clipboard
* Fix exceptions #3967, #3994, #3995
* Make sure the external package inclusion cache refills are not attempted unnecessarily
* Fix concurrency issues when reading output of system commands

## [0.10.2] - 2025-04-01

Welcome to TeXiFy IDEA 0.10.2! This release has a lot of performance improvements, and fixes some minor bugs.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

### Added

* Improve code to find environment definitions in packages
* Add more \gls-like commands
* Remove labels when using jlatexmath preview, as these are unsupported by jlatexmath
* Improve performance checking for external direct file inclusions to index
* Improve performance of the alias manager
* Add WSL support to the bibtex run configuration, using a new setting to choose the LaTeX distribution
* Default to 0.15.0 behaviour if inputs field is not found in Tectonic.toml

### Changed

* Only the source root of the module containing the main file is used for -include-directory, instead of source roots of all modules

### Fixed

* Fix exceptions #3938, #3939, #3940, #3955
* Fix a UI freeze which could occur with autocompile on save on project open
* Fix unicode inspection for \Ohm, by @slideclimb
* Fix a false negative of the non-matching environment name inspection for empty parameters
* Fix a possible UI freeze if pdflatex doesn't exit while TeXiFy is checking for files to index
* Fix StackOverflowError when color definitions reference each other
* Fix #3906 invalid element exception
* Fix spellcheck not working in comments

## [0.10.1] - 2025-02-08

Welcome to TeXiFy IDEA 0.10.1! This release fixes a performance issue with the "Package not installed" inspection, adds the option to use macros in environment variables in the run configuration, and fixes a few exceptions.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

### Added

* Add option to run configuration to expand macros in environment variables, by @briha
* Add environment variables option to the external tool run configuration
* Let user defined folding regions override section folding regions by default

### Fixed

* Fix a performance problem caused by cache misses in "Package not installed" inspection
* Fix missing glossary reference inspection when using a glossary definition command in a \newcommand
* Improve log parsing of 'undefined control sequence' error
* Fix a parsing issue when a verbatim command is followed by a space
* Make language injection id check case insensitive
* Fix exceptions #3843, #3853, #3852, #3846, #3873, #3885 and #3866

## [0.10.0] - 2025-01-27

Welcome to TeXiFy IDEA 0.10.0! This release contains a lot of additions and performance improvements. Please report any UI freezes, slow typing or other performance issues to the GitHub issue tracker.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

### Added

* Refresh file set cache in background
* Support Tectonic V2 CLI in run configuration
* Add basic support for multiple inputs in Tectonic.toml
* Improve performance of file set cache used by inspections
* Support label references to user defined listings environment
* Add option to disable automatic compilation in power save mode
* Convert automatic compilation settings to a combobox
* Add experimental support for the addtoluatexpath package
* Add inspection to check for LaTeX package updates
* Add checkboxes to graphic insertion wizard for relative width or height
* Add sections to breadcrumbs
* Improve performance when starting a run configuration and when using autocompletion directly after starting the IDE
* Change order in structure view to match source file and sectioning level 
* Add command redefinitions to command definition filter in structure view
* Add support for automatic language injection on the minted environment
* Add support for automatic detection of custom command aliases for include commands
* Add support for DeclareGraphicsExtensions
* Add inspection to warn about a missing reference for a glossary occurrence
* Do not fold sections in a command definition
* Include optional parameters in spellcheck, if it contains text
* Improve performance of finding files to be indexed
* Show formatted file path in file not found inspection quickfix name
* Automatically index bibliography files outside the project that are included by an absolute path
* Disable quotes inspection when TeX ligatures are disabled by fontspec
* Inspections can now be suppressed for any single line, or block of text

### Fixed

* Fix parse error when using commands with arguments in parameter of \href or \url
* Fix parse error when using parentheses in a group in a key value command argument
* Fix parse erron when using inline math in cases* environment in inline math
* Fix exceptions #3813, #3818
* Fix false positive non-breaking space warning for \nameref
* Fix confusion with \micro and \mu unicode characters
* Fix auto import from local bibtex file on Windows
* Fix false positive for duplicate command definition inspection in if/else
* Fix LaTeX files not showing up when choosing main file in run configuration
* Fix various issues with the Grazie implementation, in particular default rules for Grazie Pro

## [0.9.9] - 2024-12-01

Welcome to TeXiFy IDEA 0.9.9! This release improves subfiles support, fixes some parser errors and fixes many other small bugs.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

### Added

* Use theme foreground and background colors for equation preview
* Add support for \includesvg
* Support references in \subfix command
* Provide autocompletion for all relevant directories when using subfiles
* Do not format existing directories in the given path in the file creation quickfix dialog
* Disable Evince inverse search on Windows to avoid a UI freeze on opening a second project
* Use xdg-mime to choose a default pdf viewer in the run configuration template
* Don't insert the right brace when in front of text
* Add DeclareMathSymbol to indexed command definitions in installed packages
* Add support for TEXMFHOME for reference resolving
* Add diffcoeff and upgreek packages to autocompletion, by @Toseflo
* Improve reference resolving when using subfiles
* Add setting to disable auto-import of bibtex entries from remote libraries

### Fixed

* Disallow unmatched braces after \@ifnextchar
* Fix exception #3771 when a file referenced from cache is deleted
* Fix basic case of false positive of duplicate label inspection when user defined \if commands are used
* Fix a parse error when using \else with a user defined \if-command
* Fix relative path conversion in graphics insertion wizard by resolving relative to the root file
* Fix exception #3763
* Fix 'missing import' false positive in subfiles
* Don't override the file icon for .txt files, by @Steve-Li-1998
* Fix exceptions #3754 and #3326
* Fix exceptions in structure view when command parameters are missing
* Improve error report submitter for long stacktraces
* Fix a parser issue with bidirectional arrow in TikZ
* Fix default Docker image name when running Dockerized TeX Live without a project SDK
* Always use content roots to resolve references
* Fix 'package not found' error when using texlive-full on Windows, and improve running of system commands, by @tristankretzer
* Fix rename of files when using subfiles
* Fix incorrect 'package already included' warning for the subfiles package
* Fix exception #3557 if using bibtex structure view when bibtex file type is reassigned to plain text
* Avoid referencing obsolete psifiles, fix exception #3635

## [0.9.8]

Welcome to TeXiFy IDEA 0.9.8! This release adds a new intention, improves autocompletion and fixes some exceptions.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

### Added

* Add intention to swap command arguments, by @jojo2357
* Insert \right) when typing \left( without using autocomplete, by @slideclimb
* Update siunix commands in autocompletion, by @Toseflo

### Fixed

* Fix crash in structure view if section command has no parameters
* Fix exceptions #3698, #3672, #3699 and #3659

## [0.9.7] - 2024-07-12

Welcome to TeXiFy IDEA 0.9.7! This release improves the Evince support by Tim Klocke (@taaem), adds a simple editor for postfix templates, and more.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

### Added

* Improve Evince forward/inverse search support, by @taaem
* Support conversion of arguments in \def -> \newcommand quickfix, by @slideclimb
* Add a simple editor for postfix templates, by @slideclimb
* Add support for \ProvidesExpl(Class|File), by @Sirraide
* Support TeX Live docker image
* Formatter support for plain TeX \if-statements
* Index files from the TEXINPUTS variable, for autocompletion

### Fixed

* Fix Evince synchronization after creating a new run configuration, by @taaem
* Fix unresolved file reference for \input commands

## [0.9.6] - 2024-06-01

Welcome to TeXiFy IDEA 0.9.6! This release fixes an issue with the table insertion wizard, fixes pasting from a pdf file, and more.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

### Added

* Don't insert \right when brace is already matched, by @slideclimb
* Show file and line number in 'go to definition' view, by @slideclimb
* Do not select the extension when refactoring file names, by @jojo2357

### Fixed

* Handle file extensions case sensitive, by @jojo2357
* Autocomplete \left* with \right* for all variants, by @jojo2357
* Fix pasting text from pdf file
* Fix table insertion wizard not inserting text

## [0.9.5] - 2024-05-01

Welcome to TeXiFy IDEA 0.9.5! This release automatically translates HTML from the clipboard to LaTeX when pasting, by @jojo2357, and includes a lot of additions and bug fixes!

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

### Added

* Improve performance when editing run configurations
* Add support for multiline todos by @slideclimb
* Added multicite versions of biblatex commands by @frankbits
* Add \newcommandx and related commands from the xparse package, by @tmczar
* Improve line wrapping by preferring line breaks at whitespace
* Add support for the nomencl package
* Add starred and capitalized versions of cleveref commands to exceptions for non-breaking space inspection, by @niknetniko
* Add option to the run configuration settings to run LaTeX commands before compiling the main file
* Autocompletion for compiler arguments in run configuration settings
* Support a local Zotero instance in the remote libraries tool window via the BBT plugin
* Support Zotero groups in the remote libraries tool window
* Support local BibTeX files in the remote libraries tool window
* Improve file filters for the LaTeX package index
* Improve \DescribeMacro handling for the package doocumentation index
* Automatically translate HTML from the clipboard to LaTeX, by @jojo2357
* Add option to disable indentation of environments, by @slideclimb

### Fixed

* Fix exceptions #3510, #3462, #3512
* Fix parse error when using \BeforeBeginEnvironment or \AfterEndEnvironment
* Fix quoted links in bibtex
* Ignore \begin and \end commands in \newcommand definition in the parser
* Fix a parser issue when having a single \begin or \end in a \newcommand definition
* Fix exception #2976
* Fix exception #3469
* Avoid line breaks when reformatting in the middle of commands, math and words
* Fix exception #3274 in the equation preview
* Never use jlatexmath for the TikZ preview
* Destroy invalid tokens for the remote libraries tool windows
* Fix missing folding for commands in math environments, by @jojo2357
* Fix an issue when inlining files with whitespace, by @jojo2357

## [0.9.4] - 2024-02-06

Welcome to TeXiFy IDEA 0.9.4! This release ensures that TeXiFy works well in the upcoming 2024.1 IntelliJ release.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

### Added

* Add support for IDEA 2024.1
* Add folding settings for all folding builders
* Improve internal logging

### Fixed

* Fix autocompletion of file path arguments when text is already present

## [0.9.3] - 2024-01-16

Welcome to TeXiFy IDEA 0.9.3! This release fixes some UI freezes related to package indexing and autocompletion, and fixes a few bugs.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

### Added

* Improve MiKTeX package source files extraction
* Add optidev environments as math environments, by @leandrolerena
* Improve autocompletion performance after starting IDE
* Improve plugin loading performance

### Fixed

* Improve user feedback for equation preview when Inkscape is not installed
* Fix incorrectly inserted \items in enumeration environments, by @jojo2357
* Fix false positives for equation gathering inspection, by @jojo2357
* Don't attempt to use mthelp when it is not available, by @jojo2357
* Fix #3361: false positive on duplicate identifier on @string entries in bib files
* Replace code deprecated in 2023.3
* Avoid creating output directories recursively and improve the cleanup process

## [0.9.2] - 2023-11-24

Welcome to TeXiFy IDEA 0.9.2! This release introduces a new 'extract value' functionality to easily create custom commands from a text selection, and fixes some minor bugs.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

### Added

* Add "Extract value" refactoring to create custom LaTeX commands and replace all occurrences, by @jojo2357
* Include bibliography files from the LaTeX installation in the possible inclusion targets
* Add some missing starred versions of table environments
* Add support for package indexing for native TeX Live installations

### Fixed

* Ignore non-TeXiFy indexing related exceptions
* Fix structure view nesting
* Fix some bugs in the math environment toggle intention
* Fix support for inlining commands in non-IntelliJ IDEs, by @jojo2357
* Fix a sync issue with remote libraries

## [0.9.1] - 2023-08-15

This release fixes a bug related to the new file icons from the previous release.

### Added

* Improve performance on plugin installation

### Fixed

* Fix a bug where the plugin would override file icons of non-LaTeX files

## [0.9.0] - 2023-08-14

Welcome to TeXiFy IDEA 0.9.0! In this release, we celebrate the completely new icon set by @HannahSchellekens which blends in much better with the new IntelliJ UI.
Enjoy the new icons for LaTeX file types, menu entries, tool windows and more!

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

### Added

* New icons
* Improve performance of file reference resolving

### Fixed

* Fix a parse issue with TEXINPUTS
* Fix a possible infinite loop when resolving imports
* Fix an initialization issue in the remote library tool window
* Fix an incorrect package import when completing commands from a project class file that is also in a project root

## [0.7.33] - 2023-07-27

This release fixes an exception introduced by IntelliJ 2023.2.

### Fixed

* Fix a PluginException that occurred in 2023.2
* Fix parsing of TEXINPUTS
* Change phi and varphi folding characters

## [0.7.32] - 2023-07-13

### Added

* Add option to add custom environments/commands in label convention settings, by @jojo2357
* Autocomplete \{...\}, by @jojo2357
* Improve inspections performance
* Include globally defined TEXINPUTS when looking for files

### Fixed

* Fix PluginException #3140
* Fix false positive non-breaking space warning when starting a sentence with a reference, by @jojo2357

## [0.7.31] - 2023-07-01

Welcome to TeXiFy IDEA 0.7.31! This release improves the grammar checks and syntax highlighter, as well as the parsing of optional parameters.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.

### Added

* Add text parameters of commands to grammar checked text
* Improved parsing of optional parameters
* Internal improvements (#3092, #3102)

### Fixed

* Improve code of the documentation provider
* Fix double highlighting of inline and display math, by @jojo2357
* Fix false positive grammar error when a sentence ends with a closing brace
* Fix false positive grammar error when a newline follows a command between sentences

## [0.7.30] - 2023-06-01

Welcome to TeXiFy IDEA 0.7.30! This release removes the LaTeX Module Type deprecated by IntelliJ, fixes some bugs and more.

We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
Your input is valuable and well appreciated.
In particular, many thanks to @svensieber for reporting a critical bug in a pre-release version!

### Added

* Add inspection to check for suspicious formatting in section-like commands
* Add latexindent command line options to settings
* Add inspection preview for the unicode inspection.
* Add partial support for detecting non-global installations of SumatraPDF.

### Fixed

* Fix Textidote exceptions #3086 and #3089
* Fix NPE #3083
* Fix an issue where the LaTeX Project Task Runner would override those of other languages
* Internal improvements (#3070, #3072, #3074)
* Fix InvalidVirtualFileAccessException #2991
* Disable some editor actions in non-LaTeX files
* Disable forward search action in non-LaTeX files, by @endorh
* Fix IndexOutOfBoundsException #3036
* Fix FileBasedIndex getting a default project (#3049)
* Fix issue with running an unsupported run configuration taken from another OS.

### Removed

* Removed the LaTeX module type, as this is deprecated by IntelliJ

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

[Unreleased]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.11.4...HEAD
[0.11.4]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.11.3...v0.11.4
[0.11.3]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.11.2...v0.11.3
[0.11.2]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.11.1...v0.11.2
[0.11.1]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.10.4...v0.11.1
[0.11.0]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.10.4...v0.11.0
[0.10.4]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.10.3...v0.10.4
[0.10.3]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.10.2...v0.10.3
[0.10.2]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.10.1...v0.10.2
[0.10.1]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.10.0...v0.10.1
[0.10.0]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.9.9...v0.10.0
[0.9.9]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.9.8...v0.9.9
[0.9.8]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.9.7...v0.9.8
[0.9.7]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.9.6...v0.9.7
[0.9.6]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.9.5...v0.9.6
[0.9.5]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.9.4...v0.9.5
[0.9.4]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.9.3...v0.9.4
[0.9.3]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.9.2...v0.9.3
[0.9.2]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.9.1...v0.9.2
[0.9.1]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.9.0...v0.9.1
[0.9.0]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.33...v0.9.0
[0.7.33]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.32...v0.7.33
[0.7.32]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.31...v0.7.32
[0.7.31]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.30...v0.7.31
[0.7.30]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.29...v0.7.30
[0.7.29]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.28...v0.7.29
[0.7.28]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.27...v0.7.28
[0.7.27]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.26...v0.7.27
[0.7.26]: https://github.com/Hannah-Sten/TeXiFy-IDEA/compare/v0.7.25...v0.7.26
[0.7.25]: https://github.com/Hannah-Sten/TeXiFy-IDEA/commits/v0.7.25
