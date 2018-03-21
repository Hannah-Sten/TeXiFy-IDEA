[![Build Status](https://travis-ci.org/Ruben-Sten/TeXiFy-IDEA.svg?branch=master)](https://travis-ci.org/Ruben-Sten/TeXiFy-IDEA)
[![Join the chat at https://gitter.im/TeXiFy-IDEA/Lobby](https://img.shields.io/badge/gitter-join%20chat-green.svg)](https://gitter.im/TeXiFy-IDEA)
[![View at JetBrains](https://img.shields.io/jetbrains/plugin/d/9473-texify-idea.svg)](https://plugins.jetbrains.com/plugin/9473-texify-idea)

# TeXiFy-IDEA
LaTeX support for the IntelliJ Platform by [JetBrains](https://www.jetbrains.com/).

No idea where to start? Have a look at the [installation instructions](#installation-instructions). Otherwise, take a look at the [tips](#tips) instead.

Create the most beautiful LaTeX documents with the user friendliness of the IntelliJ platform.
This plugin adds the tools to make creating LaTeX documents a breeze. We are currently doing our best to develop the plugin

## Feedback and support
***We are currently extremely busy IRL, so there might be some delay in support and development.***

You can share new ideas/feature requests/bugs/calls for help in multiple ways:
1. Live chat via [gitter](https://gitter.im/TeXiFy-IDEA) (you can login with your GitHub account). Gitter also has a nice app, we use it to get notified of new activity.
2. [Issues](https://github.com/Ruben-Sten/TeXiFy-IDEA/issues). These may be bug reports, feature requests, user support, etc. Just generally anything you have a problem with/suggestion for. For general feedback we advice using the gitter.

Please bear in mind that this is just a side project for us. It might take a while to fully process your feedback. We try our best :3

## Contributing
We would love it if you want to contribute to this project!
Please have a look at the [contributing guidelines](CONTRIBUTING.md) to get started.

## Features

* Syntax highlighting
* Autocomplete of labels, (custom defined) commands and environments
* Writer ergonomics - writing LaTeX made less cumbersome
* Run configurations for LaTeX and BibTeX (pdfLaTeX, bibtex)
* Inspections. Intentions. And heaps more inspections.
* Full BibTeX support
* Formatter for LaTeX and BibTeX
* Structure view for LaTeX and BibTeX with filters
* Code folding for imports, sections, and environments
* SumatraPDF support with forward and backward search
* Unicode math preview
* Gutter icons for quick compilation and file includes
* Fancy icons that fit in with the IntelliJ style
* Brace matching
* Word counting tool
* File templates for .tex, .sty, .cls and .bib files
* Automagically import packages of common commands
* Go to declaration of labels
* Shortcuts for styling text
* Line commenter
* Support for user-created document classes and packages
* Toggle star action
* Words of encouragement

We could make a detailed list, but that would take up your whole screen! We might add one later though :)

## <a name="build-from-source">Building from source using IntelliJ</a>

#### I know what I'm doing

* This project uses gradle. Make a new project from existing sources and import the project. Done.

#### I have no idea what I'm doing

It is assumed that git, IntelliJ, java and LaTeX are installed. If not, try the normal [installation instructions](#installation-instructions) first.
* Make a new project from version control if you don't have it yet downloaded, or from existing sources if you have.
* On the GitHub [home page](https://github.com/Ruben-Sten/TeXiFy-IDEA) of TeXiFy click 'clone or download' and copy the url to Git Repository Url.
* If the project opens and you get a popup 'Import Gradle project', click that.
* If you are prompted to open the `build.gradle` file, do so.
* Select 'Use auto-import'.
* Thank Gradle that you're done now!
* Install [SumatraPDF](https://www.sumatrapdfreader.org/download-free-pdf-viewer.html) if you want to use Go To Line in PDF/Source. 
* Check that in Settings - Build, Execution, Deployment - Compiler - Kotlin Compiler the Target JVM version is set correctly, currently it should be 1.8. If you encounter an error like `Kotlin: Cannot inline bytecode built with JVM target 1.8 into bytecode that is being built with JVM target 1.6.` when building, you need to look here.

#### To run directly from source
* Click the Gradle button on the right, the gradle task is located in Tasks - intellj - runIde. Right-click and run.
* If at some time you cannot use this and you need to run from command line, use `gradlew runIde`.
* Note how IntelliJ adds this task as a run configuration in the normal location if you have run it once, so you can use that one the next time.
* The first time it will look like you are installing a new IntelliJ - don't worry, just click through it.
* Use the option LaTeX - SumatraPDF - Configure Inverse Search to enable the option to go directly to the right line in your source file when you double-click in the pdf.
* To make a new project but also to open existing `.tex` files, use New Project - LaTeX.
* Compile a `.tex` file by clicking on the gutter icon next to `\begin{document}` or create a custom run configuration using the drop-down menu.

#### To build a zip which contains the plugin
* Click the Gradle button on the right, the gradle task is located in Tasks - other - zip. Right-click and run. The zip will be in build/distributions.
* Add the plugin to IntelliJ using Settings - Plugins - Install plugin from disk.
* Use the option LaTeX - SumatraPDF - Configure Inverse Search to enable the option to go directly to the right line in your source file when you double-click in the pdf.

#### To run tests
* Click the Gradle button on the right, the gradle task is located in Tasks - verification - check. Right-click and run. Note that check includes test so it will run the tests.


## <a name="installation-instructions">Installation instructions</a>
### Installing IntelliJ and the TeXiFy-IDEA plugin
* It is probably a good idea to keep these instructions open as a reference while carrying them out.
* If you don't have the latest version yet, download and install [IntelliJ IDEA](https://www.jetbrains.com/idea/download/), the Community edition is free.
* Download and install [LaTeX for Windows](https://miktex.org/download) or [LaTeX for Mac](https://miktex.org/howto/install-miktex-mac). During installation, choose the option `Install missing packages on the fly: yes`.
* If you're on Windows, download and install [SumatraPDF](https://www.sumatrapdfreader.org/download-free-pdf-viewer.html), you will use it to view your compiled pdf files. If you know you are on a 64-bit system you can download the 64-bit build. If you have no idea, download the normal installer which is the top-most link.
* Open IntelliJ, choose `create new project`, click `empty project` and click next. Save it anywhere because we will not use this project.
* If you see a window named Project Structure, just click ok.
* Go to File - Settings - Plugins, search for `texify`, click `search in repositories` and click `install`.
* Restart your pc to finish the LaTeX and the plugin installations.
* Click on File - New - Project, select LaTeX in the left column and click next. Specify a name for your project, and a location. A project can contain multiple LaTeX files, so for example if you make a new document for each new homework you get, place them all in the same project.
* Double-click or click on the arrow next to your project name on the left to open the directory.
* A standard document is already made for you, you can find it in the `src` folder. This folder will contain all your LaTeX. Double-click on the `.tex` file to open it. If you cannot see any directory structure (the folders on the left side), hit Project (it's written on it's side) on the left of your screen.
* Type some text between the `\begin{document}` and `\end{document}` lines, hit the compile icon next to the `\begin{document}` line and click Run. If you see a pdf appearing, congratulations! 
* If you see `LaTeX error: fily a4.sty not found` then you did not restart your pc. Do it now.
* The next time you can also compile using the Run button (looks like a Play button) in the top-right menu, or using <kbd>Shift</kbd>+<kbd>F10</kbd> on Windows.

#### <a name="tips">Tips</a>
* You never have to remember to save your work, IntelliJ will automatically save every letter you type.
* You can personalise the template that is used when you create a new LaTeX file in settings - editor - file and code templates - LaTeX source.
* You don't need to close the pdf to recompile, it will automatically refresh.
* A good way to start learning LaTeX is by asking someone how to do what you want to do or by Googling "what-I-want-to-do latex".
* But some standard LaTeX commands are available in the LaTeX menu.
* Pay attention to squiggles (wavey lines) under text you typed, they indicate that something is wrong. Hovering over it gives extra information. In some cases, a ready-made fix is waiting to be applied: hit the lightbulb that appears on the left, or hit <kbd>Alt</kbd>+<kbd>Enter</kbd> to view and apply it. A quick overview of useful shortcuts is [below](#installation-shortcuts-overview).
* If your LaTeX indentation (the number of spaces that is in front of each line) looks messy, try to reformat with <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>L</kbd>.
* If you are searching how a particular symbol has to be written in LaTeX, the [Detexify](http://detexify.kirelabs.org/classify.html) tool can probably help you. Just draw your symbol in the `draw here` box and the command will be listed on the right.
* If you want a proper explanation of what LaTeX and its philosophy is about, read the [Not So Short Introduction To LaTeX2e](http://ctan.cs.uu.nl/info/lshort/english/lshort.pdf).

### When you want to work together: install git
* Often you will be working together on one document. In that case, use git to make this go smoothly. We use git because git is awesome, but there exist other tools as well.
* But sometimes you cannot use IntelliJ, for example because you are at school. For that, we use Overleaf.
* Install [git](https://git-scm.com/downloads), during installation just click `next` everywhere.
* Restart your computer.
* Go to [Overleaf](https://www.overleaf.com/signup) and sign up.
* Create a new project, choose a blank template. Possibly give it a useful name.
* Click `Share` and copy the link under `clone with git`.
* Go to IntelliJ and click File - New - Project from VCS - git
* Paste the url here, and choose the parent directory you want.
* If you are asked by IntelliJ `Do you want to add this file to git?`, just click `no`
when you do not recognize the file.
* You can share the Overleaf link under `Read & Edit Link` with your co-authors.

#### To push (upload) changes to Overleaf
* Hit <kbd>Ctrl</kbd>+<kbd>K</kbd> to commit changes to git.
* Specify a commit message.
* Click `commit and push` by hovering over the `commit` button.
* If your git username is asked, specify it.
* Click `push`.

#### To pull (download) changes from Overleaf
* Hit <kbd>Ctrl</kbd>+<kbd>T</kbd>.

### I want to know more about git
* That's great! Because git and similar tools are used everywhere by programmers to collaborate, it's not just for LaTeX and doesn't work just with Overleaf.
* [GitHub](https://github.com/), the site you are now looking at, is used a lot to store code online and collaborate using the same push and pull mechanism as you used all the time, but now to GitHub instead of Overleaf. After you made a new repository, click `Clone or download` to get a git link, and then do the same steps as you did with the Overleaf link.
* If you want to know more, a great git tutorial is at [learngitbranching.js.org](http://learngitbranching.js.org/).
* Want to know even more of advanced use of git? Read the excellent [Pro Git book](https://git-scm.com/book/en/v2) for free.

### <a name="installation-shortcuts-overview"></a> Overview of some useful IntelliJ shortcuts
* Double <kbd>Shift</kbd> Search for any IntelliJ command, like Reformat.
* <kbd>Alt</kbd>+<kbd>Enter</kbd> View the quick fix, if there is one. Apply the fix with <kbd>Enter</kbd>.
* <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>L</kbd> Reformat your LaTeX.
* <kbd>Ctrl</kbd>+<kbd>K</kbd> Commit and push changes.
* <kbd>Ctrl</kbd>+<kbd>T</kbd> Pull changes.


Any suggestions for improvements of the installation instructions, however small? Please let us know at [gitter](https://gitter.im/TeXiFy-IDEA)!

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

#### SumatraPDF inverse search: _Error launching IDEA. No JVM installation found_
* Please make sure you have a 32-bit JDK installed. This solved the issue before ([#104](https://github.com/Ruben-Sten/TeXiFy-IDEA/issues/104)). If installing a 32-bit JDK is resolving the problem for you, please report this on the issue tracker.


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





#### `Error: java: package com.google.common.base does not exist`

* Update IntelliJ (help - check for updates).
* Update your IntelliJ SDK: go to Project Structure - SDKs.
* Hit the plus in the middle column and select IntelliJ Platform Plugin SDK.
* Select your IntelliJ installation directory (e.g. `C:\Program Files (x86)\JetBrains\IntelliJ IDEA xxxx.x`). 
* Remove your old SDK. It is called 'IntelliJ IDEA IU-xxx' where `xxx` is anything but the highest number. 
* Go to Project Structure - Project and select the new SDK.
