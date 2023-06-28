This is a work in progress. More features should be added, and each should link to its own page with documentation. Link to IJ docs where possible and useful

## [[searching]] Searching through this wiki

You can search through this wiki by using the GitHub global search bar at the top of the page, and then selecting Wiki results.

## Installation

Also see the [installation guide](Installation).

* Support for [Windows](Installation#windows-instructions), [Mac](Installation#mac-instructions) and [Unix](Installation#linux-instructions) (via for example MiKTeX or [TeX Live](Installation#texlive))
* [MIT license](https://github.com/Hannah-Sten/TeXiFy-IDEA/blob/master/LICENSE)
* Built on the IntelliJ IDE which has many features built in ([documentation](https://www.jetbrains.com/help/))
* [Works with all Jetbrains IDEs](https://plugins.jetbrains.com/plugin/9473-texify-idea/versions), including IntelliJ, PyCharm, PhpStorm, DataGrip, Rider, GoLand, RubyMine, Android Studio, WebStorm and CLion.
* Suggested updates of TeXiFy and one click to update
* [Prerelease alpha builds available](Alpha-builds)

## Writing LaTeX

* [Unicode support](Unicode)
* [Autocomplete of labels, all commands from installed packages, custom defined commands and (custom) environments](Autocomplete)
* [Smart quote substitution](Global-settings#option-to-enable-smart-quote-substitution)
* [Automagically import packages of common commands](Automatic-package-importing)
* [Shortcuts for styling text](Features#Menu-entries)
* [Line commenting shortcut](Line-commenting)
* [Renaming of labels and environments](Refactoring)
* [Inlining of files and command definitions](Inline-File)
* Support for user-created document classes and packages
* Toggle star action
* Custom shortcuts for menu items
* [Brace matching](Brace-matching)
* [Live templates](Live-templates)
* Custom live templates
* [Postfix templates](Postfix-templates)
* Custom macros with shortcuts
* Spellchecking with custom dictionaries
* [Support for the Grazie grammar and spellchecking plugin](Grazie)
* [Soft wraps and wrap on typing](Soft-wraps)
* [Many useful shortcuts](Shortcuts)
* [Automatic insertion of `\item` in list environments like itemize and enumerate](Item-insertion)
* Automatic insertion of `\end` when typing `\begin` for environments
* On typing `\[` automatic insertion of `\]`, after kbd:[Enter] also with correct indentation
* [Surround with dollars, quotes, or braces](Surrounding)
* Automatic insertion of braces of first required parameter
* Typing `}` at closing brace skips over it, same for `$..$`
* Multi-cursors
* [Easy switching to other math environments](Math-environment-switcher)
* [Support for `\graphicspath` from the graphicx package.](Graphicspath-support)
* [Magic comments](Magic-comments)
* [Support for `\newcommand`-like custom definitions of cite and label commands](Custom-commands)
* [Action for swapping arguments of LaTeX commands](Move-arguments)
* [Drag and drop graphics inside your document](++Menu-entries#insert-graphic-wizard++)
* [Paste HTML tables and turn them into LaTeX](++Menu-entries#table-creation-wizard++)
* [Paste images directly from your clipboard](++Pasting-images++)
* [Known issues with parsing LaTeX](Known-issues)

## Reading
* Customizable syntax highlighting
* [Code folding for imports, (sub(sub))sections, environments, some math symbols and some escaped symbols like `\%`](Code-folding)
* [Code formatting](Code-formatting)
* [Line markers for sections](Line-markers)
* [Color preview for the xcolor package](Color-preview)
* [Language injection](Language-injection)
* Special highlighting and marker for `todo` comments, customizable
* Links in comments and in commands which provide links (e.g. \url and \href) are clickable
* [Easy links to LaTeX package documentation, and documentation for many commands and environments](LaTeX-documentation)

## Preview
* [Unicode math preview](Preview#Unicode-math-preview)
* [Equation preview](Preview#Equation-preview)
* [TikZ preview](Preview#TikZ-preview)

## Navigation
* link:++Structure view++[Structure view with filters]
* [Go to declaration of labels, citations and custom commands](Go-to-declaration)
* [Label reference resolving when using the xr package](Go-to-declaration#using-the-xr-package)
* [Find usages for commands defined in a \newcommand-like way](Find-usages)
* "Goto Symbol" support for section names, labels, bibitems, new environments and new commands
* [Gutter icons and navigation shortcuts for file inclusions](File-inclusion-navigation)
* [Support for the import package](File-inclusion-navigation#import-package)
* Breadcrumbs for environments and commands

## Running
* Gutter icons for quick compilation
* [Customizable run configurations to compile LaTeX files](Run-configurations)
* [Compiler support for pdfLaTeX, LuaTeX, Latexmk, texliveonfly, XeLaTeX, Tectonic, bibtex, and biber](Compilers)
* [A clean overview of LaTeX, BibTeX and Biber warnings and errors](Log-messages)
* [Support for automatic compilation](Automatic-compilation)
* The pdf will appear in a separate out/ directory to avoid cluttering the source directory
* [Auxiliary files will be put in a separate auxil/ directory](Auxil-directory)
* Custom compiler path
* Option to choose different output formats like PDF and DVI
* [Support for TEXINPUTS and environment variables in general](Run-configurations#environment-variables)
* [Support for Dockerized MiKTeX](Dockerized-MiKTeX)
* [Support for TeX Live from WSL](WSL-support)

### PDF viewers
* [Built-in PDF viewer](Built-in-pdf-viewer-support)
* [SumatraPDF (Windows) support with forward and backward search](SumatraPDF-support)
* [Evince (Linux) support with forward and backward search](Evince-support)
* [Okular (Linux) support with forward and backward search](Okular-support)
* [Zathura (Linux) support with forward and backward search](Zathura-support)
* [Skim (MacOS) support with forward and backward search](Skim-support)
* [Custom pdf viewer](Run-configurations#Custom-pdf-viewer)
* Opens system default pdf viewer when no custom or supported pdf viewer is known

## External tools
* [Support for run configurations to create an index/glossary, also when auxil/ or out/ is used](Makeindex)
* [Jinja2 support](External-tools#Jinja2-support)
* [Run any external tool before compiling](Run-configurations#before-run-tasks)

### BibTeX

If you are new to BibTeX, see the [BibTeX introduction](BibTeX).

* Syntax highlighting
* Formatter
* [Autocomplete](BibTeX-autocomplete)
* Structure view with filters
* kbd:[Ctrl + Q] on a bibtex reference will show a popup with title and authors from the bibtex entry
* [Support for @string variables](String-variables)
* [Folding](Bibtex-folding)
* [Chapterbib support](Chapterbib-support)

## Managing LaTeX
* Never press kbd:[Ctrl + S] again: saves while you type
* Project management
* Support for multiple content roots

## Tools
* VCS integration including Git
* Terminal window
* [Access Remote Libraries](Tools#remote-libraries)
* [Detexify](Tools#detexify)
* [Extend TeXiFy functionality with custom scripts](https://github.com/dkandalov/live-plugin)
* [Word counting tool](Menu-entries#Word-counting-tool)
* [Customizable file templates for .tex, .sty, .cls and .bib files](Menu-entries#file-templates)
* [Table Creation Wizard](++Menu-entries#table-creation-wizard++)
* [Graphic Insertion Wizard](++Menu-entries#insert-graphic-wizard++)
* [Dummy Text (Lorem Ipsum) Wizard](++Menu-entries#insert-dummy-text-wizard++)
* [Menu button to delete generated auxiliary files](++Menu-entries#clear-aux-files++)
* Crash reporting dialog
* File creation dialog

## UI
* [Symbol tool window](Symbol-view)
* [Editor tabs](https://www.jetbrains.com/help/idea/using-code-editor.html#manage_tabs)
* [Split screen editing](https://www.jetbrains.com/help/idea/using-code-editor.html#split_screen)
* [Change display font](https://www.jetbrains.com/help/idea/configuring-colors-and-fonts.html#fonts)
* [Custom color scheme](https://www.jetbrains.com/help/idea/configuring-colors-and-fonts.html#customize-color-scheme)
* [RTL/bidirectional support](https://www.jetbrains.com/help/idea/text-direction.html)
* [Fancy icons that fit in with the IntelliJ style](UI#Icons)

## Settings and preferences
### Global settings

These settings can be found in menu:File[Settings > Languages & Frameworks > TeXiFy] and are global to your IntelliJ: they will be same for all projects.

* [Option to disable automatic insertion of second $](Global-settings#closing-math)
* [Option to disable automatic brace insertion around text in subscript and superscript](Global-settings#brace-insertion)
* [Option to disable auto-insertion of \item](Global-settings#item-insertion)
* [Option to disable automatic package dependency checks](Global-settings#dependency-check)
* [Option to enable automatic compilation](Global-settings#automatic-compilation)
* [Option to enable continuous preview of math and TikZ environments](Global-settings#continuous-preview)
* [Option to include the backslash when selecting a LaTeX command](Global-settings#backslash-selection)
* [Option to show LaTeX package files in the structure view](Global-settings#package-structure-view)
* [Option to disable indexing of MiKTeX/TeX Live package files](Global-settings#external-index)
* [Option to enable smart quote substitution](Global-settings#smart-quotes)

#### Conventions
These settings can be found in menu:File[Settings > Languages & Frameworks > TeXiFy > Conventions] and allow you to configure Latex code conventions that apply either globally or for the current project.

* [The maximum number of characters in a section before TeXiFy will suggest to move the section to another file](Conventions#maximum-section-size)
* [Which commands and environments should have a label and which prefix the label should have](Conventions#label-conventions)

### LaTeX SDK

* [Custom location of LaTeX installation](Latex-Sdk)

### Run configuration settings

See [Run configurations settings](Run-configurations#Run-configuration-settings) for more info.

* Choose compiler
* Custom compiler path
* Custom compiler arguments
* Custom environment variables
* (Windows) Choose a custom path to SumatraPDF
* Choose pdf viewer
* Custom pdf viewer
* Choose LaTeX source file to compile
* (MiKTeX only) Set a custom path for auxiliary files
* Set a custom path for output files
* Option to always compile documents twice
* Choose output format
* Choose LaTeX distribution
* Choose BibTeX run configuration
* Choose Makeindex run configuration
* Other tasks to run before the run configuration, including other run configurations or external tools

### Code style settings

These settings can be found in menu:File[Settings > Editor > Code Style > LaTeX (or BibTeX)].

#### Common code style settings for LaTeX and BibTex

* [Specify the number of spaces to use for indentation](Code-style-settings#indent-size)
* [Option to hard wrap LaTeX and BibTeX files](Code-style-settings#hard-wrap)

#### LaTeX specific code style settings

* [Option to start a comment at the same indentation as normal text](Code-style-settings#indent-comment)
* [Specify the number of blank lines before a sectioning command](Code-style-settings#section-newlines)
* [Indent text in sections](Code-style-settings#section-indentation)
* [Option to disable indentation of the document environment](Code-style-settings#indent-document-environment)

## Menu entries

If any shortcut is assigned to a menu entry, it will be shown next to it.

* **menu:File[New > LaTeX File]**\
Create a new LaTeX file of type Source (`.tex`), Bibliography(`.bib`), Package (`.sty`), Document class (`.cls`) or TikZ (`.tikz`)
* **menu:File[Other Settings > Run configuration Templates for New Projects]**\
[Change the run configuration template](Run-configurations)
* **menu:Edit[LaTeX > Sectioning]**\
Insert sectioning commands like `\part` or `\subsection`. If any text is selected, it will be used as argument to the command.
* **menu:Edit[LaTeX > Font Style]**\
Insert font style commands like `\textbf` for bold face. If any text is selected, it will be used as argument to the command.
* **menu:Edit[LaTeX > [Insert Table...](Menu-entries#table-creation-wizard)]**\
Displays a table creation wizard that generates a LaTeX table.
* **menu:Edit[LaTeX > [Insert Graphic...](Menu-entries#insert-graphic-wizard)]**\
Displays a wizard that generates graphic inclusion LaTeX.

* **menu:Edit[LaTeX > Toggle Star]**\
Toggle the star of a command.
* **menu:Edit[Fill Paragraph]**\
Fill the paragraph that is currently under the cursor such that each line is filled until the right margin, but does not exceed it.
* **menu:Code[Reformat File with Latexindent]**\
[Run Latexindent.pl on the LaTeX file the caret is in.](Code-formatting#latexindent)
* **menu:Code[Reformat File with bibtex-tidy]**\
[Run bibtex-tidy on the file the caret is in.](Code-formatting#bibtex-tidy)
* **menu:Analyze[Code > Word Count]**\
[Word counting tool](Menu-entries#_word_counting_tool).
* **menu:Tools[LaTeX > Equation Preview]**\
Preview equations.
* **menu:Tools[LaTeX > TikZ Preview]**\
Preview TikZ pictures.
* **menu:Tools[LaTeX > [Clear Auxiliary Files](Menu-entries#clear-aux-files)]**\
Clear the generated auxiliary files.
* **menu:Tools[LaTeX > [Clear Generated Files](Menu-entries#clear-generated-files)]**\
Clear all generated files.
* **menu:Tools[LaTeX > SumatraPDF]**\
(Windows only) Forward search and configuration of inverse search

### Context menu entries

* **menu:Right-click on any file[New > LaTeX File]**\
Create a new LaTeX file.
* **menu:Right-click on LaTeX source file[Run 'filename']**\
Compiles the file.

## Inspections

* [Inspection suppression](Inspection-suppression)
* [Creating Custom Inspections](https://www.jetbrains.com/help/idea/creating-custom-inspections.html)

### BibTeX
* [Duplicate ID](BibTeX-inspections#Duplicate-ID)
* [Missing bibliography style](BibTeX-inspections#Missing-bibliography-style)
* [Duplicate bibliography style commands](BibTeX-inspections#Duplicate-bibliography-style)
* [Same bibliography is included multiple times](BibTeX-inspections#Same-bibliography-is-included-multiple-times)
* [Bib entry is not used](BibTex-inspections#Bib-entry-is-not-used)

### LaTeX

If you see a minor issue, like some missing metadata about commands or environments, you are encouraged to check if you can [fix it yourself](Contributing-to-TeXiFy#editing-magic).

#### Typesetting issues
Issues which have influence on the typeset result.

* [Nesting of sectioning commands](Typesetting-issues#Nesting-of-sectioning-commands)
* [Collapse cite commands](Typesetting-issues#Collapse-cite-commands)
* [En dash in number ranges](Typesetting-issues#en-dash)
* [Use of `.` instead of `\cdot`](Typesetting-issues#dot)
* [Use of `x` instead of `\times`](Typesetting-issues#times)
* [Vertically uncentered colon: use of raw `:=` instead of `\coloneqq` by mathtools (and variants)](Typesetting-issues#vertically-uncentered-colon)
* [Insert `\qedhere` in trailing displaymath environment](Typesetting-issues#qedhere)
* [Dotless versions of i and j must be used with diacritics](Typesetting-issues#dotless-i)
* [Enclose high commands with `\leftX..\rightX`](Typesetting-issues#high-commands)
* [Citations must be placed before interpunction](Typesetting-issues#citation-before-interpunction)
* [Incorrectly typeset quotation marks](Typesetting-issues#incorrect-quotes)
* [Issues reported by the external Textidote linter](Typesetting-issues#Textidote)

##### Spacing
Typesetting issues related to incorrect spacing.

* [Non-escaped common math operators](Typesetting-issues#non-escaped-common-math-operators)
* [Non-breaking spaces before references](Typesetting-issues#non-breaking-spaces-before-references)
* [Ellipsis with `...` instead of `\ldots` or `\dots`](Typesetting-issues#ellipsis)
* [Normal space after abbreviation](Typesetting-issues#normal-space-after-abbreviation)
* [End-of-sentence space after sentences ending with capitals](Typesetting-issues#end-of-sentence-space-after-capitals)
* [Use the matching amssymb symbol for extreme inequalities](Typesetting-issues#extreme-inequalities)

#### Code style issues
Issues which do not have influence on the typeset result but improve maintainability.

* [Math functions in `\text`](Code-style-issues#math-functions-in-text)
* [Grouped superscript and subscript](Code-style-issues#grouped-superscript-and-subscript)
* [Gather equations](Code-style-issues#Gather-equations)
* [Figure not referenced](Code-style-issues#Figure-not-referenced)
* [Missing labels](Code-style-issues#Missing-labels)
* [Label conventions](Code-style-issues#Label-conventions)
* [Start sentences on a new line](Code-style-issues#Start-sentences-on-a-new-line)
* [Use `\eqref{...}` instead of `(\ref{...})`](Code-style-issues#ins:eqref)
* [Use `\RequirePackage{...}` instead of `\usepackage{...}`](Code-style-issues#ins:requirepackage)
* [File that contains a document environment should contain a `\documentclass` command](Code-style-issues#ins:documentclass)
* [Might break TeXiFy functionality](Code-style-issues#Might-break-TeXiFy-functionality)
* [Too large section](Code-style-issues#too-large-section)

#### Probable bugs
Issues which indicate probable unintended behaviour and often highlight possible compilation errors.

* [Unsupported Unicode character](Probable-bugs#Unsupported-Unicode-character)
* link:++Probable-bugs#File argument should not include the extension++[File argument should not include the extension]
* link:++Probable-bugs#File argument should include the extension++[File argument should include the extension]
* [Missing documentclass](Probable-bugs#Missing-documentclass)
* [Missing document environment](Probable-bugs#Missing-document-environment)
* [Unresolved references](Probable-bugs#Unresolved-references)
* [Non matching environment commands](Probable-bugs#Non-matching-environment-commands)
* [Open if-then-else control sequence](Probable-bugs#Open-if-then-else-control-sequence)
* [File not found](Probable-bugs#File-not-found)
* [Absolute path not allowed](Probable-bugs#Absolute-path-not-allowed)
* [Inclusion loops](Probable-bugs#Inclusion-loops)
* [Nested includes](Probable-bugs#Nested-includes)
* [Label is before caption](Probable-bugs#label-is-before-caption)
* [Unescaped `#` symbol](Probable-bugs#unescaped--symbol#)
* [Multiple \graphicspath definitions](Probable-bugs#Multiple-graphicspath)
* [Relative path to parent is not allowed when using BIBINPUTS](Probable-bugs#bibinputs-relative-path)
* [Command is not defined anywhere](Probable-bugs#undefined-command)

##### Packages
Probable bugs related to packages.

* [Package could not be found](Probable-bugs#Package-could-not-be-found)
* [Package is not installed](Probable-bugs#Package-not-installed)
* [Package name does not match file name](Probable-bugs#Package-name-does-not-match-file-name)
* [Package name does not contain the correct path](Probable-bugs#Package-name-does-not-contain-the-correct-path)
* [Missing imports](Probable-bugs#Missing-imports)

#### Redundant LaTeX
Warns for redundant code.

* [Redundant escape when Unicode is enabled](Redundant-LaTeX#redundant-escape-when-unicode-is-enabled)
* [Redundant use of `\par`](Redundant-LaTeX#redundant-use-of-par)
* [Unnecessary whitespace in section commands](Redundant-LaTeX#unnecessary-whitespace-in-section-commands)
* [Command is already defined](Redundant-LaTeX)
* [Duplicate labels](Redundant-LaTeX)
* [Package has been imported multiple times](Redundant-LaTeX)
* [Duplicate command definitions](Redundant-LaTeX)

#### Discouraged use of TeX or obsolete LaTeX
Issues related to code maturity and use of deprecated constructs.

* [Use of `\over` discouraged](Code-maturity#over)
* [TeX styling primitives usage is discouraged](Code-maturity#styling-primitives)
* [Discouraged use of `\def` and `\let`](Code-maturity#def)
* [Avoid `eqnarray`](Code-maturity#ins:avoid-eqnarray)
* [Discouraged use of primitive TeX display math](Code-maturity#primitive-display-math)
* [Discouraged use of `\makeatletter` in tex sources](Code-maturity#makeatletter)

## Intentions

### LaTeX

See [Intentions](Intentions).

* Add label
* Toggle inline/display math mode
* Insert comments to disable the formatter
* Change to `\left..\right`
* Convert to other math environment
* Move section contents to separate file
* Move selection contents to separate file
* Split into multiple `\usepackage` commands
