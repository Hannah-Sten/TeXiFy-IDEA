Also see the [option to enable continuous preview of math and TikZ environments](Global-settings#continuous-preview)

## Unicode math preview

TeXiFy can show a unicode preview of some math characters like greek letters.
You can trigger this with your cursor on a math command and clicking menu:Code[Folding > Collapse] or using kbd:[Ctrl + NumPad -].
Clicking on it will expand it again.
TeXiFy shows these previews automatically when a project is opened.

## Equation preview

You can use the Equation Preview by making sure your cursor is in a math environment and clicking menu:Tools[LaTeX > Preview Equation], or using kbd:[Ctrl + Shift + X].

Since version 0.7.11, if you don’t have a custom preamble we use jlatexmath (https://github.com/opencollab/jlatexmath) and it should work out of the box.

If you do have a custom preamble, which jlatexmath does not support, TeXiFy creates a preview by putting your equation in a new temporary (fairly minimal) document and compiling that, and then uses Inkscape to convert to an image.
You may need to have certain fonts installed to make this work.

The current implementation of the Equation Preview was contributed by Sergei Izmailov and requires external dependencies, for which installation instructions follow.
It also relies on system installed fonts, because it converts svg to image, and svg doesn’t include fonts.

### Instructions for Linux

This is only necessary if you want to use a custom preamble (using magic comments), or if you have a TeXiFy version older than 0.7.11.

* Install Inkscape from [inkscape.org/release](https://inkscape.org/release).
* If not using Inkscape 1.0 or later, you have to install the `pdf2svg` package for your distro, for example on Ubuntu with `sudo apt-get install pdf2svg` or on Arch Linux with `sudo pacman -S pdf2svg`.

### Instructions for Windows

This is only necessary if you want to use a custom preamble (using magic comments), or if you have a TeXiFy version older than 0.7.11.

* Install Inkscape from [inkscape.org/release](https://inkscape.org/release), suppose you install it in `C:\Program Files\Inkscape`.
* If not using Inkscape 1.0 or later, you have to install pdf2svg from [github.com/textext/pdf2svg/releases](https://github.com/textext/pdf2svg/releases), suppose you install it in `C:\Program Files\pdf2svg`.
* Add both `C:\Program Files\Inkscape` and (if you installed it) `C:\Program Files\pdf2svg` to your PATH environment variable, for example by searching for Environment Variables on your computer, clicking 'Edit the system environment variables', clicking 'Environment Variables', and under System variables find the one named Path, edit it and insert the paths here. Make sure the paths are separated by a `;` if using Windows 8 or lower.
* Log out and back in.

## TikZ preview

You can use the TikZ Preview by placing your cursor in a `tikzpicture` environment and clicking menu:Tools[LaTeX > TikZ Picture Preview], or using kbd:[Ctrl + Shift + Y].

The TikZ Preview will take TikZ and pgf libraries into account.

The requirements are the same as for the Equation preview.

## Custom preamble for equation and TikZ preview

_Since b.0.6.10_

To include part of your preamble in the equation/TikZ preview, enclose this part with the magic comments `%! begin preamble = tikz` and `%! end preamble = tikz` for the TikZ preview, and `%! begin preamble = math` and `%! end preamble = math` for the math preview.

To include an entire file (for example your `tikzsettings.sty`) in the preamble, put the magic comment `%! preview preamble = tikz` at the start of the file.

For example, to include your custom command `\newcommand{\letters}{\alpha \beta \gamma \delta \epsilon}` in the preamble of the equation preview, use

```latex
\documentclass{article}

%! begin preamble = math
\newcommand{\letters}{\alpha \beta \gamma \delta \epsilon}
%! end preamble = math

\begin{document}
    \[
        x = \letters
    \]
\end{document}
```

## Using the preview on macOS

TeXiFy seems to have problems with running pdflatex, as commented at [#25](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/25#issuecomment-314573002) this might have to do with PATH problems.
The comment links to http://depressiverobot.com/2016/02/05/intellij-path.html which has a couple of workarounds, for example using `open -a "IntelliJ IDEA CE"` or `open -a pycharm`. There is also a more permanent workaround.
If you use the Jetbrains Toolbox, you can find the path in the Toolbox under menu:Settings[Tools].

If you do know how we could fix this on the plugin side, please let us know.
