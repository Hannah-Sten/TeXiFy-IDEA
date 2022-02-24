## Beta 0.7.15

### Additions
* Add conventions settings, by @fberlakovich. (#1917)
* Recognise custom environments as defining a tabular-like environment. (#2221)
* Add some missing biblatex entry types to autocompletion. (#2228)
* Add inspection which checks if all commands are defined somewhere. (#2229)
* Improve package indexing and autocompletion. (#2232)

### Bug fixes
* Many improvements in log error parsing. (#2227)
* Update Grazie implementation. (#1951)
* Fix false positive in File Not Found inspection. (#2240)
* Resolve to command definition inside definition when not the first parameter. (#2240)
* Ignore nested enumerations when finding the item marker in autocompletion. (#2220)
* Add FILE_NAME to the new file properties. (#2218)
* Allow \begin and \end commands in table header column prefix/suffix. (#2218)
* Fix autocompletion filtering for texlive. (#2215)
* Suppress notifications for background SDK checks. (#2215)
* Fix \item being inserted on hard line wrap with IdeaVim installed. (JetBrains/ideavim#468)
* Fix output path in run config template overriding the auxil path. (#2219)
* Fix invalid index access. (#2240)

Thanks to @JeremiasBohn and @fberlakovich for contributing to this release!