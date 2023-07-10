_Since b0.6.9_

TeXiFy supports the use of the `\graphicspath` command from the `graphicx` package.
You can use this to add extra directories in which graphicx will search for images.

For example, if you have images in a path `/path/to/figures` you could write

```latex
\documentclass{article}
\usepackage{graphicx}
\graphicspath{{/path/to/figures/}}
\begin{document}
    \begin{figure}
        \includegraphics{figure.jpg}
    \end{figure}
\end{document}
```

You can also use relative paths, but no matter what path you use it _has_ to end in a forward slash `/`.
You also need to use forward slashes on Windows.

You can include multiple search paths by continuing the list, like `\includegraphics{{/path1/}{../path2/}}`.

For more information, see the documentation linked at [https://ctan.org/pkg/graphicx](https://ctan.org/pkg/graphicx)