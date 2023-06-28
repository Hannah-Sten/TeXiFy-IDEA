## Duplicate ID

You cannot have two bibtex entries with the same id.

## Missing bibliography style

When you include a bibliography, you should also define a bibliography style, for example `\bibliographystyle{plain}`.

## Duplicate bibliography style commands

You should only have one `\bibliographystyle` command.

## Same bibliography is included multiple times

It is not useful to include the same bibliography in the same document multiple times.

## Bib entry is not cited

_Since b0.7.3_

This inspection detects entries in bibliographies that are not cited in the main document.
It greys out the identifier of the entry, and provides a quick fix to safe delete this entry.
When using the safe deletion, TeXiFy will also search for cites that appear in comments, and you will be warned if any are found, see Figure 1.

**View of the safe delete that found a usage in a comment.**

![bibtex-safe-delete-entry](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Bibtex/figures/bibtex-safe-delete-entry.png)
