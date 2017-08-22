# TeXiFy-IDEA
LaTeX support for the IntelliJ Platform by [JetBrains](https://www.jetbrains.com/).

Create the most beautiful LaTeX documents with the user friendliness of the IntelliJ platform.
This plugin adds the tools to make creating LaTeX documents a breeze. We are currently doing our best to develop the plugin.
Feel free to share your ideas and contributions with us.
Please bear in mind that this is just a side project for us.

For live chat, visit the [gitter.im chat room](https://gitter.im/TeXiFy-IDEA/Lobby).

# Features

## Currently supported

### Run configurations
* Multiple 'compile' (run) configurations.
* Supported compilers: `pdfLaTeX`
* Seperate auxiliary files from output (only supported for `MiKTeX`).
* Dynamically detect what runtime configuration to use based on the active file.
* Support for `PDF` and `DVI` output.

### Editor
* Autocompletion form a predefined list of commands.
* Commands defined in the project will also be added to the autocompletion list.
* Autocompletion of defined labels (using `\label`).
* Autocompletion for file names.
* Brace matching for `{}`, `[]`, `\[\]` and `$$`.
* Automatically inserts other half of `{}`, `[]`, `\[\]` and `$$`.
* Most math commands get replaced by their unicode representation using folding.
* Gutter icon to navigate to included files.
* Gutter icon to automatically compile the active file.
* Comment out lines.
* Code folding for environments.
* Go to declaration of labels.
* Toggle star in commands.
* Automatically includes packages for registered commands (in TeXiFy, e.g. `ulem` for `\sout`)

### Syntax highlighting
Braces, 
Brackets, 
Optional parameters, 
Commands, 
Commands in inline math mode, 
Commands in display math mode, 
Comments, 
Inline math, 
Display math, 
and Stars.

### Structure view
* Shows sectioning, inclusions, labels and command definitions.
* Items can be sorted.
* Filters to add/remove items from view.
* Section numbering behind section items that takes `\setcounter` and `\addtocounter` into account.
* Included files also show their item tree in the overview.
* Updates automatically during editing.

### Inspections
Most inspections come with quick fixes.

* Integration with the default IntelliJ spell checker. 
* Already defined commands
* Discouraged use of `\def` and `\let`
* Duplicate labels
* Label conventions (`sec:`, `fig:` etc.)
* Breaking TeXiFy IDEA functionality
* Missing document environment
* Missing `\documentclass`
* Missing imports
* Missing labels on `\section`, `\subsection` and `\chapter`
* Non-breaking spaces before references
* Non-escaped common math operators
* Redundant escapes when unicode is enabled
* Start sentences on a new line
* Discouraged TeX styling primitive usage
* Too large sections: move to another file
* Unresolved references
* Unsupported Unicode characters
* Use of `\over` discouraged

### PDF Viewer
* Offical support for SumatraPDF on Windows
* Supports forward and backward search (Windows only)

### User Interface
* Create new `.tex`, `.sty` and `.cls` files from the new file menu.
* Some insertion actions from the LaTeX menu for styling and sections.

### Other
* Word- and character counting tool
* Available file templates for `.tex`, `.sty` and `.cls` files. 
* Many fancy icons with the look and feel of the IntelliJ platform.

## How to build the project using IntelliJ
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

## FAQ

#### `Error: java: package com.google.common.base does not exist`

* Update IntelliJ (help - check for updates).
* Update your IntelliJ SDK: go to Project Structure - SDKs.
* Hit the plus in the middle column and select IntelliJ Platform Plugin SDK.
* Select your IntelliJ installation directory (e.g. `C:\Program Files (x86)\JetBrains\IntelliJ IDEA xxxx.x`). 
* Remove your old SDK. It is called 'IntelliJ IDEA IU-xxx' where `xxx` is anything but the highest number. 
* Go to Project Structure - Project and select the new SDK.
