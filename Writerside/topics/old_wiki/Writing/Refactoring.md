_Since b0.6.9_

Currently, refactoring (renaming) elements is supported for files, labels and environments.

To rename a label, place your cursor on a label definition or reference, e.g. `\ref{some-<cursor>label}` and press kbd:[Shift+F6].

To find out what elements need to be renamed as well (definition and other usages), the functionality from [Find usages](Find-usages) is used.

**❗ IMPORTANT**\
You need to select 'Search for references' if you get a popup to rename an element, in order to let IntelliJ rename all the references to for example a file.

Similarly, you can easily rename an environment, i.e. replace

```latex
\begin{center}
\end{center}
```

with

```latex
\begin{abstract}
\end{abstract}
```

by making sure your cursor is on the environment name inside either the `\begin` or `\end` command and using kbd:[Shift + F6], then type the new name.

When you try to rename an element for which refactoring is not supported, the element will simply not change or in some cases a warning "Inserted identifier is not valid" will be shown.
