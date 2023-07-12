# Troubleshooting

## Debugging performance issues

If you are experiencing UI freezes, IntelliJ will generate a thread dump, please upload this file as well.

For any performance issue: if you do not have a favourite profiler yet, you can use VisualVM. Install it using your package manager or go to [https://visualvm.github.io](https://visualvm.github.io)

* First, just run TeXiFy like usual.
* Start VisualVM.
* In the Applications panel on the left, identify the instance of IntelliJ where TeXiFy is running, probably it is named Idea. Right-click on it and open.
* Go to the Sampler tab.
* Click Settings, and click Profile only packages. Specify `nl.hannahsten.**` (or a specific class you want to filter on. Note that if you want to filter for a Kotlin class you have to append `Kt` to the class name, e.g. `nl.hannahsten.texifyidea.editor.UpDownAutoBracketKt`. However, not all classes will appear in the view.)
* Click CPU to start profiling
* Reproduce the performance issue
* Stop the profiling
* Take a Snapshot to view and save results. Note that you may have to click a few more levels open to see the actual methods.
* Now you can zip the nps file and upload it here on GitHub.

## Main file is not detected correctly

If TeXiFy does not detect which file is your main/root LaTeX file, you may experience problems like package imports being placed in the wrong file, or imports not being resolved correctly.
If this is the case, please report a [GitHub issue](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/new/choose).
Until the problem is fixed, you can use a [Magic comment](Editing-a-LaTeX-file.md#magic-comments) as a workaround.

## Known parser issues

TeXiFy relies on a lexer and parser for most of its functionality.
The parser is relatively strict, and it will not accept all valid LaTeX.
This has the advantage that it is relatively easy to implement features which make use of this imposed structure, but you will always be able to create cases of valid LaTeX which break TeXiFy.
We intend to make the parser such that it will accept almost all LaTeX that we think is well-structured and readable.

If you do encounter a parse error that you think is incorrect, please raise an issue.
As a workaround, if you want to keep syntax highlighting for that part you can use magic comments to disable the formatter (see [Code formatting](Code-formatting)) to avoid it incorrectly formatting your file:

```latex
% @formatter:off
...
% @formatter:on
```

If you are fine without the syntax highlighting for that part, you can disable the parser entirely (see [Magic comments](Editing-a-LaTeX-file.md#magic-comments)).
This will ensure that TeXiFy completely ignores this part of the code, and other parts should remain working fine.

```latex
%! parser = off
...
%! parser = on
```

### Examples of known parser bugs

We have two `\begin` commands but only one `\end` command, so the parser will be confused. ([#2141](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/2141))
Since it isn’t clear which `\begin` should be matched with the `\end`, ideally it wouldn’t try to match them at all.
But the only reason this is valid LaTeX at all is the `\if`, and whether we need to match depends on many things, for example whether the `\end` is inside the `\if` or not.

```latex
\newenvironment{messageTable}[2]
{
    \begin{center}
        #2\\
        \ifx{#1}{2}
            \begin{tabular}{|c|c|}
            \hline
            \textbf{Bytes} & \textbf{Name} \\
        \else
            \begin{tabular}{|c|c|c|}
            \hline
            \textbf{Bytes} & \textbf{Name} & \textbf{Value} \\
        \fi

}
{
        \hline
        \end{tabular}
    \end{center}
}

```

## Pasting images and tables into LaTeX

If you drag and drop an image file into a LaTeX file, or paste an image or table from your clipboard, TeXiFy will start a wizard to help you inserting the image or table into your document.
See [Insert Graphics wizard](Menu-entries.md#insert-graphic-wizard) and [Table Creation Wizard](Menu-entries.md#table-creation-wizard) for starting these wizards manually.

### Pasting images from the clipboard

_Since b0.7.3_

You can paste images from your clipboard directly into your LaTeX document. When pasting, you will be prompted by a dialog for saving the image to your workspace. The default folder is "resources", then any source root that is not "src/source(s)", then the source root itself. You can customize the location where the image is going to be saved. You can also specify the file name and the format ("jpg" and "png") are supported. Other formats get converted to "jpg" or "png".

An Insert Graphic dialog will be opened immediately after saving the pasted image.

![demo video](https://raw.githubusercontent.com/wiki/Hannah-Sten/TeXiFy-IDEA/writerside-images/paste-image.gif).