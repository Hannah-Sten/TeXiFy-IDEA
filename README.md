# TeXiFy-IDEA
LaTeX support for the IntelliJ Platform by [JetBrains](https://www.jetbrains.com/).

Create the most beautiful LaTeX documents with the user friendliness of the IntelliJ platform.
This plugin adds the tools to make creating LaTeX documents a breeze. We are currently doing our best to develop the plugin.
Feel free to share your ideas and contributions with us.
Please bear in mind that this is just a side project for us.

# Features

## Currently supported

### Run configurations
* Multiple 'compile' (run) configurations.
* Supported compilers: `pdfLaTeX`
* Seperate auxiliary files from output (only supported for `MiKTeX`).
* Dynamically detect what runtime configuration to use based on the active file.

### Editor
* Basic autocompletion form a predefined list of commands.
* Brace matching for `{}`, `[]`, `\[\]` and `$$`.
* Automatically inserts other half of `{}`, `[]`, `\[\]` and `$$`.
* Most math commands get replaced by their unicode representation using folding.
* Gutter icon to navigate to included files.
* Gutter icon to automatically compile the active file.

### Syntax highlighting
* Braces
* Brackets
* Optional parameters
* Commands
* Commands in inline math mode
* Commands in display math mode
* Comments
* Inline math
* Display math
* Stars

### Templates
* Available file templates for `.tex`, `.sty` and `.cls` files. 

### User Interface
* Create new `.tex`, `.sty` and `.cls` files from the new file menu.

### Other
* Fancy icons for `.tex`, `.sty` and `.cls` files (see [GreanTeaFlavouredIcons](https://github.com/RubenSchellekens/GreenTeaFlavouredIcons)), and LaTeX modules.

## In the works
* Document structure view [[structure-view](https://github.com/Ruben-Sten/TeXiFy-IDEA/tree/structure-view)].
* Jumping to source code of packages [[TeXiFy-IDEA-2](https://github.com/Ruben-Sten/TeXiFy-IDEA/tree/TeXiFy-IDEA-2)].

## Planned
* Embedded (automatically updating) PDF viewer.
* Autocomplete for custom commands.
* Inspections.
* Bibliography file (`.bib`) support.
* Support for more LaTeX compilers.
* Menu items to insert LaTeX commands/templates for the forgetful.
* Code folding.
* Easter eggs.
