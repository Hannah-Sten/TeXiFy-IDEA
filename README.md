[![Join the chat at https://gitter.im/TeXiFy-IDEA/Lobby](https://img.shields.io/badge/gitter-join%20chat-green.svg)](https://gitter.im/TeXiFy-IDEA)
[![View at JetBrains](https://img.shields.io/jetbrains/plugin/d/9473-texify-idea.svg)](https://plugins.jetbrains.com/plugin/9473-texify-idea)

# TeXiFy-IDEA
LaTeX support for the IntelliJ Platform by [JetBrains](https://www.jetbrains.com/).

No idea where to start? Have a look at the [installation instructions](#installation-instructions). Otherwise, take a look at the [tips](#tips) instead.

Create the most beautiful LaTeX documents with the user friendliness of the IntelliJ platform.
This plugin adds the tools to make creating LaTeX documents a breeze. We are currently doing our best to develop the plugin

## Feedback and support
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

## Building from source using IntelliJ
* Clone or download the project.
* Make a new project from existing sources, even if you used the option 'new project from version control'.
* Follow the instructions.
* In `TeXiFy-IDEA.iml` (in the root directory) change the module type to `PLUGIN_MODULE`.
* In Project Structure under Project, add a new project SDK, namely an IntelliJ Platform Plugin SDK.
* Select the (by default selected) IntelliJ directory if prompted.
* Under Project Structure - Modules - Plugin Deployment change the resources path to the correct path, `\path\to\TeXiFy-IDEA\resources`.
* Add a new run configuration of type Plugin (if you cannot make a new run configuration, restart some stuff or wait a bit).
* Mark the `resources` folder as resources root by right-clicking on it and selecting Mark Directory As - Resources, otherwise it won't find the great icons.
* Go to Project Structure - Libraries and hit the plus icon in the middle column. Select `Java`. Select `lib/pretty-tools-JDDE-2.1.0.jar`.
* Install [SumatraPDF](https://www.sumatrapdfreader.org/download-free-pdf-viewer.html) if you want to use Go To Line in PDF/Source. 

#### To run directly from source
* Note: sometimes this way will not work and will generate strange errors. In that case, see below to build the plugin to use it directly in IntelliJ.
* Find out the location of your IntelliJ Sandbox by going to Project Structure - SDKs - IntelliJ IDEA ... - Sandbox Home, and copy the two `.dll`s from `path/to/TeXiFy/project/lib/` to `path/to/sandbox/plugins/TeXiFy-IDEA/lib/`.
* Run in debug mode, normal run may not work.
* The first time it will look like you are installing a new IntelliJ - don't worry, just click through it.
* Use the option LaTeX - SumatraPDF - Configure Inverse Search to enable the option to go directly to the right line in your source file when you double-click in the pdf.
* To make a new project but also to open existing `.tex` files, use New Project - LaTeX.
* Compile a `.tex` file by clicking on the gutter icon next to `\begin{document}` or create a custom run configuration using the drop-down menu.
#### To build the plugin for use in IntelliJ
* Use Build - Prepare Plugin Module ...
* Add the two `.dll`s from `lib/pretty-tools-JDDE-2.1.0/` to the compiled plugin by unzipping, copying the files to the `lib` directory and zipping.
* Add the plugin to IntelliJ using Settings - Plugins - Install plugin from disk.
* Use the option LaTeX - SumatraPDF - Configure Inverse Search to enable the option to go directly to the right line in your source file when you double-click in the pdf.


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

## FAQ

#### SumatraPDF inverse search: _Error launching IDEA. No JVM installation found_
* Please make sure you have a 32-bit JDK installed. This solved the issue before ([#104](https://github.com/Ruben-Sten/TeXiFy-IDEA/issues/104)). If installing a 32-bit JDK is resolving the problem for you, please report this on the issue tracker.

#### `Error: java: package com.google.common.base does not exist`

* Update IntelliJ (help - check for updates).
* Update your IntelliJ SDK: go to Project Structure - SDKs.
* Hit the plus in the middle column and select IntelliJ Platform Plugin SDK.
* Select your IntelliJ installation directory (e.g. `C:\Program Files (x86)\JetBrains\IntelliJ IDEA xxxx.x`). 
* Remove your old SDK. It is called 'IntelliJ IDEA IU-xxx' where `xxx` is anything but the highest number. 
* Go to Project Structure - Project and select the new SDK.
