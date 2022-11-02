## Beta 0.7.24


### Additions
* Add support for IDEA 2022.3. (#2683, #2682)
* Also search latexmkrc files for additions to TEXINPUTS. (#2700)
* Stop parsing LaTeX after \endinput. (#2702)
* Parse urls in bibtex as verbatim text. (#2703)
* Add autocompletion support for commands with multiple label reference parameters. (#2705)
* Add support for Textidote. (#2707)
* Exit inline math with tab. (#2709)

### Bug fixes
* Fix SumatraPDF detection for portable installation in PATH. (#2732)
* Fix false positive Grazie inspection on parentheses. (#2692)
* Fix missing parent sections in structure view when starting with a sublevel. (#2693)
* Fix math environment check for in particular blkarray. (#2697)
* Fix spellcheck in command parameters. (#2699)
* Fix formatting issue with environment parameters. (#2715)
* Fix ignored file-level inspection suppressions. (#2718)
* Fix TeXiFy shortcuts being applied in non-LaTeX files. (#2718)
* Fix inline math in required parameters after angular bracket parameter. (#2728)
* Fix crashes. (#2708)

Thanks to @MisterDeenis for contributing to this release!
