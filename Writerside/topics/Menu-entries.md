## Word counting tool

menu:Analyze[LaTeX > Word count]

Count words in the currently open file, excluding LaTeX commands.
Since 0.7.4, in case the [texcount](https://app.uio.no/ifi/texcount/intro.html) LaTeX package is available, that will be used.
It also counts words in included files.
Otherwise, a built-in word counting tool will be used.
Also includes the text in:

* Sections (\section, \subsection etc.)
* csquotes (\enquote)

## [[file-templates]] Customizable file templates

menu:Settings[Editor > File and Code Templates]

Right-click in Project tool window, then menu:New[LaTeX File].

![new-file](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/figures/new-file.png)

## [[table-creation-wizard]]Table Creation Wizard

menu:Edit[LaTeX > Insert Table...]

Displays a dialog with a table creation wizard with a table, caption, and label. Click the plus to add a column, the
minus to remove the column of the currently selected cell, and the pencil to edit this column.
Hit kbd:[Tab] to edit the next cell (same row, column on the right of the current cell).
Hit kbd:[Tab] on the last cell of a column to add a new row.

Each column has a name, and a type.
The available column types are:

* Text: This is normal text, aligned left.
* Math: Use this to display math symbols in your table. All entries in this column will be enclosed in `$..$` when generating
LaTeX.
* Numbers: As per convention, numbers are aligned on the right.

_Since b0.7.3:_ the table wizard also opens when you paste an HTML table (in your browser, Excel, LibreOffice, ...) into the editor. _Since b0.7.4_ you can also open the wizard in the Generate menu (kbd:[Alt+Insert]).

## [[insert-graphic-wizard]]Insert Graphic Wizard

_Since b0.7.3_

menu:Edit[LaTeX > Insert Graphic...]

Helps you generate LaTeX for inserting graphics. The wizard also opens when dropping a supported image file into a .tex file. You can also paste an image directly from your clipboard into the editor. You’ll be prompted with the wizard after you have saved the image from your clipboard using another dialog. _Since b0.7.4_ you can also open the wizard in the Generate menu (kbd:[Alt+Insert]).

See [demo video](https://user-images.githubusercontent.com/17410729/103922867-b0108300-5114-11eb-92d8-25d63eaeb1f1.mp4).

## [[insert-dummy-text]]Insert Dummy Text Wizard

_Since b0.7.4_

menu:Edit[LaTeX > Insert Dummy Text...]

Tool for inserting `blindtext` and `lipsum` commands. Also provides a `raw text` option that just pastes raw dummy text in the editor. There is currently one style available: TeXiFy IDEA Ipsum. This is a report and business style ipsum generator.

You can also access this action via the Generate menu by pressing kbd:[Alt + Insert] in a LaTeX document.

## [[clear-aux-files]]Clear Auxiliary Files

menu:Tools[LaTeX > Clear Auxiliary Files]

Searches the entire project, i.e., every folder in the project module, for auxiliary files and deletes them. The files with the following extensions are defined as auxiliary files:

aux, bbl, bcf, brf, fls, idx, ind, lot, lot, nav, out, snm, toc

If auxiliary files in a certain directory do not get deleted, make sure you mark that directory as a source directory by right-clicking on it and selecting menu:Mark Directory as[Sources Root].

## Clear generated files

_Since b0.6.7_

menu:Tools[LaTeX > Clear Generated Files]

Be careful, you might not be able to fully undo this operation!

This will delete all generated files in `src/`, `auxil/` and `out/`, including pdfs and generated `_minted-*` folders.
