
[#maximum-section-size]
== Maximum Section Size

When a section grows bigger than the configured number of characters, TeXiFy will suggest to move the section to another file.

[#label-conventions]
== Label Conventions

Label conventions allow you to configure which commands and environments should have a label. TeXiFy will emit a warning if any of the configured commands or environments does not have a label and provide a quickfix to add a new label. In addition, you can configure the preferred prefix for a label, e.g., `fig:` for a figure. TeXiFy will check that all existing labels adhere to the configured convention, and if not, provide a quickfix to change prefix. Newly added labels will also automatically receive the configured prefix.