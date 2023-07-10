TeXiFy supports autocomplete of labels, all commands from installed LaTeX packages, user defined commands, and (user defined) environments.

This includes for example commands you defined with the `xparse` package.

![xparse-autocomplete](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Writing/figures/xparse-autocomplete.png)

## Autocompletion for all words

If you are looking for a 'dumb' autocompletion mode to autocomplete on any word in the file/project, you can use Hippie completion: [https://www.jetbrains.com/go/guide/tips/cyclic-expand-word/](https://www.jetbrains.com/go/guide/tips/cyclic-expand-word/) and [https://www.jetbrains.com/help/idea/auto-completing-code.html#hippie_completion](https://www.jetbrains.com/help/idea/auto-completing-code.html#hippie_completion)

## GitHub Copilot

GitHub Copilot is machine-learning based autocompletion provided as a separate plugin.
It can give larger suggestions than TeXiFy will do, but in some cases the completion conflicts with or confuses TeXiFy, so use with care.

![172696176-2d49266b-95e3-4f4b-93a3-cefa08204cb9](https://user-images.githubusercontent.com/15669080/172696176-2d49266b-95e3-4f4b-93a3-cefa08204cb9.png)

![172696465-ee68437d-1fbf-4fd7-adc3-76d58711fb23](https://user-images.githubusercontent.com/15669080/172696465-ee68437d-1fbf-4fd7-adc3-76d58711fb23.png)

## Autocompletion of required parameters
_Since b0.6.9_

When invoking autocomplete on a command or environment that takes required parameters, TeXiFy will insert a brace pair for each parameter.
The caret is placed in the first pair, and you can use kbd:[Tab] to skip to the next pair.
Since optional parameters are, well, optional, both the command with and without the optional parameters appear in the autocomplete list.
If you always select the version with optional parameters, after a couple of times IntelliJ will remember your choice and show it first (so above the version without optional parameters).

![required-parameters-autocomplete](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Writing/figures/required-parameters-autocomplete.gif)
![required-parameters-environments](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Writing/figures/required-parameters-environments.gif)

## Autocompletion of commands from installed LaTeX packages.
_Since b0.7.4_

TeXiFy will look in your LaTeX installation for installed LaTeX packages, and then figures out what commands those packages provide to put those in the autocompletion.
The indexing of all packages can take significant time (up to one minute for TeX Live full with thousands of packages) so this is persistent between restarts.
If you want to reset the index, use <ui-path>File | Invalidate Caches / Restart</ui-path>.

Often, the extracted information includes the command parameters and some documentation about the command (see [LaTeX documentation](LaTeX-documentation)).
However, this relies on package authors respecting the LaTeX conventions (using the doc package).
If you find something incorrect, please let us know and then we can determine whether something needs to be improved in the LaTeX package or in TeXiFy.

In the case of TeX Live, TeXiFy will currently not suggest commands from _all_ packages you have installed, because a lot of users have TeX Live full installed, so you would get completion for _all_ commands in _any_ LaTeX package ever written!
This would flood the completion with many commands that are very rarely used.
Therefore, TeXiFy will only suggest commands from packages that you have already included somewhere in your project, directly or indirectly via other packages.

![command-autocomplete1](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Writing/figures/command-autocomplete1.png)
![command-autocomplete2](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Writing/figures/command-autocomplete2.png)

### MiKTeX admin install

With MiKTeX, TeXiFy needs to extract zipped files in order to obtain source files of LaTeX packages.
If you installed MiKTeX as admin, this will not be possible.
The MiKTeX installer clearly warns about this:

![miktex-admin](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Writing/figures/miktex-admin.PNG)

On Linux the warning is less clear though:

![miktex-linux](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/Writing/figures/miktex-linux.png)