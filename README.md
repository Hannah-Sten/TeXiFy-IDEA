![GitHub Workflow Status (branch)](https://img.shields.io/github/workflow/status/Hannah-Sten/TeXiFy-IDEA/CI/master?style=flat-square)
[![Version](https://img.shields.io/jetbrains/plugin/v/9473-texify-idea.svg?style=flat-square)](https://plugins.jetbrains.com/plugin/9473-texify-idea)
[![View at JetBrains](https://img.shields.io/jetbrains/plugin/d/9473-texify-idea.svg?style=flat-square)](https://plugins.jetbrains.com/plugin/9473-texify-idea)
[![Join the chat at https://gitter.im/TeXiFy-IDEA/Lobby](https://img.shields.io/badge/gitter-join%20chat-green.svg?style=flat-square)](https://gitter.im/TeXiFy-IDEA)
[![Donate via PayPal](https://img.shields.io/badge/Donate!-PayPal-orange.png?style=flat-square)](https://www.paypal.me/HannahSchellekens)
[![codecov](https://img.shields.io/codecov/c/github/Hannah-Sten/TeXiFy-IDEA/master?style=flat-square)](https://codecov.io/gh/Hannah-Sten/TeXiFy-IDEA)

# TeXiFy-IDEA
LaTeX support for the IntelliJ Platform by [JetBrains](https://www.jetbrains.com/).

No idea where to start? Have a look at the [installation instructions](#installation-instructions). Otherwise, take a look at the [tips](#tips) instead.

Create the most beautiful LaTeX documents with the user friendliness of the IntelliJ platform.
This plugin adds the tools to make creating LaTeX documents a breeze. We are currently doing our best to develop the plugin.

## Feedback and support
You can share new ideas/feature requests/bugs/calls for help in multiple ways:
1. Live chat via [gitter](https://gitter.im/TeXiFy-IDEA) (you can login with your GitHub account). Gitter also has a nice app, we use it to get notified of new activity.
2. [Issues](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues). These may be bug reports, feature requests, user support, etc. Just generally anything you have a problem with/suggestion for. For general feedback we advice using the gitter.

Please bear in mind that this is just a side project for us. It might take a while to fully process your feedback. We try our best :3

## Contributing
We would love it if you want to contribute to this project!
Please have a look at the [contributing guidelines](CONTRIBUTING.md) to get started.

## Features

* Syntax highlighting
* Autocomplete of labels, (custom defined) commands and environments
* Writer ergonomics - writing LaTeX made less cumbersome
* Compiler support for pdfLaTeX, LuaTeX, Latexmk, texliveonfly, XeLaTeX, bibtex, and biber
* Inspections. Intentions. And heaps more inspections
* Full BibTeX support
* Formatter for LaTeX and BibTeX
* Structure view for LaTeX and BibTeX with filters
* Code folding for imports, sections, and environments
* SumatraPDF (Windows), Evince (Linux), Okular (Linux) and Skim (MacOS) support with forward and backward search
* Smart Quotes
* Unicode math preview
* Equation preview
* Gutter icons for quick compilation and file includes
* Fancy icons that fit in with the IntelliJ style
* Brace matching
* Word counting tool
* File templates for .tex, .sty, .cls and .bib files
* Automagically import packages of common commands
* Go to declaration and find usages for labels, citations, and custom commands
* Renaming of labels, citations, environments and files
* Shortcuts for styling text
* Line commenter
* Support for user-created document classes and packages
* Toggle star action
* Words of encouragement

A more extensive (but not complete) list as well as documentation for these features can be found in the [Wiki](https://github.com/Hannah-Sten/TeXiFy-IDEA/wiki/Features).
You can find IntelliJ documentation at https://www.jetbrains.com/idea/documentation/.

## Installation instructions and getting started

See https://github.com/Hannah-Sten/TeXiFy-IDEA/wiki/Installation

## <a name="equation-preview">Equation preview and TikZ Preview</a>

You can use the Equation Preview by making sure your cursor is in a math environment and clicking the Tools | LaTeX | Preview Equation menu, or using <kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>X</kbd>.
You can use the TikZ Preview by placing your cursor in a `tikzpicture` environment and click the Tools | LaTeX | Preview TikZ Picture, or using <kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>Y</kbd>.
It works by putting your equation or TikZ picture in a new temporary (fairly minimal) document and compiling that, so custom commands and packages from your document will not be taken into account, but it ensures the preview is really fast.
The TikZ Preview will take TikZ and pgf libraries into account.

The current implementation of the Equation Preview was contributed by Sergei Izmailov and requires external dependencies, for which installation instructions follow.

### Instructions for Linux

* Install Inkscape from [inkscape.org/release](https://inkscape.org/release).
* Install the `pdf2svg` package for your distro, for example on Ubuntu with `sudo apt-get install pdf2svg` or on Arch Linux with `sudo pacman -S pdf2svg`.

### Instructions for Windows
* Install Inkscape from [inkscape.org/release](https://inkscape.org/release), suppose you install it in `C:\Program Files\Inkscape`.
* Install pdf2svg from [github.com/textext/pdf2svg/releases](https://github.com/textext/pdf2svg/releases), suppose you install it in `C:\Program Files\pdf2svg`.
* Add both `C:\Program Files\Inkscape` and `C:\Program Files\pdf2svg` to your PATH environment variable, for example by searching for Environment Variables on your computer, clicking 'Edit the system environment variables', clicking 'Environment Variables', and under System variables find the one named Path, edit it and insert the paths here. Make sure the paths are separated by a `;`.
* Reboot your system.

## Alpha channel: subscribing to the very latest features

This plugin also has an alpha channel besides the default stable channel.
The alpha channel contains the latest build with the latest features available, and is updated much more frequently than the stable channel.
It is used for testing features before they are released in the stable channel, so alpha versions of the plugin may be more unstable.

#### Subscribing to the alpha channel

More detailed information is at https://www.jetbrains.com/help/idea/managing-plugins.html#repos but we will quickly summarize the steps.
* Uninstall the plugin
* Subscribe to the alpha channel by going to Settings | Plugins | gear icon | Manage Plugin Repositories | plus icon, then use the url https://plugins.jetbrains.com/plugins/alpha/list
* Install the plugin by going to Marketplace and searching for `TeXiFy-IDEA`, you should see the version next to the name is the alpha version.

## <a name="build-from-source">Building from source using IntelliJ</a>

#### I know what I'm doing

* This project uses gradle. Make a new project from existing sources and import the project. Done.

#### I have no idea what I'm doing

It is assumed that git, IntelliJ, java and LaTeX are installed. If not, try the normal [installation instructions](#installation-instructions) first.
* Make a new project from version control if you don't have it yet downloaded, or from existing sources if you have.
* On the GitHub [home page](https://github.com/Hannah-Sten/TeXiFy-IDEA) of TeXiFy click 'clone or download' and copy the url to Git Repository Url.
* If the project opens and you get a popup 'Import Gradle project', click that.
* If you are prompted to open the `build.gradle` file, do so.
* Select 'Use auto-import'.
* Thank Gradle that you're done now!
* Check that in Settings - Build, Execution, Deployment - Compiler - Kotlin Compiler the Target JVM version is set correctly, currently it should be 1.8. If you encounter an error like `Kotlin: Cannot inline bytecode built with JVM target 1.8 into bytecode that is being built with JVM target 1.6.` when building, you need to look here.
* Test it worked by executing the 'build' task in Tasks - build - build.
* You can ignore deprecation warnings in the build output.
* If something doesn't work, try looking at the [FAQ](#FAQ) first.

#### To run directly from source
* Click the Gradle button on the right, the gradle task is located in Tasks - intellj - runIde. Right-click and run.
* If at some time you cannot use this and you need to run from command line, use `gradlew runIde`.
* Note how IntelliJ adds this task as a run configuration in the normal location if you have run it once, so you can use that one the next time.
* The first time it will look like you are installing a new IntelliJ - don't worry, just click through it.
* You can also debug against other IDEs. At the moment only PyCharm is set up, but it is easy to add others. You can use it by specifying the argument `-PusePycharm=true` in your runIde run configuration.
* Use the option Tools - LaTeX - SumatraPDF - Configure Inverse Search to enable the option to go directly to the right line in your source file when you double-click in the pdf.
* To make a new project but also to open existing `.tex` files, use New Project - LaTeX.
* Compile a `.tex` file by clicking on the gutter icon next to `\begin{document}` or create a custom run configuration using the drop-down menu.

#### To build a zip which contains the plugin
* Click the Gradle button on the right, the gradle task is located in Tasks - intellij - buildPlugin. Right-click and run. The zip will be in build/distributions.
* Add the plugin to IntelliJ using Settings - Plugins - Install plugin from disk.
* Use the option LaTeX - SumatraPDF - Configure Inverse Search to enable the option to go directly to the right line in your source file when you double-click in the pdf.

#### To run tests
* Click the Gradle button on the right, the gradle task is located in Tasks - verification - check. Right-click and run. Note that check includes test so it will run the tests.

## <a name="FAQ">FAQ</a>

#### What should my document structure look like?

In general you have a main file which contains the documentclass and the document environment (the `\begin{document}` and `\end{document}`).
From here you can include other files which can then include even more files and so on.
An example is:

```latex
\documentclass{exam}

% Packages
\usepackage{amsthm}

\author{L.A.\ TeX}
\title{Example document setup}

% Possible other definitions, configurations etc. that you need
\theoremstyle{definition}
\newtheorem{theorem}{Theorem}

% Document
\begin{document}

    \maketitle

    \section{Introduction}\label{sec:introduction}
    \input{introduction}

    \section{Example theorems} \label{sec:exampleTheorem}
    \input{example-theorems}

\end{document}
```

where the files `introduction.tex` and `example-theorems.tex` contain just the content, for example these could be the complete file contents of `introduction.tex`:

```latex
\begin{theorem}
    If the meanings of 'true' and 'false' were switched, then this sentence wouldn't be false.
\end{theorem}
```

#### How can I set-up shortcuts for e.g. `\emph{}` or the itemize environment?

You can define a live template via File -> Settings... -> Editor -> Live Templates. For example, for the `itemize` environment, you could use the following template:

```tex
\begin{itemize}
    \item $PARM1$
\end{itemize}
```

Set the template to be applicable in LaTeX files.

![Screenshot of live template UI, showing the template text above along with an abbreviation of itemize and a description of 'Add itemize env'. The macro is set to be 'Applicable in LaTeX' and expands with the Tab key.](doc/macro.png)

Once the live template is created, close the Settings dialog. Use Edit -> Macros -> Start Macro Recording and enter the live template abbreviation. Finish recording the macro, and name it. Via Settings -> Keymap, assign the macro a key binding such as <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>Shift</kbd>+<kbd>I</kbd>.

Now, you can use the macro key binding and hit <kbd>Enter</kbd> to insert a new `itemize` environment with an item. The cursor will automatically move to the first `\item`.

For commands, you can define templates for e.g. `\emph{$PARM1$}`.

#### The Equation Preview does not work

Make sure you have installed the dependencies, instructions are in the [Equation Preview](#equation-preview) section.

#### makeindex or bibtex not writing to file (`openout_any = p`)
When you get during compilation the error
```
makeindex: Not writing to /path/to/project/latexfile.ind (openout_any = p).
Can't create output index file /path/to/project/latexfile.ind.
```
or
```
bibtex: Not writing to ../out/latexfile.blg (openout_any = p).
```
this probably happens because you are trying to use makeindex or bibtex with a separate output directory. You should either disable the out directory in the run config or change the security setting in `texmf.cnf`, see [this tex.stackexchange.com answer](https://tex.stackexchange.com/questions/12686/how-do-i-run-bibtex-after-using-the-output-directory-flag-with-pdflatex-when-f/289336#289336).

#### How to integrate a latex compiler other than pdflatex:
Most tex distributions contain a commandline tool called [arara](https://github.com/cereda/arara).
Arara uses a small configuration in your main *.tex file

For example:

     % arara: lualatex: {shell: yes,  action: nonstopmode}
     % arara: biber
     % arara: lualatex: {shell: yes,  action: nonstopmode}

when you add these small code snippets and call

    arara -v yourFavoritTexFileHere.tex

Arara calls lualatex biber and lualatex again. Any Jetbrains IDE allows you to add something they call "tools". There you can add the arara call and add a shortcut.
Arara pipes the full output from all subsequent commands, which is not all the time usefull to find errors. So I put the arara call into a small bash script and add a grep filter.

    #!/usr/bin/env bash

    arara thesis.tex -v | grep -C 1 -E "((E|e)rror|ERROR|SUCCESS|FAILURE|Undefined control sequence)"

##### Alternative for integrating a latex compiler other than pdflatex:
In your run configuration, tick the box `select custom compiler executable path (required on Mac OS X)`.
You can now provide the path to any latex compiler. 
When using Windows and MiKTeX, these executables are located in 

    C:\Program Files\MiKTeX 2.9\miktex\bin\x64

or (in some cases)

    C:\Users\user\AppData\Local\Programs\MiKTeX 2.9\miktex\bin\x64
    
For example, to use lualatex:

    C:\Program Files\MiKTeX 2.9\miktex\bin\x64\lualatex.exe
    
You can run this run configuration by pressing <kbd>Shift</kbd>+<kbd>F10</kbd> (on Windows), by clicking the play button, or by clicking the gutter icon.

#### `Gtk-WARNING **: Unable to locate theme engine in module_path: "murrine"`

If you get this warning, it is not critical so you could ignore it but to solve it you can install the mentioned gtk engine, in this case Murrine.
For example on Arch Linux, install the `gtk-engine-murrine` package. Arch Linux sets the default theme to Adwaita, so install that with the `gnome-themes-extra` package.
For more information see [wiki.archlinux.org](https://wiki.archlinux.org/index.php/GTK+).

#### `Unable to find method 'sun.misc.Unsafe.defineClass'` or `Please provide the path to the Android SDK` when syncing Gradle

This probably means your Gradle cache is corrupt, delete (on Windows) `C:\Users\username\.gradle\caches` and `C:\Users\username\.gradle\wrapper\dists` or (on Linux) `~/.gradle/caches` and `~/.gradle/wrapper/dists`, then reboot your system.

#### `Error: java: package com.google.common.base does not exist`

* Update IntelliJ (help - check for updates).
* Update your IntelliJ SDK: go to Project Structure - SDKs.
* Hit the plus in the middle column and select IntelliJ Platform Plugin SDK.
* Select your IntelliJ installation directory (e.g. `C:\Program Files (x86)\JetBrains\IntelliJ IDEA xxxx.x`). 
* Remove your old SDK. It is called 'IntelliJ IDEA IU-xxx' where `xxx` is anything but the highest number. 
* Go to Project Structure - Project and select the new SDK.
