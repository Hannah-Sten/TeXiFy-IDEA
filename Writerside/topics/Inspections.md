# Inspections

Inspections check your LaTeX and BibTeX as you write, and highlight any possible issues.
If you see a minor bug in an inspection, like some missing metadata about commands or environments, you are encouraged to check if you can [fix it yourself](Contributing-to-the-source-code.md#adding-an-inspection).


## Suppress inspections

Most inspections can be ignored for only a single line, environment or file.
To do this, use the format `%! Suppress = MyInspectionName`.
The easiest way is usually to use the available context menu on the inspection warning itself.
In some cases, like block of text spanning multiple lines, placing a magic comment in front of it will suppress the inspection for the whole block, instead of for a single line.

![Suppression](suppression-menu.png)

![Suppression](suppression.png)

## Custom inspections

If you have project-specific issues which need to be highlighted, but are of no use for the general public, you can create your own search and replace inspections.
See [Create custom inspections | IntelliJ IDEA Documentation](https://www.jetbrains.com/help/idea/creating-custom-inspections.html).

## Spellchecking

IntelliJ has a default spellchecking inspection, see [Spellchecking | IntelliJ IDEA Documentation](https://www.jetbrains.com/help/idea/spellchecking.html#configure-the-typo-inspection).
To enable spellcheck everywhere in LaTeX code, see [TeXiFy settings](TeXiFy-settings.md#enable-spellcheck-in-all-scopes).

## Grammar checking

_Since b0.6.8_

TeXiFy provides support for Grazie, which is a grammar and spellchecking plugin.

Make sure it is installed and enabled by going to <ui-path>File | Settings | Plugins</ui-path>.
You can switch on or off grammar rules in <ui-path>File | Settings | Tools | Grazie</ui-path>.

For more information about Grazie, see [https://plugins.jetbrains.com/plugin/12175-grazie/](https://plugins.jetbrains.com/plugin/12175-grazie/) and [Grammar | IntelliJ IDEA Documentation](https://www.jetbrains.com/help/idea/grammar.html).

If you want more advanced features than Grazie provides (e.g. provide your own rules in addition to the included ones), have a look at the machine-learning based [https://plugins.jetbrains.com/plugin/16136-grazie-professional](https://plugins.jetbrains.com/plugin/16136-grazie-professional). While Grazie comes bundled with IntelliJ, Grazie Professional must be installed explicitely.

![Grazie](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Writing/grazie.png)
