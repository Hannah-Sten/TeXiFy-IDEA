# Inspections

Inspections check your LaTeX and BibTeX as you write, and highlight any possible issues.
If you see a minor bug in an inspection, like some missing metadata about commands or environments, you are encouraged to check if you can [fix it yourself](Contributing-to-TeXiFy#editing-magic).


## Suppress inspections

Most inspections can be ignored for only a single line, environment or file.
To do this, use the format `%! Suppress = MyInspectionName`.
The easiest way is usually to use the available context menu on the inspection warning itself.

![Suppression](suppression-menu.png)

![Suppression](suppression.png)

## Custom inspections

If you have project-specific issues which need to be highlighted, but are of no use for the general public, you can create your own search and replace inspections.
See [Create custom inspections | IntelliJ IDEA Documentation](https://www.jetbrains.com/help/idea/creating-custom-inspections.html).