# Tools

## Word counting tool

<ui-path>Tools | LaTeX | Word count</ui-path>

Count words in the document of the currently open file and reports on the total number of words in the entire document as well as the number of words in the currently open file only. 
The count for the currently open file at the moment does not count included files.

Since 0.7.4, in case the [texcount](https://app.uio.no/ifi/texcount/intro.html) LaTeX package is available, that will be used.
Otherwise, a built-in word counting tool will be used.
This built-in tool also counts words in LaTeX command arguments, unless the command is known to not typeset its argument. 

## Table Creation Wizard {id="table-creation-wizard"}

<ui-path>Edit | LaTeX | Insert Table...</ui-path>

Displays a dialog with a table creation wizard with a table, caption, and label. Click the plus to add a column, the
minus to remove the column of the currently selected cell, and the pencil to edit this column.
Hit <shortcut>Tab</shortcut> to edit the next cell (same row, column on the right of the current cell).
Hit <shortcut>Tab</shortcut> on the last cell of a column to add a new row.

Each column has a name, and a type.
The available column types are:

* Text: This is normal text, aligned left.
* Math: Use this to display math symbols in your table. All entries in this column will be enclosed in `$..$` when generating
  LaTeX.
* Numbers: As per convention, numbers are aligned on the right.

![Insert table](insert-table-wizard.png)

![Insert table](insert-table-wizard.gif)

_Since b0.7.3:_ the table wizard also opens when you paste an HTML table (in your browser, Excel, LibreOffice, ...) into the editor. _Since b0.7.4_ you can also open the wizard in the Generate menu (<shortcut>Alt+Insert</shortcut>).

## Insert Graphic Wizard {id="insert-graphic-wizard"}

_Since b0.7.3_

<ui-path>Edit | LaTeX | Insert Graphic...</ui-path>

Helps you generate LaTeX for inserting graphics. The wizard also opens when dropping a supported image file into a .tex file. You can also paste an image directly from your clipboard into the editor. You’ll be prompted with the wizard after you have saved the image from your clipboard using another dialog. _Since b0.7.4_ you can also open the wizard in the Generate menu (<shortcut>Alt+Insert</shortcut>).

![demo video](insert-graphic-wizard.gif)

## Insert Dummy Text Wizard {id="insert-dummy-text"}

_Since b0.7.4_

<ui-path>Edit | LaTeX | Insert Dummy Text...</ui-path>

Tool for inserting `blindtext` and `lipsum` commands. Also provides a `raw text` option that just pastes raw dummy text in the editor. There is currently one style available: TeXiFy IDEA Ipsum. This is a report and business style ipsum generator.

You can also access this action via the Generate menu by pressing <shortcut>Alt + Insert</shortcut> in a LaTeX document.

## Pasting HTML into a LaTeX file {id="paste-html-into-latex"}

If you copy HTML to your clipboard, when pasting into a LaTeX file the html will be automatically converted to LaTeX.
In particular, for tables or images the table or image wizard will show.
You can also use Pandoc instead of the built-in translator, you can configure this in [settings](TeXiFy-settings.md).

![html to latex](html-paste.gif)

## Clear Auxiliary Files {id="clear-aux-files"}

<ui-path>Tools | LaTeX | Clear Auxiliary Files</ui-path>

Searches the entire project, i.e., every folder in the project module, for auxiliary files and deletes them. The files with the following extensions are defined as auxiliary files:

aux, bbl, bcf, brf, fls, idx, ind, lot, lot, nav, out, snm, toc

If auxiliary files in a certain directory do not get deleted, make sure you mark that directory as a source directory by right-clicking on it and selecting <ui-path>Mark Directory as | Sources Root</ui-path>.

## Clear generated files

_Since b0.6.7_

<ui-path>Tools | LaTeX | Clear Generated Files</ui-path>

Be careful, you might not be able to fully undo this operation!

This will delete all generated files in `src/`, `auxil/` and `out/`, including pdfs and generated `_minted-*` folders.

## Crash reporting dialog

When there is an internal error in TeXiFy, a notification will be shown from which you can directly report the issue to our issue tracker.


## Refresh fileset

_Since b0.11.0_

<ui-path>Tools | LaTeX | Refresh Fileset</ui-path>

This will call for a refresh of the fileset, updating the references in commands like `\input` and `\usepackage`.
Can be useful with customized automatic refresh periods in the [settings](TeXiFy-settings.md#fileset-refresh-period).

## Performance diagnostics

_Since b0.11.0_

<ui-path>Tools | LaTeX | Performance Diagnostics</ui-path>

Check some performance diagnostics of TeXiFy IDEA (currently for fileset build performance).

Usually, it should not take more than a second.
Please report any performance issues you find to our issue tracker.

See also [Refresh fileset](#refresh-fileset) and [Fileset refresh period](TeXiFy-settings.md#fileset-refresh-period).

![Performance Diagnostics](performance-diagnostics.png)
