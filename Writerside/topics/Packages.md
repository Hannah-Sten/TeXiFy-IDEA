# Packages


## Package could not be found
When using `\usepackage` or `\RequirePackage`, we check if the package exists.
To do this, we check the project for use defined packages, and we check a list of all available CTAN packages.
This list of CTAN packages is manually updated by us, and might be out of date.

## Package is not installed
_Since b0.6.9_

This inspection is for TeX Live only, MiKTeX automatically installs packages on the fly.
When using `\usepackage` or `\RequirePackage`, TeXiFy checks if the packages is installed.
If it isn’t installed, it provides a quick fix to install the package.

## Package update available
_Since b0.9.10_

When a package has an update available on CTAN, this inspection will provide a quickfix to update the package.
Currently, it only works when tlmgr (TeX Live manager) is installed.
The list of available package updates is cached until IntelliJ is restarted or the quickfix is used.

## Package name does not match file name
_Since b0.6.10_

The package name given in a `\ProvidesPackage` command does not match the name of the `sty` file the command is in.
This inspection provides a quick fix to fix the package name.

## Package name does not contain the correct path
_Since b0.6.10_

The package name given in a `\ProvidesPackage` command does not contain the correct path of the `sty` file.
This package name should include the path relative to the main file where the package is used.
This inspection provides a quick fix to fix the path in the package name.

## Missing imports

For some common commands TeXiFy knows which package is needed to use them.
A quickfix is available to add the missing `\usepackage` or `\RequirePackage` command.
For more information, see [Automagically import packages of common commands](Editing-a-LaTeX-file.md#automatic-package-importing).

Note that if TeXiFy complains incorrectly that you did not import the package, you may need to tell TeXiFy which file is your root file, see [Magic comments](Editing-a-LaTeX-file.md#magic-comments).
It could also be that you installed TeX Live via your package manager instead of via the official installer.
In general it is better (and easier for TeXiFy) to install the official distribution, see [Installation](Installation-guide.md#installing-tex-live).