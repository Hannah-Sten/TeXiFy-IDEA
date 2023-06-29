Each project can either use the globally configured conventions (called `Default`) or project specific conventions. You can select the conventions to use with the `Scheme` combobox at the top of the settings page.

## Maximum Section Size

When a section grows bigger than the configured number of characters, and there are also other sections in the file, TeXiFy will show a warning and suggest to move the section to separate file.

See [Too large section inspection](Code-style-issues#too-large-section).

## Label Conventions

Label conventions allow you to configure which commands and environments should have a label and which prefix the label should habe. TeXiFy will show a warning if any of the configured commands or environments does not have a label and provide a quickfix to add a new label. 

![Missing Label Warning](https://user-images.githubusercontent.com/7955528/153943614-d5671569-dfa4-47c8-9ae2-e61db11c90b1.png)

In addition, you can configure the preferred prefix for a label, e.g., `fig:` for a figure or `sec:` for a section. TeXiFy will check that all existing labels adhere to the configured convention, and if not, provide a quickfix to change the prefix. Newly added labels will also automatically receive the configured prefix.

![Added Label](https://user-images.githubusercontent.com/7955528/153943754-176325aa-cd3c-4efb-8ec6-0b561d8ac3dc.png)

See [Label conventions inspection](Code-style-issues#Label-conventions) and [Missing labels inspection](Code-style-issues#Missing-labels).
