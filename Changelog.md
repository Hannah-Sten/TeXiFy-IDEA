# Beta 0.7.7

Welcome to TeXiFy IDEA 0.7.7! This release contains some minor bug fixes, and enhanced command execution on Mac.

# Additions
* Update color definition when using color picker. (#1864)
* Improve system command execution on Mac. (#1901)
* Handle quoted custom compiler arguments. (#1902)

# Changes

# Bug fixes
* Fix index out of bounds exception in color values. (#1864)
* Fix grammar mistakes not being checked at the beginning of environments. (#1892)
* Fix parse error when >{ is not used to define table columns. (#1894)
* Add some missing table environments. (#1894)
* Revert loading SafeDeleteFix class from the optional Java plugin. (#1894)
* Fixed other crashes. (#1881, #1879)

Thanks to Felix Berlakovich for contributing to this release.