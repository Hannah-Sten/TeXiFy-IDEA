Using live templates, you can quickly insert a predefined piece of text by typing just a few characters of a certain key.
You can denote places to which the cursor skips when you press kbd:[Tab] after inserting the live template.

To use a live template, type (a part of) the key, for example `fig`, hit enter when the live template is suggested in the autocomplete, type things and use kbd:[Tab] to skip to the next place to type information.

![live-templates](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Writing/figures/live-templates.gif)

Currently implemented by default are live templates for:

* figures, tables, itemize, enumerate, and in math for summations and integrals;
* sectioning with automatic label (triggered with `\partl`, `\chapl`, `\secl`, etc.), _since b0.7.3_.

You can find these live templates, as well as add your own, under menu:File[Settings > Editor > Live Templates > LaTeX]. _Since b0.7.4:_ the default live templates are disabled in verbatim contexts.

![live-template-settings](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Writing/figures/live-template-settings.png)

For more information, see [https://www.jetbrains.com/help/idea/creating-and-editing-live-templates.html](https://www.jetbrains.com/help/idea/creating-and-editing-live-templates.html)
