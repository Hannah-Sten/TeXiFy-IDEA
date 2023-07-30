## Inspections

### LaTeX

* Spellchecking with custom dictionaries
* [Support for the Grazie grammar and spellchecking plugin](Grazie)

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
